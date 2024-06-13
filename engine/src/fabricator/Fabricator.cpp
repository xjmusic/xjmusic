// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/fabricator/Fabricator.h"

using namespace XJ;

Fabricator::Fabricator(
    FabricatorFactory fabricatorFactory,
    FabricationEntityStore store,
    ContentStore sourceMaterial,
    int segmentId,
    double outputFrameRate,
    int outputChannels,
    std::optional<Segment::Type> overrideSegmentType
  )  {
    this->store = store;
    this->sourceMaterial = sourceMaterial;
    this->outputFrameRate = outputFrameRate;
    this->outputChannels = outputChannels;

    // keep elapsed time based on system nano time
    startAtSystemNanoTime = std::chrono::high_resolution_clock::now();

    // read the chain, configs, and bindings
    chain = store.readChain()
      .orElseThrow(() -> new FabricationFatalException("No chain found"));
    try {
      templateConfig = new TemplateConfig(chain.getTemplateConfig());
    } catch (ValueException e) {
      throw new FabricationFatalException("Failed to read template config!");
    }
    templateBindings = sourceMaterial.getTemplateBindings();
    boundProgramIds = ChainUtils.targetIdsOfType(templateBindings, TemplateBinding::Type.Program);
    boundInstrumentIds = ChainUtils.targetIdsOfType(templateBindings, TemplateBinding::Type.Instrument);
    LOG.debug("[segId={}] Chain {} configured with {} and bound to {} ", segmentId, chain.id, templateConfig, CsvUtils.prettyFrom(templateBindings, "and"));

    // set up the segment retrospective
    retrospective = fabricatorFactory.loadRetrospective(segmentId);

    // digest previous instrument audio
    preferredAudios = computePreferredInstrumentAudio();

    // the current segment on the workbench
    this.segmentId = segmentId;

    // Override the segment type by passing the fabricator a segment type on creation
    // live performance modulation https://github.com/xjmusic/workstation/issues/197
    if (Objects.nonNull(overrideSegmentType)) {
      type = overrideSegmentType;
    }

    // final pre-flight check
    ensureShipKey();
  }

  
  void addMessage(SegmentMessageType messageType, std::string body) {
    try {
      SegmentMessage msg;
      msg.setId(randomUUID());
      msg.setSegmentId(getSegment().id);
      msg.setType(messageType);
      msg.setBody(body);
      put(msg, false);
    } catch (FabricationException e) {
      LOG.warn("Failed to add message!", e);
    }
  }

  
  void addErrorMessage(std::string body) {
    addMessage(SegmentMessageType.ERROR, body);
  }

  
  void addWarningMessage(std::string body) {
    addMessage(SegmentMessageType.WARNING, body);
  }

  
  void addInfoMessage(std::string body) {
    addMessage(SegmentMessageType.INFO, body);
  }



  void deleteEntity(int segmentId, std::string type, UUID id) {
    store.delete(segmentId, type, id);
  }

  
  std::vector<SegmentChoiceArrangement> getArrangements() {
    return store.readAll(segmentId, SegmentChoiceArrangement.class);
  }

  
  std::vector<SegmentChoiceArrangement> getArrangements(std::vector<SegmentChoice> choices) {
    std::vector<UUID> choiceIds = EntityUtils.idsOf(choices);
    return getArrangements().stream().filter(arrangement -> choiceIds.contains(arrangement.getSegmentChoiceId())).collect(Collectors.toList());
  }

  
  Chain getChain() {
    return chain;
  }

  
  TemplateConfig getTemplateConfig() {
    return templateConfig;
  }

  
  std::vector<SegmentChoice> getChoices() {
    return store.readAll(segmentId, SegmentChoice.class);
  }

  
  std::optional<SegmentChord> getChordAt(float position) {
    if (!chordAtPosition.containsKey(position)) {
      std::optional<SegmentChord> foundChord = std::optional.empty();
      float foundPosition = null;

      // we assume that these entities are in order of position ascending
      for (SegmentChord segmentChord : getSegmentChords()) {
        // if it's a better match (or no match has yet been found) then use it
        if (Objects.isNull(foundPosition) || (segmentChord.getPosition() > foundPosition && segmentChord.getPosition() <= position)) {
          foundPosition = segmentChord.getPosition();
          foundChord = std::optional.of(segmentChord);
        }
      }
      chordAtPosition.put(position, foundChord);
    }

    return chordAtPosition.get(position);
  }

  
  std::optional<SegmentChoice> getCurrentMainChoice() {
    return getChoiceOfType(Program::Type::Main);
  }

  
  std::vector<SegmentChoice> getCurrentDetailChoices() {
    return getBeatChoices();
  }

  
  std::optional<SegmentChoice> getCurrentBeatChoice() {
    return getChoiceOfType(Program::Type::Beat);
  }

  
  Set<Instrument::Type> getDistinctChordVoicingTypes() {
    if (Objects.isNull(distinctChordVoicingTypes)) {
      var mainChoice = getCurrentMainChoice();
      if (mainChoice.isEmpty()) return Set.of();
      var voicings = sourceMaterial.getSequenceChordVoicingsOfProgram(mainChoice.get().programId);
      distinctChordVoicingTypes = voicings.stream().flatMap(voicing -> {
        try {
          return Stream.of(getProgramVoiceType(voicing));
        } catch (FabricationException e) {
          LOG.warn("Failed to get distinct chord voicing type!", e);
          return Stream.empty();
        }
      }).collect(Collectors.toSet());
    }

    return distinctChordVoicingTypes;
  }

  
  Long getElapsedMicros() {
    return (System.nanoTime() - startAtSystemNanoTime) / NANOS_PER_MICRO;
  }

  
  InstrumentConfig getInstrumentConfig(Instrument instrument) {
    if (!instrumentConfigs.containsKey(instrument.id))
      instrumentConfigs.put(instrument.id, new InstrumentConfig(instrument));
    return instrumentConfigs.get(instrument.id);
  }

  
  std::optional<SegmentChoice> getChoiceIfContinued(ProgramVoice voice) {
    try {
      if (!Objects.equals(Segment::Type::Continue, getSegment().type)) return std::optional.empty();
      return retrospective.getChoices().stream().filter(choice -> {
        var candidateVoice = sourceMaterial.getProgramVoice(choice.programVoiceId);
        return candidateVoice.isPresent() && Objects.equals(candidateVoice.get().name, voice.name) && Objects.equals(candidateVoice.get().type, voice.type);
      }).findFirst();

    } catch (Exception e) {
      LOG.warn(formatLog(std::string.format("Could not get previous voice instrumentId for voiceName=%s", voice.name)), e);
      return std::optional.empty();
    }
  }

  
  std::optional<SegmentChoice> getChoiceIfContinued(Instrument::Type instrumentType) {
    try {
      if (!Objects.equals(Segment::Type::Continue, getSegment().type)) return std::optional.empty();
      return retrospective.getChoices().stream().filter(choice -> Objects.equals(instrumentType, choice.instrumentType)).findFirst();

    } catch (Exception e) {
      LOG.warn(formatLog(std::string.format("Could not get previous choice for instrumentType=%s", instrumentType)), e);
      return std::optional.empty();
    }
  }

  
  std::optional<SegmentChoice> getChoiceIfContinued(Instrument::Type instrumentType, Instrument::Mode instrumentMode) {
    try {
      if (!Objects.equals(Segment::Type::Continue, getSegment().type)) return std::optional.empty();
      return retrospective.getChoices().stream().filter(choice -> Objects.equals(instrumentType, choice.instrumentType) && Objects.equals(instrumentMode, choice.getInstrumentMode())).findFirst();

    } catch (Exception e) {
      LOG.warn(formatLog(std::string.format("Could not get previous choice for instrumentType=%s", instrumentType)), e);
      return std::optional.empty();
    }
  }

  
  std::vector<SegmentChoice> getChoicesIfContinued(Program::Type programType) {
    try {
      if (!Objects.equals(Segment::Type::Continue, getSegment().type)) return Set.of();
      return retrospective.getChoices().stream().filter(choice -> Objects.equals(programType, choice.programType)).collect(Collectors.toSet());

    } catch (Exception e) {
      LOG.warn(formatLog(std::string.format("Could not get previous choice for programType=%s", programType)), e);
      return Set.of();
    }
  }

  
  std::string computeCacheKeyForVoiceTrack(SegmentChoiceArrangementPick pick) {
    std::string cacheKey = sourceMaterial().getProgramSequencePatternEvent(pick.getProgramSequencePatternEventId()).flatMap(event -> sourceMaterial().getTrackOfEvent(event).map(ProgramVoiceTrack::getProgramVoiceId)).map(UUID::toString).orElse(UNKNOWN_KEY);

    return std::string.format(KEY_VOICE_TRACK_TEMPLATE, cacheKey, pick.getEvent());
  }

  
  Chord getKeyForChoice(SegmentChoice choice) throws FabricationException {
    std::optional<Program> program = getProgram(choice);
    if (ValueUtils.isSet(choice.programSequenceBindingId)) {
      var sequence = getSequence(choice);
      if (sequence.isPresent() && !StringUtils.isNullOrEmpty(sequence.get().getKey()))
        return Chord.of(sequence.get().getKey());
    }

    return Chord.of(program.orElseThrow(() -> new FabricationException("Cannot get key for nonexistent choice!")).getKey());
  }

  
  std::optional<ProgramSequence> getProgramSequence(SegmentChoice choice) {
    if (Objects.nonNull(choice.programSequenceId))
      return sourceMaterial.getProgramSequence(choice.programSequenceId);
    if (Objects.isNull(choice.programSequenceBindingId)) return std::optional.empty();
    var psb = sourceMaterial.getProgramSequenceBinding(choice.programSequenceBindingId);
    if (psb.isEmpty()) return std::optional.empty();
    return sourceMaterial.getProgramSequence(psb.get().programSequenceId);
  }

  
  std::optional<SegmentChoice> getMacroChoiceOfPreviousSegment() {
    if (Objects.isNull(macroChoiceOfPreviousSegment))
      macroChoiceOfPreviousSegment = retrospective.getPreviousChoiceOfType(Program::Type::Macro);
    return macroChoiceOfPreviousSegment;
  }

  
  std::optional<SegmentChoice> getPreviousMainChoice() {
    if (Objects.isNull(mainChoiceOfPreviousSegment))
      mainChoiceOfPreviousSegment = retrospective.getPreviousChoiceOfType(Program::Type::Main);
    return mainChoiceOfPreviousSegment;
  }

  
  ProgramConfig getCurrentMainProgramConfig() throws FabricationException {
    try {
      return new ProgramConfig(
        sourceMaterial.getProgram(getCurrentMainChoice()
            .orElseThrow(() -> new FabricationException("No current main choice!")).programId)
          .orElseThrow(() -> new FabricationException("Failed to retrieve current main program config!")));

    } catch (ValueException e) {
      throw new FabricationException(e);
    }
  }

  
  std::optional<ProgramSequence> getCurrentMainSequence() {
    var mc = getCurrentMainChoice();
    if (mc.isEmpty()) return std::optional.empty();
    return getProgramSequence(mc.get());
  }

  
  std::optional<ProgramSequence> getPreviousMainSequence() {
    var mc = getPreviousMainChoice();
    if (mc.isEmpty()) return std::optional.empty();
    return getProgramSequence(mc.get());
  }

  
  MemeIsometry getMemeIsometryOfNextSequenceInPreviousMacro() {
    var previousMacroChoice = getMacroChoiceOfPreviousSegment();
    if (previousMacroChoice.isEmpty())
      return MemeIsometry.none();

    var previousSequenceBinding = sourceMaterial().getProgramSequenceBinding(previousMacroChoice.get().programSequenceBindingId);
    if (previousSequenceBinding.isEmpty())
      return MemeIsometry.none();

    var nextSequenceBinding = sourceMaterial().getBindingsAtOffsetOfProgram(previousMacroChoice.get().programId,
      previousSequenceBinding.get().getOffset() + 1, true);

    return MemeIsometry.of(templateConfig.getMemeTaxonomy(),
      Stream.concat(
        sourceMaterial.getMemesOfProgram(previousMacroChoice.get().programId).stream().map(ProgramMeme::getName),
        nextSequenceBinding.stream().flatMap(programSequenceBinding ->
          sourceMaterial.getMemesOfSequenceBinding(programSequenceBinding.id).stream().map(ProgramSequenceBindingMeme::getName))
      ).collect(Collectors.toList()));
  }

  
  MemeIsometry getMemeIsometryOfSegment() {
    return MemeIsometry.of(templateConfig.getMemeTaxonomy(), EntityUtils.namesOf(getSegmentMemes()));
  }

  
  int getNextSequenceBindingOffset(SegmentChoice choice) {
    if (ValueUtils.isEmpty(choice.programSequenceBindingId)) return 0;

    var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.programSequenceBindingId);
    int sequenceBindingOffset = getSequenceBindingOffsetForChoice(choice);
    int offset = null;
    if (sequenceBinding.isEmpty()) return 0;
    for (int availableOffset : sourceMaterial.getAvailableOffsets(sequenceBinding.get()))
      if (0 < availableOffset.compareTo(sequenceBindingOffset))
        if (Objects.isNull(offset) || 0 > availableOffset.compareTo(offset)) offset = availableOffset;

    // if none found, loop back around to zero
    return Objects.nonNull(offset) ? offset : 0;

  }

  
  std::vector<std::string> getNotes(SegmentChordVoicing voicing) {
    return new ArrayList<>(CsvUtils.split(voicing.notes));
  }

  
  std::vector<SegmentChoiceArrangementPick> getPicks() {
    return store.readAll(segmentId, SegmentChoiceArrangementPick.class);
  }

  
  std::vector<SegmentChoiceArrangementPick> getPicks(SegmentChoice choice) {
    if (!picksForChoice.containsKey(choice.id)) {
      var arrangementIds = getArrangements().stream().filter(a -> a.getSegmentChoiceId().equals(choice.id)).map(SegmentChoiceArrangement::getId).toList();
      picksForChoice.put(choice.id, getPicks().stream()
        .filter(p -> arrangementIds.contains(p.getSegmentChoiceArrangementId()))
        .sorted(Comparator.comparing(SegmentChoiceArrangementPick::getStartAtSegmentMicros)).toList());
    }
    return picksForChoice.get(choice.id);
  }

  
  std::optional<InstrumentAudio> getPreferredAudio(std::string parentIdent, std::string ident) {
    std::string cacheKey = std::string.format(KEY_VOICE_NOTE_TEMPLATE, parentIdent, ident);

    if (preferredAudios.containsKey(cacheKey)) return std::optional.of(preferredAudios.get(cacheKey));

    return std::optional.empty();
  }

  
  std::optional<Program> getProgram(SegmentChoice choice) {
    return sourceMaterial.getProgram(choice.programId);
  }

  
  ProgramConfig getProgramConfig(Program program) throws FabricationException {
    try {
      return new ProgramConfig(program);
    } catch (ValueException e) {
      throw new FabricationException(e);
    }
  }

  
  std::vector<ProgramSequenceChord> getProgramSequenceChords(ProgramSequence programSequence) {
    if (!completeChordsForProgramSequence.containsKey(programSequence.id)) {
      Map<float, ProgramSequenceChord> chordForPosition = new HashMap<>();
      Map<float, int> validVoicingsForPosition = new HashMap<>();
      for (ProgramSequenceChord chord : sourceMaterial.getChordsOfSequence(programSequence)) {
        int validVoicings = sourceMaterial.getVoicingsOfChord(chord).stream().map(V -> CsvUtils.split(V.notes).size()).reduce(0, int::sum);
        if (!validVoicingsForPosition.containsKey(chord.getPosition()) || validVoicingsForPosition.get(chord.getPosition()) < validVoicings) {
          validVoicingsForPosition.put(chord.getPosition(), validVoicings);
          chordForPosition.put(chord.getPosition(), chord);
        }
      }
      completeChordsForProgramSequence.put(programSequence.id, chordForPosition.values());
    }

    return completeChordsForProgramSequence.get(programSequence.id);
  }

  
  NoteRange getProgramRange(UUID programId, Instrument::Type instrumentType) {
    var cacheKey = std::string.format("%s__%s", programId, instrumentType);

    if (!rangeForChoice.containsKey(cacheKey)) {
      rangeForChoice.put(cacheKey, computeProgramRange(programId, instrumentType));
    }

    return rangeForChoice.get(cacheKey);
  }

  NoteRange computeProgramRange(UUID programId, Instrument::Type instrumentType) {
    return NoteRange.ofStrings(
      sourceMaterial.getSequencePatternEventsOfProgram(programId).stream()
        .filter(event -> sourceMaterial.getVoiceOfEvent(event).map(voice -> Objects.equals(voice.type, instrumentType)).orElse(false)
          && !Objects.equals(Note.of(event.getTones()).getPitchClass(), PitchClass.None))
        .flatMap(programSequencePatternEvent -> CsvUtils.split(programSequencePatternEvent.getTones()).stream())
        .collect(Collectors.toList()));
  }

  
  int getProgramRangeShiftOctaves(Instrument::Type type, NoteRange sourceRange, NoteRange targetRange) throws FabricationException {
    var cacheKey = std::string.format("%s__%s__%s", type, sourceRange.toString(Accidental.None), targetRange.toString(Accidental.None));

    if (!rangeShiftOctave.containsKey(cacheKey)) switch (type) {
      case Bass -> rangeShiftOctave.put(cacheKey, computeLowestOptimalRangeShiftOctaves(sourceRange, targetRange));
      case Drum -> {
        return 0;
      }
      case Pad, Stab, Sticky, Stripe ->
        rangeShiftOctave.put(cacheKey, NoteRange.computeMedianOptimalRangeShiftOctaves(sourceRange, targetRange));
    }

    return rangeShiftOctave.get(cacheKey);
  }

  
  int getProgramTargetShift(Instrument::Type instrumentType, Chord fromChord, Chord toChord) {
    if (!fromChord.isPresent()) return 0;
    var cacheKey = std::string.format("%s__%s__%s", instrumentType, fromChord, toChord);
    if (!targetShift.containsKey(cacheKey)) {
      if (instrumentType.equals(Instrument::Type::Bass)) {
        targetShift.put(cacheKey, fromChord.getRoot().delta(toChord.getSlashRoot()));
      } else {
        targetShift.put(cacheKey, fromChord.getRoot().delta(toChord.getRoot()));
      }
    }

    return targetShift.get(cacheKey);
  }

  
  Program::Type getProgramType(ProgramVoice voice) throws FabricationException {
    return sourceMaterial.getProgram(voice.programId).orElseThrow(() -> new FabricationException("Could not get program!")).type;
  }

  
  Instrument::Type getProgramVoiceType(ProgramSequenceChordVoicing voicing) throws FabricationException {
    return sourceMaterial.getProgramVoice(voicing.programVoiceId).orElseThrow(() -> new FabricationException("Could not get voice!")).type;
  }

  
  NoteRange getProgramVoicingNoteRange(Instrument::Type type) {
    if (!voicingNoteRange.containsKey(type)) {
      voicingNoteRange.put(type, NoteRange.ofStrings(getChordVoicings().stream().filter(SegmentUtils::containsAnyValidNotes).filter(segmentChordVoicing -> Objects.equals(segmentChordVoicing.type, type)).flatMap(segmentChordVoicing -> getNotes(segmentChordVoicing).stream()).collect(Collectors.toList())));
    }

    return voicingNoteRange.get(type);
  }

  
  std::optional<ProgramSequence> getRandomlySelectedSequence(Program program) {
    var bag = MarbleBag.empty();
    sourceMaterial.getProgramSequences().stream().filter(s -> Objects.equals(s.programId, program.id)).forEach(sequence -> bag.add(1, sequence.id));
    if (bag.isEmpty()) return std::optional.empty();
    return sourceMaterial.getProgramSequence(bag.pick());
  }

  
  std::optional<ProgramSequenceBinding> getRandomlySelectedSequenceBindingAtOffset(Program program, int offset) {
    var bag = MarbleBag.empty();
    for (ProgramSequenceBinding sequenceBinding : sourceMaterial.getBindingsAtOffsetOfProgram(program, offset, true))
      bag.add(1, sequenceBinding.id);
    if (bag.isEmpty()) return std::optional.empty();
    return sourceMaterial.getProgramSequenceBinding(bag.pick());
  }

  
  std::optional<ProgramSequencePattern> getRandomlySelectedPatternOfSequenceByVoiceAndType(SegmentChoice choice) {
    var bag = MarbleBag.empty();
    sourceMaterial.getProgramSequencePatterns().stream().filter(pattern -> Objects.equals(pattern.programSequenceId, choice.programSequenceId)).filter(pattern -> Objects.equals(pattern.programVoiceId, choice.programVoiceId)).forEach(pattern -> bag.add(1, pattern.id));
    if (bag.isEmpty()) return std::optional.empty();
    return sourceMaterial.getProgramSequencePattern(bag.pick());
  }

  
  std::optional<Note> getRootNoteMidRange(std::string voicingNotes, Chord chord) {
    return rootNotesByVoicingAndChord.computeIfAbsent(std::string.format("%s_%s", voicingNotes, chord.name),
      (std::string key) -> NoteRange.ofStrings(CsvUtils.split(voicingNotes)).getNoteNearestMedian(chord.getSlashRoot()));
  }

  
  void putStickyBun(StickyBun bun) throws JsonProcessingException, FabricationException {
    store.put(new SegmentMeta()
      .id(randomUUID())
      .segmentId(getSegment().id)
      .key(bun.computeMetaKey())
      .value(jsonProvider.getMapper().writeValueAsString(bun)));
  }

  
  std::optional<StickyBun> getStickyBun(UUID eventId) {
    if (!templateConfig.isStickyBunEnabled()) return std::optional.empty();
    //
    var currentMeta = getSegmentMeta(StickyBun.computeMetaKey(eventId));
    if (currentMeta.isPresent()) {
      try {
        return std::optional.of(jsonProvider.getMapper().readValue(currentMeta.get().getValue(), StickyBun.class));
      } catch (JsonProcessingException e) {
        addErrorMessage(std::string.format("Failed to deserialize current segment meta value StickyBun JSON for Event[%s]", eventId));
      }
    }
    //
    var previousMeta = retrospective.getPreviousMeta(StickyBun.computeMetaKey(eventId));
    if (previousMeta.isPresent()) {
      try {
        return std::optional.of(jsonProvider.getMapper().readValue(previousMeta.get().getValue(), StickyBun.class));
      } catch (JsonProcessingException e) {
        addErrorMessage(std::string.format("Failed to deserialize previous segment meta value StickyBun JSON for Event[%s]", eventId));
      }
    }
    var event = sourceMaterial.getProgramSequencePatternEvent(eventId);
    if (event.isEmpty()) {
      addErrorMessage(std::string.format("Failed to get StickyBun for Event[%s] because it does not exist", eventId));
      return std::optional.empty();
    }
    var bun = new StickyBun(eventId, CsvUtils.split(event.get().getTones()).size());
    try {
      putStickyBun(bun);
    } catch (FabricationException e) {
      addErrorMessage(std::string.format("Failed to put StickyBun for Event[%s] because %s", eventId, e.getMessage()));
    } catch (JsonProcessingException e) {
      addErrorMessage(std::string.format("Failed to serialize segment meta value StickyBun JSON for Event[%s]", eventId));
    }
    return std::optional.of(bun);
  }

  private std::optional<SegmentMeta> getSegmentMeta(std::string key) {
    return store.readAll(segmentId, SegmentMeta.class).stream()
      .filter(m -> Objects.equals(key, m.getKey()))
      .findAny();
  }

  
  long getSegmentMicrosAtPosition(double tempo, double position) {
    return (long) (getMicrosPerBeat(tempo) * position);
  }

  
  long getTotalSegmentMicros() {
    return Objects.requireNonNull(getSegment().durationMicros);
  }

  
  Segment getSegment() {
    return store.readSegment(segmentId).orElseThrow(() -> new RuntimeException(std::string.format("Found no Segment[%d]", segmentId)));
  }

  
  std::vector<SegmentChord> getSegmentChords() {
    return store.readAll(segmentId, SegmentChord.class).stream()
      .sorted(Comparator.comparing(SegmentChord::getPosition))
      .collect(Collectors.toList());
  }

  
  std::vector<SegmentChordVoicing> getChordVoicings() {
    return store.readAll(segmentId, SegmentChordVoicing.class);
  }

  
  std::vector<SegmentMeme> getSegmentMemes() {
    return store.readAll(segmentId, SegmentMeme.class);
  }

  
  std::optional<ProgramSequence> getSequence(SegmentChoice choice) {
    std::optional<Program> program = getProgram(choice);
    if (program.isEmpty()) return std::optional.empty();
    if (ValueUtils.isSet(choice.programSequenceBindingId)) {
      var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.programSequenceBindingId);
      if (sequenceBinding.isPresent())
        return sourceMaterial.getProgramSequence(sequenceBinding.get().programSequenceId);
    }

    if (!sequenceForChoice.containsKey(choice))
      getRandomlySelectedSequence(program.get()).ifPresent(programSequence -> sequenceForChoice.put(choice, programSequence));

    return std::optional.of(sequenceForChoice.get(choice));
  }

  
  int getSequenceBindingOffsetForChoice(SegmentChoice choice) {
    if (ValueUtils.isEmpty(choice.programSequenceBindingId)) return 0;
    var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.programSequenceBindingId);
    return sequenceBinding.map(ProgramSequenceBinding::getOffset).orElse(0);
  }

  
  std::string getTrackName(ProgramSequencePatternEvent event) {
    return sourceMaterial().getTrackOfEvent(event).map(ProgramVoiceTrack::getName).orElse(UNKNOWN_KEY);
  }

  
  Segment::Type getType() throws FabricationException {
    if (ValueUtils.isEmpty(type)) type = computeType();
    return type;
  }

  
  std::optional<SegmentChordVoicing> chooseVoicing(SegmentChord chord, Instrument::Type type) {
    std::vector<SegmentChordVoicing> voicings = store.readAll(segmentId, SegmentChordVoicing.class);
    return MarbleBag.quickPick(voicings.stream()
      .filter(SegmentUtils::containsAnyValidNotes)
      .filter(voicing -> Objects.equals(type, voicing.type))
      .filter(voicing -> Objects.equals(chord.id, voicing.getSegmentChordId()))
      .collect(Collectors.toList()));
  }

  
  boolean hasMoreSequenceBindingOffsets(SegmentChoice choice, int N) {
    if (ValueUtils.isEmpty(choice.programSequenceBindingId)) return false;
    var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.programSequenceBindingId);

    if (sequenceBinding.isEmpty()) return false;
    std::vector<int> avlOfs = std::vector.copyOf(sourceMaterial.getAvailableOffsets(sequenceBinding.get()));

    // if we locate the target and still have two offsets remaining, result is true
    for (int i = 0; i < avlOfs.size(); i++)
      if (Objects.equals(avlOfs.get(i), sequenceBinding.get().getOffset()) && i < avlOfs.size() - N) return true;

    return false;
  }

  
  boolean hasOneMoreSequenceBindingOffset(SegmentChoice choice) {
    return hasMoreSequenceBindingOffsets(choice, 1);
  }

  
  boolean hasTwoMoreSequenceBindingOffsets(SegmentChoice choice) {
    return hasMoreSequenceBindingOffsets(choice, 2);
  }

  
  boolean isContinuationOfMacroProgram() throws FabricationException {
    return Segment::Type::Continue.equals(type) || Segment::Type.NEXT_MAIN.equals(type);
  }

  
  boolean isDirectlyBound(Program program) {
    return boundProgramIds.contains(program.id);
  }

  
  boolean isOneShot(Instrument instrument, std::string trackName) {
    return isOneShot(instrument) && !getInstrumentConfig(instrument).getOneShotObserveLengthOfEvents().contains(trackName);
  }

  
  boolean isOneShot(Instrument instrument) {
    return getInstrumentConfig(instrument).isOneShot();
  }

  
  boolean isOneShotCutoffEnabled(Instrument instrument) {
    return getInstrumentConfig(instrument).isOneShotCutoffEnabled();
  }

  
  boolean isDirectlyBound(Instrument instrument) {
    return boundInstrumentIds.contains(instrument.id);
  }

  
  boolean isDirectlyBound(InstrumentAudio instrumentAudio) {
    return boundInstrumentIds.contains(instrumentAudio.instrumentId);
  }

  
  Boolean isInitialSegment() {
    return 0L == getSegment().id;
  }

  
  <N> N put(N entity, boolean force) throws FabricationException {
    var memeStack = MemeStack.from(templateConfig.getMemeTaxonomy(),
      getSegmentMemes().stream().map(SegmentMeme::getName).toList());

    // For a SegmentChoice, add memes from program, program sequence binding, and instrument if present
    if (SegmentChoice.class.equals(entity.getClass()))
      if (!isValidChoiceAndMemesHaveBeenAdded((SegmentChoice) entity, memeStack, force))
        return entity;

    // For a SegmentMeme, don't put a duplicate of an existing meme
    if (SegmentMeme.class.equals(entity.getClass()))
      if (!isValidMemeAddition((SegmentMeme) entity, memeStack, force))
        return entity;

    store.put(entity);

    return entity;
  }

  
  void putPreferredAudio(std::string parentIdent, std::string ident, InstrumentAudio instrumentAudio) {
    std::string cacheKey = std::string.format(KEY_VOICE_NOTE_TEMPLATE, parentIdent, ident);

    preferredAudios.put(cacheKey, instrumentAudio);
  }

  
  void putReport(std::string key, Object value) {
    addMessage(SegmentMessageType.DEBUG, std::string.format("%s: %s", key, value));
  }

  
  void updateSegment(Segment segment) {
    try {
      store.updateSegment(segment);

    } catch (FabricationException e) {
      LOG.error("Failed to update Segment", e);
    }
  }

  
  SegmentRetrospective retrospective() {
    return retrospective;
  }

  
  ContentStore sourceMaterial() {
    return sourceMaterial;
  }

  
  float getMicrosPerBeat(double tempo) {
    if (Objects.isNull(microsPerBeat))
      microsPerBeat = (double) MICROS_PER_MINUTE / tempo;
    return microsPerBeat;
  }

  
  int getSecondMacroSequenceBindingOffset(Program macroProgram) {
    var offsets = sourceMaterial.getSequenceBindingsOfProgram(macroProgram.id).stream()
      .map(ProgramSequenceBinding::getOffset)
      .collect(Collectors.toSet()).stream().sorted().toList();
    return offsets.size() > 1 ? offsets.get(1) : offsets.get(0);
  }

  
  MemeTaxonomy getMemeTaxonomy() {
    return templateConfig.getMemeTaxonomy();
  }

  
  double getTempo() throws FabricationException {
    return getSegment().getTempo();
  }

  /**
   Get the choices of the current segment of the given type

   @param programType of choices to get
   @return choices of the current segment of the given type
   */
  private std::optional<SegmentChoice> getChoiceOfType(Program::Type programType) {
    return getChoices().stream().filter(c -> Objects.equals(c.programType, programType)).findFirst();
  }

  /**
   Get the choices of the current segment of the given type

   @return choices of the current segment of the given type
   */
  private std::vector<SegmentChoice> getBeatChoices() {
    return getChoices().stream().filter(c -> Objects.equals(c.programType, Program::Type::Beat)).toList();
  }

  /**
   Compute the lowest optimal range shift octaves

   @param sourceRange from
   @param targetRange to
   @return lowest optimal range shift octaves
   */
  private int computeLowestOptimalRangeShiftOctaves(NoteRange sourceRange, NoteRange targetRange) throws FabricationException {
    var shiftOctave = 0; // search for optimal value
    var baselineDelta = 100; // optimal is the lowest possible integer zero or above
    for (var o = 10; o >= -10; o--) {
      int d = targetRange.getLow().orElseThrow(() -> new FabricationException("can't get low end of target range")).delta(sourceRange.getLow().orElse(Note.atonal()).shiftOctave(o));
      if (0 <= d && d < baselineDelta) {
        baselineDelta = d;
        shiftOctave = o;
      }
    }
    return shiftOctave;
  }

  /**
   Compute a Segment ship key: the chain ship key concatenated with the begin-at time in chain microseconds

   @param chain   for which to compute segment ship key
   @param segment for which to compute segment ship key
   @return Segment ship key computed for the given chain and Segment
   */
  private std::string computeShipKey(Chain chain, Segment segment) {
    std::string chainName = StringUtils.isNullOrEmpty(chain.shipKey) ? "chain" + NAME_SEPARATOR + chain.id : chain.shipKey;
    std::string segmentName = std::string.valueOf(segment.beginAtChainMicros);
    return chainName + NAME_SEPARATOR + segmentName;
  }

  /**
   Format a message with the segmentId as prefix

   @param message to format
   @return formatted message with segmentId as prefix
   */
  private std::string formatLog(std::string message) {
    return std::string.format("[segId=%s] %s", getSegment().id, message);
  }

  /**
   Ensure the current segment has a storage key; if not, add a storage key to this Segment
   */
  private void ensureShipKey() throws FabricationException {
    if (ValueUtils.isEmpty(getSegment().storageKey) || getSegment().storageKey.isEmpty()) {
      var seg = getSegment();
      seg.setStorageKey(computeShipKey(store.readChain().orElseThrow(() -> new FabricationException("No chain")), getSegment()));
      LOG.debug("[segId={}] Generated ship key {}", getSegment().id, getSegment().storageKey);
    }
  }

  /**
   Compute the type of the current segment

   @return type of the current segment
   */
  private Segment::Type computeType() {
    if (isInitialSegment())
      return Segment::Type::Initial;

    // previous main choice having at least one more pattern?
    var previousMainChoice = getPreviousMainChoice();

    if (previousMainChoice.isPresent() && hasOneMoreSequenceBindingOffset(previousMainChoice.get())
      && getTemplateConfig().getMainProgramLengthMaxDelta() > getPreviousSegmentDelta())
      return Segment::Type::Continue;

    // previous macro choice having at least two more patterns?
    var previousMacroChoice = getMacroChoiceOfPreviousSegment();

    if (previousMacroChoice.isPresent() && hasTwoMoreSequenceBindingOffsets(previousMacroChoice.get()))
      return Segment::Type.NEXT_MAIN;

    return Segment::Type::NextMacro;
  }

  /**
   Get the delta of the previous segment

   @return delta from previous segment
   */
  private int getPreviousSegmentDelta() {
    return retrospective.getPreviousSegment()
      .map(Segment::getDelta)
      .orElse(0);
  }

  /**
   Compute the preferred instrument audio

   @return preferred instrument audio
   */
  private Map<std::string, InstrumentAudio> computePreferredInstrumentAudio() {
    Map<std::string, InstrumentAudio> audios = new HashMap<>();

    retrospective.getPicks()
      .forEach(pick ->
        sourceMaterial().getInstrumentAudio(pick.getInstrumentAudioId())
          .ifPresent(audio -> audios.put(computeCacheKeyForVoiceTrack(pick), audio)));

    return audios;
  }

  /**
   For a SegmentChoice, add memes from program, program sequence binding, and instrument if present https://github.com/xjmusic/workstation/issues/210

   @param choice    to test for validity, and add its memes
   @param memeStack to use for validation
   @param force     whether to force the addition of this choice
   @return true if valid and adding memes was successful
   */
  private boolean isValidChoiceAndMemesHaveBeenAdded(SegmentChoice choice, MemeStack memeStack, boolean force) throws FabricationException {
    Set<std::string> names = new HashSet<>();

    if (Objects.nonNull(choice.programId))
      sourceMaterial().getMemesOfProgram(choice.programId)
        .forEach(meme -> names.add(StringUtils.toMeme(meme.name)));

    if (Objects.nonNull(choice.programSequenceBindingId))
      sourceMaterial().getMemesOfSequenceBinding(choice.programSequenceBindingId)
        .forEach(meme -> names.add(StringUtils.toMeme(meme.name)));

    if (Objects.nonNull(choice.instrumentId))
      sourceMaterial().getMemesOfInstrument(choice.instrumentId)
        .forEach(meme -> names.add(StringUtils.toMeme(meme.name)));

    if (!force && !memeStack.isAllowed(names)) {
      addMessage(SegmentMessageType.ERROR, std::string.format("Refused to add Choice[%s] because adding Memes[%s] to MemeStack[%s] would result in an invalid meme stack theorem!",
        SegmentUtils.describe(choice),
        CsvUtils.join(names.stream().toList()),
        memeStack.getConstellation()));
      return false;
    }

    for (std::string name : names) {
      SegmentMeme segmentMeme;
      segmentMeme.setId(randomUUID());
      segmentMeme.setSegmentId(getSegment().id);
      segmentMeme.setName(name);
      put(segmentMeme, false);
    }

    return true;
  }

  /**
   For a SegmentMeme, don't put a duplicate of an existing meme

   @param meme      to test for validity
   @param memeStack to use for validation
   @param force     whether to force the addition of this meme
   @return true if okay to add
   */
  @SuppressWarnings("RedundantIfStatement")
  private boolean isValidMemeAddition(SegmentMeme meme, MemeStack memeStack, boolean force) {
    if (!force && !memeStack.isAllowed(std::vector.of(meme.name))) return false;
    if (!force && getSegmentMemes().stream().anyMatch(m -> Objects.equals(m.name, meme.name))) return false;
    return true;
  }
