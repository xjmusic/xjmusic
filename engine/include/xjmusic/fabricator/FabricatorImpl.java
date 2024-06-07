// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.fabricator;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.xj.model.HubContent;
import io.xj.model.InstrumentConfig;
import io.xj.model.ProgramConfig;
import io.xj.model.TemplateConfig;
import io.xj.model.entity.EntityUtils;
import io.xj.model.enums.TemplateBinding::Type;
import io.xj.model.enums.InstrumentMode;
import io.xj.model.enums.Instrument::Type;
import io.xj.model.enums.Program::Type;
import io.xj.model.json.JsonProvider;
import io.xj.model.jsonapi.JsonapiPayloadFactory;
import io.xj.model.meme.MemeStack;
import io.xj.model.meme.MemeTaxonomy;
import io.xj.model.music.Accidental;
import io.xj.model.music.Chord;
import io.xj.model.music.Note;
import io.xj.model.music.NoteRange;
import io.xj.model.music.PitchClass;
import io.xj.model.music.StickyBun;
import io.xj.model.pojos.Instrument;
import io.xj.model.pojos.InstrumentAudio;
import io.xj.model.pojos.Program;
import io.xj.model.pojos.ProgramMeme;
import io.xj.model.pojos.ProgramSequence;
import io.xj.model.pojos.ProgramSequenceBinding;
import io.xj.model.pojos.ProgramSequenceBindingMeme;
import io.xj.model.pojos.ProgramSequenceChord;
import io.xj.model.pojos.ProgramSequenceChordVoicing;
import io.xj.model.pojos.ProgramSequencePattern;
import io.xj.model.pojos.ProgramSequencePatternEvent;
import io.xj.model.pojos.ProgramVoice;
import io.xj.model.pojos.ProgramVoiceTrack;
import io.xj.model.pojos.TemplateBinding;
import io.xj.model.util.CsvUtils;
import io.xj.model.util.StringUtils;
import io.xj.model.util.ValueException;
import io.xj.model.util.ValueUtils;
import io.xj.engine.FabricationException;
import io.xj.model.pojos.Chain;
import io.xj.model.pojos.Segment;
import io.xj.model.pojos.SegmentChoice;
import io.xj.model.pojos.SegmentChoiceArrangement;
import io.xj.model.pojos.SegmentChoiceArrangementPick;
import io.xj.model.pojos.SegmentChord;
import io.xj.model.pojos.SegmentChordVoicing;
import io.xj.model.pojos.SegmentMeme;
import io.xj.model.pojos.SegmentMessage;
import io.xj.model.enums.SegmentMessageType;
import io.xj.model.pojos.SegmentMeta;
import io.xj.model.enums.Segment::Type;
import io.xj.engine.util.MarbleBag;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.model.util.ValueUtils.MICROS_PER_MINUTE;
import static io.xj.model.util.ValueUtils.NANOS_PER_MICRO;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class FabricatorImpl implements Fabricator {
  static final String KEY_VOICE_NOTE_TEMPLATE = "voice-%s_note-%s";
  static final String KEY_VOICE_TRACK_TEMPLATE = "voice-%s_track-%s";
  static final String NAME_SEPARATOR = "-";
  static final String UNKNOWN_KEY = "unknown";
  final Logger LOG = LoggerFactory.getLogger(FabricatorImpl.class);
  final Chain chain;
  final TemplateConfig templateConfig;
  final Collection<TemplateBinding> templateBindings;
  final HubContent sourceMaterial;
  final double outputFrameRate;
  final int outputChannels;
  final JsonapiPayloadFactory jsonapiPayloadFactory;
  final JsonProvider jsonProvider;
  final Map<Double, Optional<SegmentChord>> chordAtPosition;
  final Map<Instrument::Type, NoteRange> voicingNoteRange;
  final Map<SegmentChoice, ProgramSequence> sequenceForChoice;
  final Map<String, InstrumentAudio> preferredAudios;
  final Map<String, InstrumentConfig> instrumentConfigs;
  final Map<String, InstrumentConfig> pickInstrumentConfigs;
  final Map<String, Integer> rangeShiftOctave;
  final Map<String, Integer> targetShift;
  final Map<String, NoteRange> rangeForChoice;
  final Map<String, Optional<Note>> rootNotesByVoicingAndChord;
  final Map<UUID, Collection<ProgramSequenceChord>> completeChordsForProgramSequence;
  final Map<UUID, List<SegmentChoiceArrangementPick>> picksForChoice;
  private final FabricationEntityStore store;
  final SegmentRetrospective retrospective;
  final Set<UUID> boundInstrumentIds;
  final Set<UUID> boundProgramIds;
  final long startAtSystemNanoTime;
  private final Integer segmentId;
  Segment::Type type;
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  Optional<SegmentChoice> macroChoiceOfPreviousSegment;
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  Optional<SegmentChoice> mainChoiceOfPreviousSegment;

  @Nullable
  Double microsPerBeat;

  @Nullable
  Set<Instrument::Type> distinctChordVoicingTypes;

  public FabricatorImpl(
    FabricatorFactory fabricatorFactory,
    FabricationEntityStore store,
    HubContent sourceMaterial,
    Integer segmentId,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    JsonProvider jsonProvider,
    double outputFrameRate,
    int outputChannels,
    @Nullable Segment::Type overrideSegmentType
  ) throws FabricationException, FabricationFatalException {
    this.store = store;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.jsonProvider = jsonProvider;
    this.sourceMaterial = sourceMaterial;
    this.outputFrameRate = outputFrameRate;
    this.outputChannels = outputChannels;

    // caches
    chordAtPosition = new HashMap<>();
    completeChordsForProgramSequence = new HashMap<>();
    instrumentConfigs = new HashMap<>();
    pickInstrumentConfigs = new HashMap<>();
    picksForChoice = new HashMap<>();
    rangeForChoice = new HashMap<>();
    rangeShiftOctave = new HashMap<>();
    rootNotesByVoicingAndChord = new HashMap<>();
    sequenceForChoice = new HashMap<>();
    targetShift = new HashMap<>();
    voicingNoteRange = new HashMap<>();

    // keep elapsed time based on system nano time
    startAtSystemNanoTime = System.nanoTime();

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

  @Override
  public void addMessage(SegmentMessageType messageType, String body) {
    try {
      var msg = new SegmentMessage();
      msg.setId(UUID.randomUUID());
      msg.setSegmentId(getSegment().id);
      msg.setType(messageType);
      msg.setBody(body);
      put(msg, false);
    } catch (FabricationException e) {
      LOG.warn("Failed to add message!", e);
    }
  }

  @Override
  public void addErrorMessage(String body) {
    addMessage(SegmentMessageType.ERROR, body);
  }

  @Override
  public void addWarningMessage(String body) {
    addMessage(SegmentMessageType.WARNING, body);
  }

  @Override
  public void addInfoMessage(String body) {
    addMessage(SegmentMessageType.INFO, body);
  }

  @Override
  public <N> void delete(int segmentId, Class<N> type, UUID id) {
    store.delete(segmentId, type, id);
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements() {
    return store.readAll(segmentId, SegmentChoiceArrangement.class);
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements(Collection<SegmentChoice> choices) {
    Collection<UUID> choiceIds = EntityUtils.idsOf(choices);
    return getArrangements().stream().filter(arrangement -> choiceIds.contains(arrangement.getSegmentChoiceId())).collect(Collectors.toList());
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
  public Collection<SegmentChoice> getChoices() {
    return store.readAll(segmentId, SegmentChoice.class);
  }

  @Override
  public Optional<SegmentChord> getChordAt(Double position) {
    if (!chordAtPosition.containsKey(position)) {
      Optional<SegmentChord> foundChord = Optional.empty();
      Double foundPosition = null;

      // we assume that these entities are in order of position ascending
      for (SegmentChord segmentChord : getSegmentChords()) {
        // if it's a better match (or no match has yet been found) then use it
        if (Objects.isNull(foundPosition) || (segmentChord.getPosition() > foundPosition && segmentChord.getPosition() <= position)) {
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
    return getChoiceOfType(Program::Type.Main);
  }

  @Override
  public Collection<SegmentChoice> getCurrentDetailChoices() {
    return getBeatChoices();
  }

  @Override
  public Optional<SegmentChoice> getCurrentBeatChoice() {
    return getChoiceOfType(Program::Type.Beat);
  }

  @Override
  public Set<Instrument::Type> getDistinctChordVoicingTypes() {
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

  @Override
  public Long getElapsedMicros() {
    return (System.nanoTime() - startAtSystemNanoTime) / NANOS_PER_MICRO;
  }

  @Override
  public InstrumentConfig getInstrumentConfig(Instrument instrument) {
    if (!instrumentConfigs.containsKey(instrument.id))
      instrumentConfigs.put(instrument.id, new InstrumentConfig(instrument));
    return instrumentConfigs.get(instrument.id);
  }

  @Override
  public Optional<SegmentChoice> getChoiceIfContinued(ProgramVoice voice) {
    try {
      if (!Objects.equals(Segment::Type.CONTINUE, getSegment().type)) return Optional.empty();
      return retrospective.getChoices().stream().filter(choice -> {
        var candidateVoice = sourceMaterial.getProgramVoice(choice.programVoiceId);
        return candidateVoice.isPresent() && Objects.equals(candidateVoice.get().getName(), voice.getName()) && Objects.equals(candidateVoice.get().type, voice.type);
      }).findFirst();

    } catch (Exception e) {
      LOG.warn(formatLog(String.format("Could not get previous voice instrumentId for voiceName=%s", voice.getName())), e);
      return Optional.empty();
    }
  }

  @Override
  public Optional<SegmentChoice> getChoiceIfContinued(Instrument::Type instrumentType) {
    try {
      if (!Objects.equals(Segment::Type.CONTINUE, getSegment().type)) return Optional.empty();
      return retrospective.getChoices().stream().filter(choice -> Objects.equals(instrumentType, choice.instrumentType)).findFirst();

    } catch (Exception e) {
      LOG.warn(formatLog(String.format("Could not get previous choice for instrumentType=%s", instrumentType)), e);
      return Optional.empty();
    }
  }

  @Override
  public Optional<SegmentChoice> getChoiceIfContinued(Instrument::Type instrumentType, InstrumentMode instrumentMode) {
    try {
      if (!Objects.equals(Segment::Type.CONTINUE, getSegment().type)) return Optional.empty();
      return retrospective.getChoices().stream().filter(choice -> Objects.equals(instrumentType, choice.instrumentType) && Objects.equals(instrumentMode, choice.getInstrumentMode())).findFirst();

    } catch (Exception e) {
      LOG.warn(formatLog(String.format("Could not get previous choice for instrumentType=%s", instrumentType)), e);
      return Optional.empty();
    }
  }

  @Override
  public Collection<SegmentChoice> getChoicesIfContinued(Program::Type programType) {
    try {
      if (!Objects.equals(Segment::Type.CONTINUE, getSegment().type)) return Set.of();
      return retrospective.getChoices().stream().filter(choice -> Objects.equals(programType, choice.programType)).collect(Collectors.toSet());

    } catch (Exception e) {
      LOG.warn(formatLog(String.format("Could not get previous choice for programType=%s", programType)), e);
      return Set.of();
    }
  }

  @Override
  public String computeCacheKeyForVoiceTrack(SegmentChoiceArrangementPick pick) {
    String cacheKey = sourceMaterial().getProgramSequencePatternEvent(pick.getProgramSequencePatternEventId()).flatMap(event -> sourceMaterial().getTrackOfEvent(event).map(ProgramVoiceTrack::getProgramVoiceId)).map(UUID::toString).orElse(UNKNOWN_KEY);

    return String.format(KEY_VOICE_TRACK_TEMPLATE, cacheKey, pick.getEvent());
  }

  @Override
  public Chord getKeyForChoice(SegmentChoice choice) throws FabricationException {
    Optional<Program> program = getProgram(choice);
    if (ValueUtils.isSet(choice.programSequenceBindingId)) {
      var sequence = getSequence(choice);
      if (sequence.isPresent() && !StringUtils.isNullOrEmpty(sequence.get().getKey()))
        return Chord.of(sequence.get().getKey());
    }

    return Chord.of(program.orElseThrow(() -> new FabricationException("Cannot get key for nonexistent choice!")).getKey());
  }

  @Override
  public Optional<ProgramSequence> getProgramSequence(SegmentChoice choice) {
    if (Objects.nonNull(choice.programSequenceId))
      return sourceMaterial.getProgramSequence(choice.programSequenceId);
    if (Objects.isNull(choice.programSequenceBindingId)) return Optional.empty();
    var psb = sourceMaterial.getProgramSequenceBinding(choice.programSequenceBindingId);
    if (psb.isEmpty()) return Optional.empty();
    return sourceMaterial.getProgramSequence(psb.get().programSequenceId);
  }

  @Override
  public Optional<SegmentChoice> getMacroChoiceOfPreviousSegment() {
    if (Objects.isNull(macroChoiceOfPreviousSegment))
      macroChoiceOfPreviousSegment = retrospective.getPreviousChoiceOfType(Program::Type.Macro);
    return macroChoiceOfPreviousSegment;
  }

  @Override
  public Optional<SegmentChoice> getPreviousMainChoice() {
    if (Objects.isNull(mainChoiceOfPreviousSegment))
      mainChoiceOfPreviousSegment = retrospective.getPreviousChoiceOfType(Program::Type.Main);
    return mainChoiceOfPreviousSegment;
  }

  @Override
  public ProgramConfig getCurrentMainProgramConfig() throws FabricationException {
    try {
      return new ProgramConfig(
        sourceMaterial.getProgram(getCurrentMainChoice()
            .orElseThrow(() -> new FabricationException("No current main choice!")).programId)
          .orElseThrow(() -> new FabricationException("Failed to retrieve current main program config!")));

    } catch (ValueException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Optional<ProgramSequence> getCurrentMainSequence() {
    var mc = getCurrentMainChoice();
    if (mc.isEmpty()) return Optional.empty();
    return getProgramSequence(mc.get());
  }

  @Override
  public Optional<ProgramSequence> getPreviousMainSequence() {
    var mc = getPreviousMainChoice();
    if (mc.isEmpty()) return Optional.empty();
    return getProgramSequence(mc.get());
  }

  @Override
  public MemeIsometry getMemeIsometryOfNextSequenceInPreviousMacro() {
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

  @Override
  public MemeIsometry getMemeIsometryOfSegment() {
    return MemeIsometry.of(templateConfig.getMemeTaxonomy(), EntityUtils.namesOf(getSegmentMemes()));
  }

  @Override
  public Integer getNextSequenceBindingOffset(SegmentChoice choice) {
    if (ValueUtils.isEmpty(choice.programSequenceBindingId)) return 0;

    var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.programSequenceBindingId);
    Integer sequenceBindingOffset = getSequenceBindingOffsetForChoice(choice);
    Integer offset = null;
    if (sequenceBinding.isEmpty()) return 0;
    for (Integer availableOffset : sourceMaterial.getAvailableOffsets(sequenceBinding.get()))
      if (0 < availableOffset.compareTo(sequenceBindingOffset))
        if (Objects.isNull(offset) || 0 > availableOffset.compareTo(offset)) offset = availableOffset;

    // if none found, loop back around to zero
    return Objects.nonNull(offset) ? offset : 0;

  }

  @Override
  public Collection<String> getNotes(SegmentChordVoicing voicing) {
    return new ArrayList<>(CsvUtils.split(voicing.notes));
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPicks() {
    return store.readAll(segmentId, SegmentChoiceArrangementPick.class);
  }

  @Override
  public List<SegmentChoiceArrangementPick> getPicks(SegmentChoice choice) {
    if (!picksForChoice.containsKey(choice.id)) {
      var arrangementIds = getArrangements().stream().filter(a -> a.getSegmentChoiceId().equals(choice.id)).map(SegmentChoiceArrangement::getId).toList();
      picksForChoice.put(choice.id, getPicks().stream()
        .filter(p -> arrangementIds.contains(p.getSegmentChoiceArrangementId()))
        .sorted(Comparator.comparing(SegmentChoiceArrangementPick::getStartAtSegmentMicros)).toList());
    }
    return picksForChoice.get(choice.id);
  }

  @Override
  public Optional<InstrumentAudio> getPreferredAudio(String parentIdent, String ident) {
    String cacheKey = String.format(KEY_VOICE_NOTE_TEMPLATE, parentIdent, ident);

    if (preferredAudios.containsKey(cacheKey)) return Optional.of(preferredAudios.get(cacheKey));

    return Optional.empty();
  }

  @Override
  public Optional<Program> getProgram(SegmentChoice choice) {
    return sourceMaterial.getProgram(choice.programId);
  }

  @Override
  public ProgramConfig getProgramConfig(Program program) throws FabricationException {
    try {
      return new ProgramConfig(program);
    } catch (ValueException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public Collection<ProgramSequenceChord> getProgramSequenceChords(ProgramSequence programSequence) {
    if (!completeChordsForProgramSequence.containsKey(programSequence.id)) {
      Map<Double, ProgramSequenceChord> chordForPosition = new HashMap<>();
      Map<Double, Integer> validVoicingsForPosition = new HashMap<>();
      for (ProgramSequenceChord chord : sourceMaterial.getChordsOfSequence(programSequence)) {
        int validVoicings = sourceMaterial.getVoicingsOfChord(chord).stream().map(V -> CsvUtils.split(V.notes).size()).reduce(0, Integer::sum);
        if (!validVoicingsForPosition.containsKey(chord.getPosition()) || validVoicingsForPosition.get(chord.getPosition()) < validVoicings) {
          validVoicingsForPosition.put(chord.getPosition(), validVoicings);
          chordForPosition.put(chord.getPosition(), chord);
        }
      }
      completeChordsForProgramSequence.put(programSequence.id, chordForPosition.values());
    }

    return completeChordsForProgramSequence.get(programSequence.id);
  }

  @Override
  public NoteRange getProgramRange(UUID programId, Instrument::Type instrumentType) {
    var cacheKey = String.format("%s__%s", programId, instrumentType);

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

  @Override
  public int getProgramRangeShiftOctaves(Instrument::Type type, NoteRange sourceRange, NoteRange targetRange) throws FabricationException {
    var cacheKey = String.format("%s__%s__%s", type, sourceRange.toString(Accidental.None), targetRange.toString(Accidental.None));

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

  @Override
  public int getProgramTargetShift(Instrument::Type instrumentType, Chord fromChord, Chord toChord) {
    if (!fromChord.isPresent()) return 0;
    var cacheKey = String.format("%s__%s__%s", instrumentType, fromChord, toChord);
    if (!targetShift.containsKey(cacheKey)) {
      if (instrumentType.equals(Instrument::Type.Bass)) {
        targetShift.put(cacheKey, fromChord.getRoot().delta(toChord.getSlashRoot()));
      } else {
        targetShift.put(cacheKey, fromChord.getRoot().delta(toChord.getRoot()));
      }
    }

    return targetShift.get(cacheKey);
  }

  @Override
  public Program::Type getProgramType(ProgramVoice voice) throws FabricationException {
    return sourceMaterial.getProgram(voice.programId).orElseThrow(() -> new FabricationException("Could not get program!")).type;
  }

  @Override
  public Instrument::Type getProgramVoiceType(ProgramSequenceChordVoicing voicing) throws FabricationException {
    return sourceMaterial.getProgramVoice(voicing.programVoiceId).orElseThrow(() -> new FabricationException("Could not get voice!")).type;
  }

  @Override
  public NoteRange getProgramVoicingNoteRange(Instrument::Type type) {
    if (!voicingNoteRange.containsKey(type)) {
      voicingNoteRange.put(type, NoteRange.ofStrings(getChordVoicings().stream().filter(SegmentUtils::containsAnyValidNotes).filter(segmentChordVoicing -> Objects.equals(segmentChordVoicing.type, type)).flatMap(segmentChordVoicing -> getNotes(segmentChordVoicing).stream()).collect(Collectors.toList())));
    }

    return voicingNoteRange.get(type);
  }

  @Override
  public Optional<ProgramSequence> getRandomlySelectedSequence(Program program) {
    var bag = MarbleBag.empty();
    sourceMaterial.getProgramSequences().stream().filter(s -> Objects.equals(s.programId, program.id)).forEach(sequence -> bag.add(1, sequence.id));
    if (bag.isEmpty()) return Optional.empty();
    return sourceMaterial.getProgramSequence(bag.pick());
  }

  @Override
  public Optional<ProgramSequenceBinding> getRandomlySelectedSequenceBindingAtOffset(Program program, Integer offset) {
    var bag = MarbleBag.empty();
    for (ProgramSequenceBinding sequenceBinding : sourceMaterial.getBindingsAtOffsetOfProgram(program, offset, true))
      bag.add(1, sequenceBinding.id);
    if (bag.isEmpty()) return Optional.empty();
    return sourceMaterial.getProgramSequenceBinding(bag.pick());
  }

  @Override
  public Optional<ProgramSequencePattern> getRandomlySelectedPatternOfSequenceByVoiceAndType(SegmentChoice choice) {
    var bag = MarbleBag.empty();
    sourceMaterial.getProgramSequencePatterns().stream().filter(pattern -> Objects.equals(pattern.programSequenceId, choice.programSequenceId)).filter(pattern -> Objects.equals(pattern.programVoiceId, choice.programVoiceId)).forEach(pattern -> bag.add(1, pattern.id));
    if (bag.isEmpty()) return Optional.empty();
    return sourceMaterial.getProgramSequencePattern(bag.pick());
  }

  @Override
  public Optional<Note> getRootNoteMidRange(String voicingNotes, Chord chord) {
    return rootNotesByVoicingAndChord.computeIfAbsent(String.format("%s_%s", voicingNotes, chord.getName()),
      (String key) -> NoteRange.ofStrings(CsvUtils.split(voicingNotes)).getNoteNearestMedian(chord.getSlashRoot()));
  }

  @Override
  public void putStickyBun(StickyBun bun) throws JsonProcessingException, FabricationException {
    store.put(new SegmentMeta()
      .id(UUID.randomUUID())
      .segmentId(getSegment().id)
      .key(bun.computeMetaKey())
      .value(jsonProvider.getMapper().writeValueAsString(bun)));
  }

  @Override
  public Optional<StickyBun> getStickyBun(UUID eventId) {
    if (!templateConfig.isStickyBunEnabled()) return Optional.empty();
    //
    var currentMeta = getSegmentMeta(StickyBun.computeMetaKey(eventId));
    if (currentMeta.isPresent()) {
      try {
        return Optional.of(jsonProvider.getMapper().readValue(currentMeta.get().getValue(), StickyBun.class));
      } catch (JsonProcessingException e) {
        addErrorMessage(String.format("Failed to deserialize current segment meta value StickyBun JSON for Event[%s]", eventId));
      }
    }
    //
    var previousMeta = retrospective.getPreviousMeta(StickyBun.computeMetaKey(eventId));
    if (previousMeta.isPresent()) {
      try {
        return Optional.of(jsonProvider.getMapper().readValue(previousMeta.get().getValue(), StickyBun.class));
      } catch (JsonProcessingException e) {
        addErrorMessage(String.format("Failed to deserialize previous segment meta value StickyBun JSON for Event[%s]", eventId));
      }
    }
    var event = sourceMaterial.getProgramSequencePatternEvent(eventId);
    if (event.isEmpty()) {
      addErrorMessage(String.format("Failed to get StickyBun for Event[%s] because it does not exist", eventId));
      return Optional.empty();
    }
    var bun = new StickyBun(eventId, CsvUtils.split(event.get().getTones()).size());
    try {
      putStickyBun(bun);
    } catch (FabricationException e) {
      addErrorMessage(String.format("Failed to put StickyBun for Event[%s] because %s", eventId, e.getMessage()));
    } catch (JsonProcessingException e) {
      addErrorMessage(String.format("Failed to serialize segment meta value StickyBun JSON for Event[%s]", eventId));
    }
    return Optional.of(bun);
  }

  private Optional<SegmentMeta> getSegmentMeta(String key) {
    return store.readAll(segmentId, SegmentMeta.class).stream()
      .filter(m -> Objects.equals(key, m.getKey()))
      .findAny();
  }

  @Override
  public long getSegmentMicrosAtPosition(double tempo, double position) {
    return (long) (getMicrosPerBeat(tempo) * position);
  }

  @Override
  public long getTotalSegmentMicros() {
    return Objects.requireNonNull(getSegment().durationMicros);
  }

  @Override
  public Segment getSegment() {
    return store.readSegment(segmentId).orElseThrow(() -> new RuntimeException(String.format("Found no Segment[%d]", segmentId)));
  }

  @Override
  public List<SegmentChord> getSegmentChords() {
    return store.readAll(segmentId, SegmentChord.class).stream()
      .sorted(Comparator.comparing(SegmentChord::getPosition))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<SegmentChordVoicing> getChordVoicings() {
    return store.readAll(segmentId, SegmentChordVoicing.class);
  }

  @Override
  public Collection<SegmentMeme> getSegmentMemes() {
    return store.readAll(segmentId, SegmentMeme.class);
  }

  @Override
  public Optional<ProgramSequence> getSequence(SegmentChoice choice) {
    Optional<Program> program = getProgram(choice);
    if (program.isEmpty()) return Optional.empty();
    if (ValueUtils.isSet(choice.programSequenceBindingId)) {
      var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.programSequenceBindingId);
      if (sequenceBinding.isPresent())
        return sourceMaterial.getProgramSequence(sequenceBinding.get().programSequenceId);
    }

    if (!sequenceForChoice.containsKey(choice))
      getRandomlySelectedSequence(program.get()).ifPresent(programSequence -> sequenceForChoice.put(choice, programSequence));

    return Optional.of(sequenceForChoice.get(choice));
  }

  @Override
  public Integer getSequenceBindingOffsetForChoice(SegmentChoice choice) {
    if (ValueUtils.isEmpty(choice.programSequenceBindingId)) return 0;
    var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.programSequenceBindingId);
    return sequenceBinding.map(ProgramSequenceBinding::getOffset).orElse(0);
  }

  @Override
  public String getTrackName(ProgramSequencePatternEvent event) {
    return sourceMaterial().getTrackOfEvent(event).map(ProgramVoiceTrack::getName).orElse(UNKNOWN_KEY);
  }

  @Override
  public Segment::Type type throws FabricationException {
    if (ValueUtils.isEmpty(type)) type = computeType();
    return type;
  }

  @Override
  public Optional<SegmentChordVoicing> chooseVoicing(SegmentChord chord, Instrument::Type type) {
    Collection<SegmentChordVoicing> voicings = store.readAll(segmentId, SegmentChordVoicing.class);
    return MarbleBag.quickPick(voicings.stream()
      .filter(SegmentUtils::containsAnyValidNotes)
      .filter(voicing -> Objects.equals(type, voicing.type))
      .filter(voicing -> Objects.equals(chord.id, voicing.getSegmentChordId()))
      .collect(Collectors.toList()));
  }

  @Override
  public boolean hasMoreSequenceBindingOffsets(SegmentChoice choice, int N) {
    if (ValueUtils.isEmpty(choice.programSequenceBindingId)) return false;
    var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.programSequenceBindingId);

    if (sequenceBinding.isEmpty()) return false;
    List<Integer> avlOfs = List.copyOf(sourceMaterial.getAvailableOffsets(sequenceBinding.get()));

    // if we locate the target and still have two offsets remaining, result is true
    for (int i = 0; i < avlOfs.size(); i++)
      if (Objects.equals(avlOfs.get(i), sequenceBinding.get().getOffset()) && i < avlOfs.size() - N) return true;

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
  public boolean isContinuationOfMacroProgram() throws FabricationException {
    return Segment::Type.CONTINUE.equals(type) || Segment::Type.NEXT_MAIN.equals(type);
  }

  @Override
  public boolean isDirectlyBound(Program program) {
    return boundProgramIds.contains(program.id);
  }

  @Override
  public boolean isOneShot(Instrument instrument, String trackName) {
    return isOneShot(instrument) && !getInstrumentConfig(instrument).getOneShotObserveLengthOfEvents().contains(trackName);
  }

  @Override
  public boolean isOneShot(Instrument instrument) {
    return getInstrumentConfig(instrument).isOneShot();
  }

  @Override
  public boolean isOneShotCutoffEnabled(Instrument instrument) {
    return getInstrumentConfig(instrument).isOneShotCutoffEnabled();
  }

  @Override
  public boolean isDirectlyBound(Instrument instrument) {
    return boundInstrumentIds.contains(instrument.id);
  }

  @Override
  public boolean isDirectlyBound(InstrumentAudio instrumentAudio) {
    return boundInstrumentIds.contains(instrumentAudio.instrumentId);
  }

  @Override
  public Boolean isInitialSegment() {
    return 0L == getSegment().id;
  }

  @Override
  public <N> N put(N entity, boolean force) throws FabricationException {
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

  @Override
  public void putPreferredAudio(String parentIdent, String ident, InstrumentAudio instrumentAudio) {
    String cacheKey = String.format(KEY_VOICE_NOTE_TEMPLATE, parentIdent, ident);

    preferredAudios.put(cacheKey, instrumentAudio);
  }

  @Override
  public void putReport(String key, Object value) {
    addMessage(SegmentMessageType.DEBUG, String.format("%s: %s", key, value));
  }

  @Override
  public void updateSegment(Segment segment) {
    try {
      store.updateSegment(segment);

    } catch (FabricationException e) {
      LOG.error("Failed to update Segment", e);
    }
  }

  @Override
  public SegmentRetrospective retrospective() {
    return retrospective;
  }

  @Override
  public HubContent sourceMaterial() {
    return sourceMaterial;
  }

  @Override
  public Double getMicrosPerBeat(double tempo) {
    if (Objects.isNull(microsPerBeat))
      microsPerBeat = (double) MICROS_PER_MINUTE / tempo;
    return microsPerBeat;
  }

  @Override
  public int getSecondMacroSequenceBindingOffset(Program macroProgram) {
    var offsets = sourceMaterial.getSequenceBindingsOfProgram(macroProgram.id).stream()
      .map(ProgramSequenceBinding::getOffset)
      .collect(Collectors.toSet()).stream().sorted().toList();
    return offsets.size() > 1 ? offsets.get(1) : offsets.get(0);
  }

  @Override
  public MemeTaxonomy getMemeTaxonomy() {
    return templateConfig.getMemeTaxonomy();
  }

  @Override
  public double getTempo() throws FabricationException {
    return getSegment().getTempo();
  }

  /**
   Get the choices of the current segment of the given type

   @param programType of choices to get
   @return choices of the current segment of the given type
   */
  private Optional<SegmentChoice> getChoiceOfType(Program::Type programType) {
    return getChoices().stream().filter(c -> Objects.equals(c.programType, programType)).findFirst();
  }

  /**
   Get the choices of the current segment of the given type

   @return choices of the current segment of the given type
   */
  private List<SegmentChoice> getBeatChoices() {
    return getChoices().stream().filter(c -> Objects.equals(c.programType, Program::Type.Beat)).toList();
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
  private String computeShipKey(Chain chain, Segment segment) {
    String chainName = StringUtils.isNullOrEmpty(chain.shipKey) ? "chain" + NAME_SEPARATOR + chain.id : chain.shipKey;
    String segmentName = String.valueOf(segment.beginAtChainMicros);
    return chainName + NAME_SEPARATOR + segmentName;
  }

  /**
   Format a message with the segmentId as prefix

   @param message to format
   @return formatted message with segmentId as prefix
   */
  private String formatLog(String message) {
    return String.format("[segId=%s] %s", getSegment().id, message);
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
      return Segment::Type.INITIAL;

    // previous main choice having at least one more pattern?
    var previousMainChoice = getPreviousMainChoice();

    if (previousMainChoice.isPresent() && hasOneMoreSequenceBindingOffset(previousMainChoice.get())
      && getTemplateConfig().getMainProgramLengthMaxDelta() > getPreviousSegmentDelta())
      return Segment::Type.CONTINUE;

    // previous macro choice having at least two more patterns?
    var previousMacroChoice = getMacroChoiceOfPreviousSegment();

    if (previousMacroChoice.isPresent() && hasTwoMoreSequenceBindingOffsets(previousMacroChoice.get()))
      return Segment::Type.NEXT_MAIN;

    return Segment::Type.NEXT_MACRO;
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
  private Map<String, InstrumentAudio> computePreferredInstrumentAudio() {
    Map<String, InstrumentAudio> audios = new HashMap<>();

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
    Set<String> names = new HashSet<>();

    if (Objects.nonNull(choice.programId))
      sourceMaterial().getMemesOfProgram(choice.programId)
        .forEach(meme -> names.add(StringUtils.toMeme(meme.getName())));

    if (Objects.nonNull(choice.programSequenceBindingId))
      sourceMaterial().getMemesOfSequenceBinding(choice.programSequenceBindingId)
        .forEach(meme -> names.add(StringUtils.toMeme(meme.getName())));

    if (Objects.nonNull(choice.instrumentId))
      sourceMaterial().getMemesOfInstrument(choice.instrumentId)
        .forEach(meme -> names.add(StringUtils.toMeme(meme.getName())));

    if (!force && !memeStack.isAllowed(names)) {
      addMessage(SegmentMessageType.ERROR, String.format("Refused to add Choice[%s] because adding Memes[%s] to MemeStack[%s] would result in an invalid meme stack theorem!",
        SegmentUtils.describe(choice),
        CsvUtils.join(names.stream().toList()),
        memeStack.getConstellation()));
      return false;
    }

    for (String name : names) {
      var segmentMeme = new SegmentMeme();
      segmentMeme.setId(UUID.randomUUID());
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
    if (!force && !memeStack.isAllowed(List.of(meme.getName()))) return false;
    if (!force && getSegmentMemes().stream().anyMatch(m -> Objects.equals(m.getName(), meme.getName()))) return false;
    return true;
  }
}
