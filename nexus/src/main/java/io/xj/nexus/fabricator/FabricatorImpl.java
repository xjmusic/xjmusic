// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.xj.api.*;
import io.xj.hub.InstrumentConfig;
import io.xj.hub.ProgramConfig;
import io.xj.hub.TemplateConfig;
import io.xj.hub.enums.ContentBindingType;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.*;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.music.*;
import io.xj.lib.util.*;
import io.xj.nexus.NexusException;
import io.xj.nexus.hub_client.client.HubClientException;
import io.xj.nexus.hub_client.client.HubContent;
import io.xj.nexus.persistence.*;
import org.glassfish.jersey.internal.guava.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static io.xj.lib.util.Values.NANOS_PER_SECOND;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
class FabricatorImpl implements Fabricator {
  private static final String KEY_VOICE_NOTE_TEMPLATE = "voice-%s_note-%s";
  private static final String KEY_VOICE_TRACK_TEMPLATE = "voice-%s_track-%s";
  private static final String NAME_SEPARATOR = "-";
  private static final String UNKNOWN_KEY = "unknown";
  private final Logger LOG = LoggerFactory.getLogger(FabricatorImpl.class);
  private final Chain chain;
  private final TemplateConfig templateConfig;
  private final Collection<TemplateBinding> templateBindings;
  private final FabricatorFactory fabricatorFactory;
  private final HubContent sourceMaterial;
  private final int workBufferAheadSeconds;
  private final int workBufferBeforeSeconds;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final long startTime;
  private final Map<Double, Optional<SegmentChord>> chordAtPosition;
  private final Map<InstrumentType, NoteRange> voicingNoteRange;
  private final Map<SegmentChoice, ProgramSequence> sequenceForChoice;
  private final Map<UUID, Collection<ProgramSequenceChord>> completeChordsForProgramSequence;
  private final Map<String, Integer> rangeShiftOctave;
  private final Map<String, Integer> targetShift;
  private final Map<String, NoteRange> rangeForChoice;
  private final Map<String, Set<String>> preferredNotes;
  private final SegmentManager segmentManager;
  private final SegmentRetrospective retrospective;
  private final SegmentWorkbench workbench;
  private final Set<UUID> boundInstrumentIds;
  private final Set<UUID> boundProgramIds;
  private final String workTempFilePathPrefix;
  private final Map<String, InstrumentAudio> preferredAudios;
  private final Map<String, InstrumentConfig> instrumentConfigs;
  private SegmentType type;
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private Optional<SegmentChoice> macroChoiceOfPreviousSegment;
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private Optional<SegmentChoice> mainChoiceOfPreviousSegment;

  @AssistedInject
  public FabricatorImpl(
    @Assisted("sourceMaterial") HubContent sourceMaterial,
    @Assisted("segment") Segment segment,
    Environment env,
    ChainManager chainManager,
    FabricatorFactory fabricatorFactory,
    SegmentManager segmentManager,
    JsonapiPayloadFactory jsonapiPayloadFactory
  ) throws NexusException {
    this.segmentManager = segmentManager;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    try {
      this.fabricatorFactory = fabricatorFactory;
      this.sourceMaterial = sourceMaterial;

      workTempFilePathPrefix = env.getTempFilePathPrefix();

      // caches
      chordAtPosition = Maps.newHashMap();
      completeChordsForProgramSequence = Maps.newHashMap();
      instrumentConfigs = Maps.newHashMap();
      rangeForChoice = Maps.newHashMap();
      rangeShiftOctave = Maps.newHashMap();
      sequenceForChoice = Maps.newHashMap();
      targetShift = Maps.newHashMap();
      voicingNoteRange = Maps.newHashMap();

      // time
      startTime = System.nanoTime();
      LOG.debug("[segId={}] StartTime {}ns since epoch zulu", segment.getId(), startTime);

      // read the chain, configs, and bindings
      chain = chainManager.readOne(segment.getChainId());
      templateConfig = new TemplateConfig(sourceMaterial.getTemplate());
      templateBindings = sourceMaterial.getAllTemplateBindings();
      boundProgramIds = Chains.targetIdsOfType(templateBindings, ContentBindingType.Program);
      boundInstrumentIds = Chains.targetIdsOfType(templateBindings, ContentBindingType.Instrument);
      LOG.debug("[segId={}] Chain {} configured with {} and bound to {} ", segment.getId(), chain.getId(),
        templateConfig,
        CSV.prettyFrom(templateBindings, "and"));

      // Buffer times from template
      workBufferAheadSeconds = templateConfig.getBufferAheadSeconds();
      workBufferBeforeSeconds = templateConfig.getBufferBeforeSeconds();

      // set up the segment retrospective
      retrospective = fabricatorFactory.loadRetrospective(segment, sourceMaterial);

      // digest previous picks
      preferredNotes = computePreferredNotes();

      // digest previous instrument audio
      preferredAudios = computePreferredInstrumentAudio();

      // get the current segment on the workbench
      workbench = fabricatorFactory.setupWorkbench(chain, segment);

      // final pre-flight check
      ensureShipKey();

    } catch (ManagerFatalException | ManagerExistenceException | ManagerPrivilegeException | ValueException | HubClientException e) {
      LOG.error("Failed to instantiate Fabricator!", e);
      throw new NexusException("Failed to instantiate Fabricator!", e);
    }
  }

  @Override
  public <N> N add(N entity) throws NexusException {
    return workbench.add(entity);
  }

  @Override
  public Instrument addMemes(Instrument p) throws NexusException {
    for (InstrumentMeme meme : sourceMaterial().getMemes(p)) {
      var segMeme = new SegmentMeme();
      segMeme.setId(UUID.randomUUID());
      segMeme.setSegmentId(getSegment().getId());
      segMeme.setName(Text.toMeme(meme.getName()));
      add(segMeme);
    }
    return p;
  }

  @Override
  public Program addMemes(Program p) throws NexusException {
    for (ProgramMeme meme : sourceMaterial().getMemes(p)) {
      var segMeme = new SegmentMeme();
      segMeme.setId(UUID.randomUUID());
      segMeme.setSegmentId(getSegment().getId());
      segMeme.setName(Text.toMeme(meme.getName()));
      add(segMeme);
    }
    return p;
  }

  @Override
  public ProgramSequenceBinding addMemes(ProgramSequenceBinding psb) throws NexusException {
    for (ProgramSequenceBindingMeme meme : sourceMaterial().getMemes(psb)) {
      var segMeme = new SegmentMeme();
      segMeme.setId(UUID.randomUUID());
      segMeme.setSegmentId(getSegment().getId());
      segMeme.setName(Text.toMeme(meme.getName()));
      add(segMeme);
    }
    return psb;
  }

  @Override
  public void addMessage(SegmentMessageType messageType, String body) throws NexusException {
    var msg = new SegmentMessage();
    msg.setId(UUID.randomUUID());
    msg.setSegmentId(getSegment().getId());
    msg.setType(messageType);
    msg.setBody(body);
    add(msg);
  }

  @Override
  public void addErrorMessage(String body) throws NexusException {
    addMessage(SegmentMessageType.ERROR, body);
  }

  @Override
  public void addWarningMessage(String body) throws NexusException {
    addMessage(SegmentMessageType.WARNING, body);
  }

  @Override
  public void addInfoMessage(String body) throws NexusException {
    addMessage(SegmentMessageType.INFO, body);
  }

  @Override
  public void done() throws NexusException {
    try {
      workbench.setSegment(workbench.getSegment().type(getType()));
      workbench.done();
    } catch (JsonapiException | ValueException e) {
      throw new NexusException("Could not complete Segment fabrication", e);
    }
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements() {
    return workbench.getSegmentChoiceArrangements();
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements(Collection<SegmentChoice> choices) {
    Collection<UUID> choiceIds = Entities.idsOf(choices);
    return getArrangements().stream()
      .filter(arrangement -> choiceIds.contains(arrangement.getSegmentChoiceId()))
      .collect(Collectors.toList());
  }

  @Override
  public double getAudioVolume(SegmentChoiceArrangementPick pick) {
    return sourceMaterial().getInstrumentAudio(pick.getInstrumentAudioId())
      .stream().map(InstrumentAudio::getVolume)
      .findAny()
      .orElse(1.0f);
  }

  @Override
  public Chain getChain() {
    return chain;
  }

  @Override
  public TemplateConfig getTemplateConfig() {
    return templateConfig;
  }

  @Override
  public String getChainFullJson() throws NexusException {
    try {
      return computeChainJson(segmentManager.readMany(ImmutableList.of(chain.getId())));

    } catch (ManagerPrivilegeException | ManagerFatalException | ManagerExistenceException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public String getChainFullJsonOutputKey() {
    return Chains.getShipKey(Chains.getFullKey(computeChainBaseKey()), FileStoreProvider.EXTENSION_JSON);
  }

  @Override
  public String getChainJson() throws NexusException {
    try {
      var now = Instant.now();
      var beforeThreshold = now.plusSeconds(workBufferAheadSeconds);
      var afterThreshold = now.minusSeconds(workBufferBeforeSeconds);
      return computeChainJson(segmentManager.readMany(ImmutableList.of(chain.getId())).stream()
        .filter(segment ->
          Instant.parse(segment.getBeginAt()).isBefore(beforeThreshold)
            && Instant.parse(segment.getEndAt()).isAfter(afterThreshold))
        .collect(Collectors.toList()));

    } catch (ManagerPrivilegeException | ManagerFatalException | ManagerExistenceException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public String getChainJsonOutputKey() {
    return Chains.getShipKey(computeChainBaseKey(), FileStoreProvider.EXTENSION_JSON);
  }

  @Override
  public Collection<SegmentChoice> getChoices() {
    return workbench.getSegmentChoices();
  }

  @Override
  public Optional<SegmentChord> getChordAt(double position) {
    if (!chordAtPosition.containsKey(position)) {
      Optional<SegmentChord> foundChord = Optional.empty();
      Double foundPosition = null;

      // we assume that these entities are in order of position ascending
      for (SegmentChord segmentChord : workbench.getSegmentChords()) {
        // if it's a better match (or no match has yet been found) then use it
        if (Objects.isNull(foundPosition) ||
          (segmentChord.getPosition() > foundPosition && segmentChord.getPosition() <= position)) {
          foundPosition = segmentChord.getPosition();
          foundChord = Optional.of(segmentChord);
        }
      }
      chordAtPosition.put(position, foundChord);
    }

    return chordAtPosition.get(position);
  }

  @Override
  public Optional<SegmentChoice> getCurrentMainChoice() {
    return workbench.getChoiceOfType(ProgramType.Main);
  }

  @Override
  public Collection<SegmentChoice> getCurrentDetailChoices() {
    return workbench.getChoicesOfType(ProgramType.Beat);
  }

  @Override
  public Optional<SegmentChoice> getCurrentBeatChoice() {
    return workbench.getChoiceOfType(ProgramType.Beat);
  }

  @Override
  public Set<InstrumentType> getDistinctChordVoicingTypes() {
    var mainChoice = getCurrentMainChoice();
    if (mainChoice.isEmpty()) return Set.of();
    var voicings = sourceMaterial
      .getValidProgramSequenceChordVoicings(mainChoice.get().getProgramId());
    return voicings.stream()
      .map(ProgramSequenceChordVoicing::getType)
      .distinct()
      .collect(Collectors.toSet());
  }

  @Override
  public Double getElapsedSeconds() {
    return (System.nanoTime() - startTime) / NANOS_PER_SECOND;
  }

  @Override
  public String getFullQualityAudioOutputFilePath() {
    return String.format("%s%s", workTempFilePathPrefix, getSegmentOutputWaveformKey());
  }

  @Override
  public InstrumentConfig getInstrumentConfig(Instrument instrument) throws NexusException {
    try {
      if (!instrumentConfigs.containsKey(instrument.getId().toString()))
        instrumentConfigs.put(instrument.getId().toString(), new InstrumentConfig(instrument));
      return instrumentConfigs.get(instrument.getId().toString());

    } catch (ValueException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public Optional<SegmentChoice> getChoiceIfContinued(ProgramVoice voice) {
    try {
      if (!Objects.equals(SegmentType.CONTINUE, getSegment().getType())) return Optional.empty();
      return retrospective.getChoices()
        .stream()
        .filter(choice -> {
          var candidateVoice = sourceMaterial.getProgramVoice(choice.getProgramVoiceId());
          return candidateVoice.isPresent()
            && Objects.equals(candidateVoice.get().getName(), voice.getName())
            && Objects.equals(candidateVoice.get().getType(), voice.getType());
        })
        .findFirst();

    } catch (Exception e) {
      LOG.warn(formatLog(String.format("Could not get previous voice instrumentId for voiceName=%s", voice.getName())), e);
      return Optional.empty();
    }
  }

  @Override
  public Optional<SegmentChoice> getChoiceIfContinued(InstrumentType instrumentType) {
    try {
      return switch (getSegment().getType()) {
        case INITIAL, NEXTMAIN, NEXTMACRO, PENDING -> Optional.empty();
        case CONTINUE -> retrospective.getChoices()
          .stream()
          .filter(choice -> Objects.equals(instrumentType.toString(), choice.getInstrumentType()))
          .findFirst();
      };

    } catch (Exception e) {
      LOG.warn(formatLog(String.format("Could not get previous choice for instrumentType=%s", instrumentType)), e);
      return Optional.empty();
    }
  }

  @Override
  public Optional<SegmentChoice> getChoiceIfContinued(ProgramType programType) {
    try {
      return switch (getSegment().getType()) {
        case PENDING, INITIAL, NEXTMAIN, NEXTMACRO -> Optional.empty();
        case CONTINUE -> retrospective.getChoices()
          .stream()
          .filter(choice -> Objects.equals(programType.toString(), choice.getProgramType()))
          .findFirst();
      };

    } catch (Exception e) {
      LOG.warn(formatLog(String.format("Could not get previous choice for programType=%s", programType)), e);
      return Optional.empty();
    }
  }

  @Override
  public String getKeyByVoiceTrack(SegmentChoiceArrangementPick pick) {
    String key =
      sourceMaterial().getProgramSequencePatternEvent(pick.getProgramSequencePatternEventId())
        .flatMap(event -> sourceMaterial().getTrack(event)
          .map(ProgramVoiceTrack::getProgramVoiceId))
        .map(UUID::toString)
        .orElse(UNKNOWN_KEY);

    return String.format(KEY_VOICE_TRACK_TEMPLATE, key, pick.getEvent());
  }

  @Override
  public Key getKeyForChoice(SegmentChoice choice) throws NexusException {
    Optional<Program> program = getProgram(choice);
    if (Values.isSet(choice.getProgramSequenceBindingId())) {
      var sequence = getSequence(choice);
      if (sequence.isPresent() && !Strings.isNullOrEmpty(sequence.get().getKey()))
        return Key.of(sequence.get().getKey());
    }

    return Key.of(program
      .orElseThrow(() -> new NexusException("Cannot get key for nonexistent choice!"))
      .getKey());
  }

  @Override
  public Optional<SegmentChoice> getMacroChoiceOfPreviousSegment() {
    if (Objects.isNull(macroChoiceOfPreviousSegment))
      macroChoiceOfPreviousSegment = retrospective.getPreviousChoiceOfType(ProgramType.Macro);
    return macroChoiceOfPreviousSegment;
  }

  @Override
  public Optional<SegmentChoice> getMainChoiceOfPreviousSegment() {
    if (Objects.isNull(mainChoiceOfPreviousSegment))
      mainChoiceOfPreviousSegment = retrospective.getPreviousChoiceOfType(ProgramType.Main);
    return mainChoiceOfPreviousSegment;
  }

  @Override
  public MemeIsometry getMemeIsometryOfNextSequenceInPreviousMacro() {
    try {
      var previousMacroChoice =
        getMacroChoiceOfPreviousSegment()
          .orElseThrow(NexusException::new);
      var previousSequenceBinding =
        sourceMaterial()
          .getProgramSequenceBinding(previousMacroChoice.getProgramSequenceBindingId())
          .orElseThrow(NexusException::new);

      var nextSequenceBinding =
        sourceMaterial().getSequenceBindingsAtProgramOffset(
          previousMacroChoice.getProgramId(),
          previousSequenceBinding.getOffset() + 1);

      return MemeIsometry.ofMemes(Streams.concat(
        sourceMaterial.getProgramMemes(previousMacroChoice.getProgramId())
          .stream()
          .map(ProgramMeme::getName),
        nextSequenceBinding.stream()
          .flatMap(programSequenceBinding -> sourceMaterial.getMemes(programSequenceBinding)
            .stream()
            .map(ProgramSequenceBindingMeme::getName))
      ).collect(Collectors.toList()));

    } catch (NexusException e) {
      LOG.warn("Could not get meme isometry of next sequence in previous macro", e);
      return MemeIsometry.none();
    }
  }

  @Override
  public MemeIsometry getMemeIsometryOfSegment() {
    return MemeIsometry.ofMemes(Entities.namesOf(workbench.getSegmentMemes()));
  }

  @Override
  public Integer getNextSequenceBindingOffset(SegmentChoice choice) {
    if (Values.isEmpty(choice.getProgramSequenceBindingId()))
      return 0;

    var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());
    Integer sequenceBindingOffset = getSequenceBindingOffsetForChoice(choice);
    Integer offset = null;
    if (sequenceBinding.isEmpty()) return 0;
    for (Integer availableOffset : sourceMaterial.getAvailableOffsets(sequenceBinding.get()))
      if (0 < availableOffset.compareTo(sequenceBindingOffset))
        if (Objects.isNull(offset) ||
          0 > availableOffset.compareTo(offset))
          offset = availableOffset;

    // if none found, loop back around to zero
    return Objects.nonNull(offset) ? offset : 0;

  }

  @Override
  public Collection<String> getNotes(SegmentChordVoicing voicing) {
    return new ArrayList<>(CSV.split(voicing.getNotes()));
  }

  @Override
  public AudioFormat getOutputAudioFormat() {
    return new AudioFormat(
      templateConfig.getOutputEncoding(),
      templateConfig.getOutputFrameRate(),
      templateConfig.getOutputSampleBits(),
      templateConfig.getOutputChannels(),
      templateConfig.getOutputChannels() * templateConfig.getOutputSampleBits() / 8,
      templateConfig.getOutputFrameRate(),
      false);
  }

  @Override
  public Collection<InstrumentAudio> getPickedAudios() {
    Collection<InstrumentAudio> audios = Lists.newArrayList();
    for (SegmentChoiceArrangementPick pick : workbench.getSegmentChoiceArrangementPicks())
      sourceMaterial.getInstrumentAudio(pick.getInstrumentAudioId())
        .ifPresent(audios::add);
    return audios;
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPicks() {
    return workbench.getSegmentChoiceArrangementPicks();
  }

  @Override
  public Optional<InstrumentAudio> getPreferredAudio(ProgramSequencePatternEvent event, String note) {
    String key = String.format(KEY_VOICE_NOTE_TEMPLATE, event.getProgramVoiceTrackId(), note);

    if (preferredAudios.containsKey(key)) return Optional.of(preferredAudios.get(key));

    return Optional.empty();
  }

  @Override
  public Optional<Set<String>> getPreferredNotes(UUID eventId, String chordName) {
    try {
      var key = computeEventChordKey(eventId, chordName);
      if (preferredNotes.containsKey(key))
        return Optional.of(new HashSet<>(preferredNotes.get(key)));

    } catch (NexusException | EntityStoreException e) {
      LOG.warn("Can't find chord of previous event and chord id", e);
    }

    return Optional.empty();
  }

  @Override
  public Optional<Program> getProgram(SegmentChoice choice) {
    return sourceMaterial.getProgram(choice.getProgramId());
  }

  @Override
  public ProgramConfig getProgramConfig(Program program) throws NexusException {
    try {
      return new ProgramConfig(program);
    } catch (ValueException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public Collection<ProgramSequenceChord> getProgramSequenceChords(ProgramSequence programSequence) {
    if (!completeChordsForProgramSequence.containsKey(programSequence.getId())) {
      Map<Double, ProgramSequenceChord> chordForPosition = Maps.newHashMap();
      Map<Double, Integer> validVoicingsForPosition = Maps.newHashMap();
      for (ProgramSequenceChord chord : sourceMaterial.getChords(programSequence)) {
        int validVoicings = sourceMaterial.getVoicings(chord)
          .stream().map(V -> CSV.split(V.getNotes()).size()).reduce(0, Integer::sum);
        if (!validVoicingsForPosition.containsKey(chord.getPosition()) ||
          validVoicingsForPosition.get(chord.getPosition()) < validVoicings) {
          validVoicingsForPosition.put(chord.getPosition(), validVoicings);
          chordForPosition.put(chord.getPosition(), chord);
        }
      }
      completeChordsForProgramSequence.put(programSequence.getId(), chordForPosition.values());
    }

    return completeChordsForProgramSequence.get(programSequence.getId());
  }

  @Override
  public NoteRange getProgramRange(UUID programId, InstrumentType instrumentType) {
    var key = String.format("%s__%s", programId, instrumentType);

    if (!rangeForChoice.containsKey(key)) {
      rangeForChoice.put(key,
        new NoteRange(
          sourceMaterial.getEvents(programId)
            .stream()
            .filter(event ->
              sourceMaterial.getVoice(event)
                .map(voice -> Objects.equals(voice.getType(), instrumentType))
                .orElse(false) &&
                !Objects.equals(Note.of(event.getNote()).getPitchClass(), PitchClass.None))
            .flatMap(programSequencePatternEvent ->
              CSV.split(programSequencePatternEvent.getNote())
                .stream())
            .collect(Collectors.toList())
        ));
    }

    return rangeForChoice.get(key);
  }

  @Override
  public int getProgramRangeShiftOctaves(InstrumentType type, NoteRange sourceRange, NoteRange targetRange) throws NexusException {
    var key = String.format("%s__%s__%s", type,
      sourceRange.toString(AdjSymbol.None), targetRange.toString(AdjSymbol.None));

    if (!rangeShiftOctave.containsKey(key))
      switch (type) {

        case Bass:
          rangeShiftOctave.put(key, computeLowestOptimalRangeShiftOctaves(sourceRange, targetRange));
          break;

        case Drum:
          return 0;

        case Pad:
        case Stab:
        case Sticky:
        case Stripe:
        default:
          rangeShiftOctave.put(key, computeMedianOptimalRangeShiftOctaves(sourceRange, targetRange));
          break;
      }

    return rangeShiftOctave.get(key);
  }

  @Override
  public int getProgramTargetShift(Key fromKey, Chord toChord) {
    if (!fromKey.isPresent()) return 0;
    var key = String.format("%s__%s", fromKey, toChord.toString());
    if (!targetShift.containsKey(key))
      targetShift.put(key, fromKey.getRoot().delta(toChord.getSlashRoot()));

    return targetShift.get(key);
  }

  @Override
  public ProgramType getProgramType(ProgramVoice voice) throws NexusException {
    return sourceMaterial.getProgram(voice.getProgramId())
      .orElseThrow(() -> new NexusException("Could not get program!"))
      .getType();
  }

  @Override
  public NoteRange getProgramVoicingNoteRange(InstrumentType type) {
    if (!voicingNoteRange.containsKey(type)) {
      voicingNoteRange.put(type, new NoteRange(workbench.getSegmentChordVoicings()
        .stream()
        .filter(Segments::containsAnyValidNotes)
        .filter(segmentChordVoicing -> Objects.equals(segmentChordVoicing.getType(), type.toString()))
        .flatMap(segmentChordVoicing -> getNotes(segmentChordVoicing).stream())
        .collect(Collectors.toList())));
    }

    return voicingNoteRange.get(type);
  }

  @Override
  public Optional<ProgramSequence> getRandomlySelectedSequence(Program program) {
    var bag = MarbleBag.empty();
    sourceMaterial.getAllProgramSequences().stream()
      .filter(s -> Objects.equals(s.getProgramId(), program.getId()))
      .forEach(sequence -> bag.add(1, sequence.getId()));
    if (bag.isEmpty()) return Optional.empty();
    return sourceMaterial.getProgramSequence(bag.pick());
  }

  @Override
  public Optional<ProgramSequenceBinding> getRandomlySelectedSequenceBindingAtOffset(Program program, Integer offset) {
    var bag = MarbleBag.empty();
    for (ProgramSequenceBinding sequenceBinding : sourceMaterial.getProgramSequenceBindingsAtOffset(program, offset))
      bag.add(1, sequenceBinding.getId());
    if (bag.isEmpty()) return Optional.empty();
    return sourceMaterial.getProgramSequenceBinding(bag.pick());
  }

  @Override
  public Optional<ProgramVoice> getRandomlySelectedVoiceForProgramId(UUID programId, Collection<UUID> excludeVoiceIds) {
    var bag = MarbleBag.empty();
    for (ProgramVoice sequenceBinding : sourceMaterial.getAllProgramVoices()
      .stream().filter(programVoice -> Objects.equals(programId, programVoice.getProgramId())
        && !excludeVoiceIds.contains(programVoice.getId())).toList())
      bag.add(1, sequenceBinding.getId());
    if (bag.isEmpty()) return Optional.empty();
    return sourceMaterial.getProgramVoice(bag.pick());
  }

  @Override
  public Optional<ProgramSequencePattern> getRandomlySelectedPatternOfSequenceByVoiceAndType(SegmentChoice choice) {
    var bag = MarbleBag.empty();
    sourceMaterial.getAllProgramSequencePatterns().stream()
      .filter(pattern -> Objects.equals(pattern.getProgramSequenceId(), choice.getProgramSequenceId()))
      .filter(pattern -> Objects.equals(pattern.getProgramVoiceId(), choice.getProgramVoiceId()))
      .forEach(pattern -> bag.add(1, pattern.getId()));
    if (bag.isEmpty()) return Optional.empty();
    return sourceMaterial.getProgramSequencePattern(bag.pick());
  }

  @Override
  public Double getSecondsAtPosition(double p) throws NexusException {
    return buildTimeComputer().getSecondsAtPosition(p);
  }

  @Override
  public double getTotalSeconds() throws NexusException {
    return getSecondsAtPosition(getSegment().getTotal());
  }

  @Override
  public Segment getSegment() {
    return workbench.getSegment();
  }

  @Override
  public Collection<SegmentChord> getSegmentChords() {
    return workbench.getSegmentChords();
  }

  @Override
  public String getSegmentJson() throws NexusException {
    try {
      return jsonapiPayloadFactory.serialize(jsonapiPayloadFactory.newJsonapiPayload()
        .setDataOne(jsonapiPayloadFactory.toPayloadObject(workbench.getSegment()))
        .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentChoiceArrangementPicks()))
        .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentChoiceArrangements()))
        .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentChoices()))
        .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentChords()))
        .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentMemes()))
        .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentMessages())));

    } catch (JsonapiException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public String getSegmentJsonOutputKey() {
    return getSegmentShipKey(FileStoreProvider.EXTENSION_JSON);
  }

  @Override
  public String getSegmentOutputWaveformKey() {
    return Segments.getStorageFilename(getSegment());
  }

  @Override
  public String getSegmentShipKey(String extension) {
    return Segments.getStorageFilename(getSegment().getStorageKey(), extension);
  }

  @Override
  public Optional<ProgramSequence> getSequence(SegmentChoice choice) {
    Optional<Program> program = getProgram(choice);
    if (program.isEmpty()) return Optional.empty();
    if (Values.isSet(choice.getProgramSequenceBindingId())) {
      var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());
      if (sequenceBinding.isPresent())
        return sourceMaterial.getProgramSequence(sequenceBinding.get().getProgramSequenceId());
    }

    if (!sequenceForChoice.containsKey(choice))
      getRandomlySelectedSequence(program.get())
        .ifPresent(programSequence -> sequenceForChoice.put(choice, programSequence));

    return Optional.of(sequenceForChoice.get(choice));
  }

  @Override
  public Integer getSequenceBindingOffsetForChoice(SegmentChoice choice) {
    if (Values.isEmpty(choice.getProgramSequenceBindingId()))
      return 0;
    var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());
    return sequenceBinding.map(ProgramSequenceBinding::getOffset).orElse(0);
  }

  @Override
  public String getTrackName(ProgramSequencePatternEvent event) {
    return sourceMaterial().getTrack(event)
      .map(ProgramVoiceTrack::getName)
      .orElse(UNKNOWN_KEY);
  }

  @Override
  public SegmentType getType() throws NexusException {
    if (Values.isEmpty(type))
      type = computeType();
    return type;
  }

  @Override
  public Optional<SegmentChordVoicing> getVoicing(SegmentChord chord, InstrumentType type) {
    Collection<SegmentChordVoicing> voicings = workbench.getSegmentChordVoicings();
    return voicings.stream()
      .filter(Segments::containsAnyValidNotes)
      .filter(voicing -> Objects.equals(type.toString(), voicing.getType()))
      .filter(voicing -> Objects.equals(chord.getId(), voicing.getSegmentChordId()))
      .findAny();
  }

  @Override
  public boolean hasMoreSequenceBindingOffsets(SegmentChoice choice, int N) {
    if (Values.isEmpty(choice.getProgramSequenceBindingId()))
      return false;
    var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());

    if (sequenceBinding.isEmpty())
      return false;
    List<Integer> avlOfs = ImmutableList.copyOf(sourceMaterial.getAvailableOffsets(sequenceBinding.get()));

    // if we locate the target and still have two offsets remaining, result is true
    for (int i = 0; i < avlOfs.size(); i++)
      if (Objects.equals(avlOfs.get(i), sequenceBinding.get().getOffset()) && i < avlOfs.size() - N)
        return true;

    return false;
  }

  @Override
  public boolean hasOneMoreSequenceBindingOffset(SegmentChoice choice) {
    return hasMoreSequenceBindingOffsets(choice, 1);
  }

  @Override
  public boolean hasTwoMoreSequenceBindingOffsets(SegmentChoice choice) {
    return hasMoreSequenceBindingOffsets(choice, 2);
  }

  @Override
  public boolean isContinuationOfMacroProgram() throws NexusException {
    return
      SegmentType.CONTINUE.equals(getType()) ||
        SegmentType.NEXTMAIN.equals(getType());
  }

  @Override
  public boolean isDirectlyBound(Program program) {
    return boundProgramIds.contains(program.getId());
  }

  @Override
  public boolean isDirectlyBound(Instrument instrument) {
    return boundInstrumentIds.contains(instrument.getId());
  }

  @Override
  public Boolean isInitialSegment() {
    return 0L == workbench.getSegment().getOffset();
  }

  @Override
  public Set<String> rememberPickedNotes(UUID programSequencePatternEventId, String chordName, Set<String> notes) {
    try {
      preferredNotes.put(computeEventChordKey(programSequencePatternEventId, chordName),
        new HashSet<>(notes));
    } catch (NexusException | EntityStoreException e) {
      LOG.warn("Can't find chord of previous event and chord id", e);
    }

    return notes;
  }

  @Override
  public void putReport(String key, Object value) {
    workbench.putReport(key, value);
  }

  @Override
  public void updateSegment(Segment segment) {
    try {
      segmentManager.update(segment.getId(), segment);
      workbench.setSegment(segment);

    } catch (ManagerFatalException | ManagerExistenceException | ManagerPrivilegeException | ManagerValidationException e) {
      LOG.error("Failed to update Segment", e);
    }
  }

  @Override
  public SegmentRetrospective retrospective() {
    return retrospective;
  }

  @Override
  public void setPreferredAudio(ProgramSequencePatternEvent event, String note, InstrumentAudio instrumentAudio) {
    String key = String.format(KEY_VOICE_NOTE_TEMPLATE, event.getProgramVoiceTrackId(), note);

    preferredAudios.put(key, instrumentAudio);
  }

  @Override
  public HubContent sourceMaterial() {
    return sourceMaterial;
  }

  @Override
  public ProgramConfig getMainProgramConfig() throws NexusException {
    try {
      return new ProgramConfig(sourceMaterial
        .getProgram(getCurrentMainChoice().orElseThrow(() -> new NexusException("No current main choice!")).getProgramId())
        .orElseThrow(() -> new NexusException("Failed to retrieve current main program!")));

    } catch (ValueException e) {
      throw new NexusException(e);
    }
  }

  /**
   @return Chain base key
   */
  private String computeChainBaseKey() {
    return
      Strings.isNullOrEmpty(getChain().getShipKey()) ?
        String.format("chain-%s", chain.getId())
        : getChain().getShipKey();
  }

  /**
   Compute the lowest optimal range shift octaves

   @param sourceRange from
   @param targetRange to
   @return lowest optimal range shift octaves
   */
  private Integer computeLowestOptimalRangeShiftOctaves(NoteRange sourceRange, NoteRange targetRange) throws NexusException {
    var shiftOctave = 0; // search for optimal value
    var baselineDelta = 100; // optimal is the lowest possible integer zero or above
    for (var o = 10; o >= -10; o--) {
      int d = targetRange.getLow().orElseThrow(() -> new NexusException("can't get low end of target range"))
        .delta(sourceRange.getLow()
          .orElse(Note.atonal())
          .shiftOctave(o));
      if (0 <= d && d < baselineDelta) {
        baselineDelta = d;
        shiftOctave = o;
      }
    }
    return shiftOctave;
  }

  /**
   Compute the median optimal range shift octaves

   @param sourceRange from
   @param targetRange to
   @return median optimal range shift octaves
   */
  private Integer computeMedianOptimalRangeShiftOctaves(NoteRange sourceRange, NoteRange targetRange) throws NexusException {
    if (sourceRange.getLow().isEmpty() ||
      sourceRange.getHigh().isEmpty() ||
      targetRange.getLow().isEmpty() ||
      targetRange.getHigh().isEmpty())
      return 0;
    var shiftOctave = 0; // search for optimal value
    var baselineDelta = 100; // optimal is the lowest possible integer zero or above
    for (var o = 10; o >= -10; o--) {
      int dLow = targetRange.getLow().orElseThrow(() -> new NexusException("Can't find low end of target range"))
        .delta(sourceRange.getLow().orElseThrow(() -> new NexusException("Can't find low end of source range"))
          .shiftOctave(o));
      int dHigh = targetRange.getHigh().orElseThrow(() -> new NexusException("Can't find high end of target range"))
        .delta(sourceRange.getHigh().orElseThrow(() -> new NexusException("Can't find high end of source range"))
          .shiftOctave(o));
      if (0 <= dLow && 0 >= dHigh && Math.abs(o) < baselineDelta) {
        baselineDelta = Math.abs(o);
        shiftOctave = o;
      }
    }
    return shiftOctave;
  }

  /**
   Compute a Segment ship key: the chain ship key concatenated with the begin-at time in microseconds since epoch

   @param chain   for which to compute segment ship key
   @param segment for which to compute segment ship key
   @return Segment ship key computed for the given chain and Segment
   */
  private String computeShipKey(Chain chain, Segment segment) {
    String chainName = Strings.isNullOrEmpty(chain.getShipKey()) ?
      "chain" + NAME_SEPARATOR + chain.getId() :
      chain.getShipKey();
    String segmentName = String.valueOf(Values.toEpochMicros(Instant.parse(segment.getBeginAt())));
    return chainName + NAME_SEPARATOR + segmentName;
  }

  /**
   Format a message with the segmentId as prefix

   @param message to format
   @return formatted message with segmentId as prefix
   */
  private String formatLog(String message) {
    return String.format("[segId=%s] %s", workbench.getSegment().getId(), message);
  }

  /**
   Get the Chain Metadata JSON file from a set of segments

   @param segments to include in metadata JSON
   @return metadata JSON
   @throws NexusException on failure
   */
  private String computeChainJson(Collection<Segment> segments) throws NexusException {
    try {
      JsonapiPayload jsonapiPayload = new JsonapiPayload();
      jsonapiPayload.setDataOne(jsonapiPayloadFactory.toPayloadObject(chain));
      for (TemplateBinding binding : templateBindings)
        jsonapiPayload.addToIncluded(jsonapiPayloadFactory.toPayloadObject(binding));
      for (Segment segment : segments)
        jsonapiPayload.addToIncluded(jsonapiPayloadFactory.toPayloadObject(segment));
      return jsonapiPayloadFactory.serialize(jsonapiPayload);

    } catch (JsonapiException e) {
      throw new NexusException(e);
    }
  }

  /**
   Key for a chord + event pairing

   @param eventId   to get key for
   @param chordName to get key for
   @return key for chord + event
   */
  private String computeEventChordKey(@Nullable UUID eventId, @Nullable String chordName) throws NexusException, EntityStoreException {
    return String.format("%s__%s",
      Strings.isNullOrEmpty(chordName) ? UNKNOWN_KEY : chordName,
      Objects.isNull(eventId) ? UNKNOWN_KEY : eventId.toString());
  }

  /**
   Get a time computer, configured for the current segment.
   Don't use it before this segment has enough choices to determine its time computer

   @return Time Computer
   */
  private TimeComputer buildTimeComputer() throws NexusException {
    double toTempo = workbench.getSegment().getTempo(); // velocity at current segment tempo
    double fromTempo = retrospective.getPreviousSegment().isPresent() ? retrospective.getPreviousSegment().get().getTempo() : toTempo;
    double totalBeats = workbench.getSegment().getTotal();
    putReport("totalBeats", totalBeats);
    putReport("fromTempo", fromTempo);
    putReport("toTempo", toTempo);
    if (0 == totalBeats) throw new NexusException("Can't instantiate time computer with zero total beats!");
    if (0 == fromTempo) throw new NexusException("Can't instantiate time computer from zero tempo!");
    if (0 == toTempo) throw new NexusException("Can't instantiate time computer to zero tempo!");
    return fabricatorFactory.createTimeComputer(totalBeats, fromTempo, toTempo);
  }

  /**
   Ensure the current segment has a storage key; if not, add a storage key to this Segment
   */
  private void ensureShipKey() {
    if (Values.isEmpty(workbench.getSegment().getStorageKey()) || workbench.getSegment().getStorageKey().isEmpty()) {
      var seg = workbench.getSegment();
      seg.setStorageKey(computeShipKey(workbench.getChain(), workbench.getSegment()));
      workbench.setSegment(seg);
      LOG.debug("[segId={}] Generated ship key {}", workbench.getSegment().getId(), workbench.getSegment().getStorageKey());
    }
  }

  /**
   Compute the type of the current segment

   @return type of the current segment
   */
  private SegmentType computeType() {
    if (isInitialSegment())
      return SegmentType.INITIAL;

    // previous main choice having at least one more pattern?
    var previousMainChoice = getMainChoiceOfPreviousSegment();

    if (previousMainChoice.isPresent() && hasOneMoreSequenceBindingOffset(previousMainChoice.get())
      && getTemplateConfig().getMainProgramLengthMaxDelta() > getPreviousSegmentDelta())
      return SegmentType.CONTINUE;

    // previous macro choice having at least two more patterns?
    var previousMacroChoice = getMacroChoiceOfPreviousSegment();

    if (previousMacroChoice.isPresent() && hasTwoMoreSequenceBindingOffsets(previousMacroChoice.get()))
      return SegmentType.NEXTMAIN;

    return SegmentType.NEXTMACRO;
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
   Digest all previously picked events for the same main program

   @return map of program types to instrument types to list of programs chosen
   */
  private Map<String, Set<String>> computePreferredNotes() {
    Map<String, Set<String>> notes = Maps.newHashMap();

    retrospective.getPicks()
      .forEach(pick -> {
        try {
          var key = computeEventChordKey(pick.getProgramSequencePatternEventId(), Objects.nonNull(pick.getSegmentChordVoicingId()) ? pick.getSegmentChordVoicingId().toString() : UNKNOWN_KEY);
          if (!notes.containsKey(key))
            notes.put(key, Sets.newHashSet());
          notes.get(key).add(pick.getNote());

        } catch (NexusException | EntityStoreException e) {
          LOG.warn("Can't find chord of previous event and chord id", e);
        }
      });

    return notes;
  }

  /**
   Compute the preferred instrument audio

   @return preferred instrument audio
   */
  private Map<String, InstrumentAudio> computePreferredInstrumentAudio() {
    Map<String, InstrumentAudio> audios = Maps.newHashMap();

    retrospective.getPicks()
      .forEach(pick ->
        sourceMaterial().getInstrumentAudio(pick.getInstrumentAudioId())
          .ifPresent(audio -> audios.put(getKeyByVoiceTrack(pick), audio)));

    return audios;
  }
}
