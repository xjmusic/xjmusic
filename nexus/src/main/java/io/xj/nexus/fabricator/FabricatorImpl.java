// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.xj.hub.HubContent;
import io.xj.hub.InstrumentConfig;
import io.xj.hub.ProgramConfig;
import io.xj.hub.TemplateConfig;
import io.xj.hub.enums.ContentBindingType;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.meme.MemeStack;
import io.xj.hub.meme.MemeTaxonomy;
import io.xj.hub.music.Accidental;
import io.xj.hub.music.Chord;
import io.xj.hub.music.Note;
import io.xj.hub.music.NoteRange;
import io.xj.hub.music.PitchClass;
import io.xj.hub.music.StickyBun;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.tables.pojos.ProgramMeme;
import io.xj.hub.tables.pojos.ProgramSequence;
import io.xj.hub.tables.pojos.ProgramSequenceBinding;
import io.xj.hub.tables.pojos.ProgramSequenceBindingMeme;
import io.xj.hub.tables.pojos.ProgramSequenceChord;
import io.xj.hub.tables.pojos.ProgramSequenceChordVoicing;
import io.xj.hub.tables.pojos.ProgramSequencePattern;
import io.xj.hub.tables.pojos.ProgramSequencePatternEvent;
import io.xj.hub.tables.pojos.ProgramVoice;
import io.xj.hub.tables.pojos.ProgramVoiceTrack;
import io.xj.hub.tables.pojos.TemplateBinding;
import io.xj.hub.util.CsvUtils;
import io.xj.hub.util.StringUtils;
import io.xj.hub.util.ValueException;
import io.xj.hub.util.ValueUtils;
import io.xj.nexus.NexusException;
import io.xj.nexus.entity.EntityUtils;
import io.xj.nexus.json.JsonProvider;
import io.xj.nexus.jsonapi.JsonapiPayloadFactory;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentChoiceArrangement;
import io.xj.nexus.model.SegmentChoiceArrangementPick;
import io.xj.nexus.model.SegmentChord;
import io.xj.nexus.model.SegmentChordVoicing;
import io.xj.nexus.model.SegmentMeme;
import io.xj.nexus.model.SegmentMessage;
import io.xj.nexus.model.SegmentMessageType;
import io.xj.nexus.model.SegmentMeta;
import io.xj.nexus.model.SegmentType;
import io.xj.nexus.persistence.ChainUtils;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.ManagerPrivilegeException;
import io.xj.nexus.persistence.ManagerValidationException;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.SegmentUtils;
import io.xj.nexus.util.MarbleBag;
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

import static io.xj.hub.util.ValueUtils.MICROS_PER_MINUTE;
import static io.xj.hub.util.ValueUtils.NANOS_PER_MICRO;

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
  final Map<InstrumentType, NoteRange> voicingNoteRange;
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
  private final NexusEntityStore store;
  final SegmentRetrospective retrospective;
  final Set<UUID> boundInstrumentIds;
  final Set<UUID> boundProgramIds;
  final long startAtSystemNanoTime;
  private final Integer segmentId;
  SegmentType type;
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  Optional<SegmentChoice> macroChoiceOfPreviousSegment;
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  Optional<SegmentChoice> mainChoiceOfPreviousSegment;

  @Nullable
  Double microsPerBeat;

  @Nullable
  Set<InstrumentType> distinctChordVoicingTypes;

  public FabricatorImpl(
    FabricatorFactory fabricatorFactory,
    NexusEntityStore store,
    HubContent sourceMaterial,
    Integer segmentId,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    JsonProvider jsonProvider,
    double outputFrameRate,
    int outputChannels,
    @Nullable SegmentType overrideSegmentType
  ) throws NexusException, FabricationFatalException, ValueException {
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
    templateConfig = new TemplateConfig(sourceMaterial.getTemplate());
    templateBindings = sourceMaterial.getTemplateBindings();
    boundProgramIds = ChainUtils.targetIdsOfType(templateBindings, ContentBindingType.Program);
    boundInstrumentIds = ChainUtils.targetIdsOfType(templateBindings, ContentBindingType.Instrument);
    LOG.debug("[segId={}] Chain {} configured with {} and bound to {} ", segmentId, chain.getId(), templateConfig, CsvUtils.prettyFrom(templateBindings, "and"));

    // set up the segment retrospective
    retrospective = fabricatorFactory.loadRetrospective(segmentId);

    // digest previous instrument audio
    preferredAudios = computePreferredInstrumentAudio();

    // the current segment on the workbench
    this.segmentId = segmentId;

    // Override the segment type by passing the fabricator a segment type on creation
    // Workstation has live performance modulation https://www.pivotaltracker.com/story/show/186003440
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
      msg.setSegmentId(getSegment().getId());
      msg.setType(messageType);
      msg.setBody(body);
      put(msg, false);
    } catch (NexusException e) {
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
  public <N> void delete(int segmentId, Class<N> type, UUID id) throws NexusException {
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
    return getChoiceOfType(ProgramType.Main);
  }

  @Override
  public Collection<SegmentChoice> getCurrentDetailChoices() {
    return getBeatChoices();
  }

  @Override
  public Optional<SegmentChoice> getCurrentBeatChoice() {
    return getChoiceOfType(ProgramType.Beat);
  }

  @Override
  public Set<InstrumentType> getDistinctChordVoicingTypes() {
    if (Objects.isNull(distinctChordVoicingTypes)) {
      var mainChoice = getCurrentMainChoice();
      if (mainChoice.isEmpty()) return Set.of();
      var voicings = sourceMaterial.getProgramSequenceChordVoicings(mainChoice.get().getProgramId());
      distinctChordVoicingTypes = voicings.stream().flatMap(voicing -> {
        try {
          return Stream.of(getProgramVoiceType(voicing));
        } catch (NexusException e) {
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
    if (!instrumentConfigs.containsKey(instrument.getId().toString()))
      instrumentConfigs.put(instrument.getId().toString(), new InstrumentConfig(instrument));
    return instrumentConfigs.get(instrument.getId().toString());
  }

  @Override
  public Optional<SegmentChoice> getChoiceIfContinued(ProgramVoice voice) {
    try {
      if (!Objects.equals(SegmentType.CONTINUE, getSegment().getType())) return Optional.empty();
      return retrospective.getChoices().stream().filter(choice -> {
        var candidateVoice = sourceMaterial.getProgramVoice(choice.getProgramVoiceId());
        return candidateVoice.isPresent() && Objects.equals(candidateVoice.get().getName(), voice.getName()) && Objects.equals(candidateVoice.get().getType(), voice.getType());
      }).findFirst();

    } catch (Exception e) {
      LOG.warn(formatLog(String.format("Could not get previous voice instrumentId for voiceName=%s", voice.getName())), e);
      return Optional.empty();
    }
  }

  @Override
  public Optional<SegmentChoice> getChoiceIfContinued(InstrumentType instrumentType) {
    try {
      return switch (getSegment().getType()) {
        case INITIAL, NEXT_MAIN, NEXT_MACRO, PENDING -> Optional.empty();
        case CONTINUE ->
          retrospective.getChoices().stream().filter(choice -> Objects.equals(instrumentType, choice.getInstrumentType())).findFirst();
      };

    } catch (Exception e) {
      LOG.warn(formatLog(String.format("Could not get previous choice for instrumentType=%s", instrumentType)), e);
      return Optional.empty();
    }
  }

  @Override
  public Optional<SegmentChoice> getChoiceIfContinued(InstrumentType instrumentType, InstrumentMode instrumentMode) {
    try {
      return switch (getSegment().getType()) {
        case INITIAL, NEXT_MAIN, NEXT_MACRO, PENDING -> Optional.empty();
        case CONTINUE ->
          retrospective.getChoices().stream().filter(choice -> Objects.equals(instrumentType, choice.getInstrumentType()) && Objects.equals(instrumentMode, choice.getInstrumentMode())).findFirst();
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
        case PENDING, INITIAL, NEXT_MAIN, NEXT_MACRO -> Optional.empty();
        case CONTINUE ->
          retrospective.getChoices().stream().filter(choice -> Objects.equals(programType, choice.getProgramType())).findFirst();
      };

    } catch (Exception e) {
      LOG.warn(formatLog(String.format("Could not get previous choice for programType=%s", programType)), e);
      return Optional.empty();
    }
  }

  @Override
  public String computeCacheKeyForVoiceTrack(SegmentChoiceArrangementPick pick) {
    String cacheKey = sourceMaterial().getProgramSequencePatternEvent(pick.getProgramSequencePatternEventId()).flatMap(event -> sourceMaterial().getTrack(event).map(ProgramVoiceTrack::getProgramVoiceId)).map(UUID::toString).orElse(UNKNOWN_KEY);

    return String.format(KEY_VOICE_TRACK_TEMPLATE, cacheKey, pick.getEvent());
  }

  @Override
  public Chord getKeyForChoice(SegmentChoice choice) throws NexusException {
    Optional<Program> program = getProgram(choice);
    if (ValueUtils.isSet(choice.getProgramSequenceBindingId())) {
      var sequence = getSequence(choice);
      if (sequence.isPresent() && !StringUtils.isNullOrEmpty(sequence.get().getKey()))
        return Chord.of(sequence.get().getKey());
    }

    return Chord.of(program.orElseThrow(() -> new NexusException("Cannot get key for nonexistent choice!")).getKey());
  }

  @Override
  public Optional<ProgramSequence> getProgramSequence(SegmentChoice choice) {
    if (Objects.nonNull(choice.getProgramSequenceId()))
      return sourceMaterial.getProgramSequence(choice.getProgramSequenceId());
    if (Objects.isNull(choice.getProgramSequenceBindingId())) return Optional.empty();
    var psb = sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());
    if (psb.isEmpty()) return Optional.empty();
    return sourceMaterial.getProgramSequence(psb.get().getProgramSequenceId());
  }

  @Override
  public Optional<SegmentChoice> getMacroChoiceOfPreviousSegment() {
    if (Objects.isNull(macroChoiceOfPreviousSegment))
      macroChoiceOfPreviousSegment = retrospective.getPreviousChoiceOfType(ProgramType.Macro);
    return macroChoiceOfPreviousSegment;
  }

  @Override
  public Optional<SegmentChoice> getPreviousMainChoice() {
    if (Objects.isNull(mainChoiceOfPreviousSegment))
      mainChoiceOfPreviousSegment = retrospective.getPreviousChoiceOfType(ProgramType.Main);
    return mainChoiceOfPreviousSegment;
  }

  @Override
  public ProgramConfig getCurrentMainProgramConfig() throws NexusException {
    try {
      return new ProgramConfig(
        sourceMaterial.getProgram(getCurrentMainChoice()
            .orElseThrow(() -> new NexusException("No current main choice!")).getProgramId())
          .orElseThrow(() -> new NexusException("Failed to retrieve current main program config!")));

    } catch (ValueException e) {
      throw new NexusException(e);
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

    var previousSequenceBinding = sourceMaterial().getProgramSequenceBinding(previousMacroChoice.get().getProgramSequenceBindingId());
    if (previousSequenceBinding.isEmpty())
      return MemeIsometry.none();

    var nextSequenceBinding = sourceMaterial().getBindingsAtOffset(previousMacroChoice.get().getProgramId(),
      previousSequenceBinding.get().getOffset() + 1);

    return MemeIsometry.of(templateConfig.getMemeTaxonomy(),
      Stream.concat(
        sourceMaterial.getProgramMemes(previousMacroChoice.get().getProgramId()).stream().map(ProgramMeme::getName),
        nextSequenceBinding.stream().flatMap(programSequenceBinding ->
          sourceMaterial.getMemesForProgramSequenceBindingId(programSequenceBinding.getId()).stream().map(ProgramSequenceBindingMeme::getName))
      ).collect(Collectors.toList()));
  }

  @Override
  public MemeIsometry getMemeIsometryOfSegment() {
    return MemeIsometry.of(templateConfig.getMemeTaxonomy(), EntityUtils.namesOf(getSegmentMemes()));
  }

  @Override
  public Integer getNextSequenceBindingOffset(SegmentChoice choice) {
    if (ValueUtils.isEmpty(choice.getProgramSequenceBindingId())) return 0;

    var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());
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
    return new ArrayList<>(CsvUtils.split(voicing.getNotes()));
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPicks() {
    return store.readAll(segmentId, SegmentChoiceArrangementPick.class);
  }

  @Override
  public List<SegmentChoiceArrangementPick> getPicks(SegmentChoice choice) {
    if (!picksForChoice.containsKey(choice.getId())) {
      var arrangementIds = getArrangements().stream().filter(a -> a.getSegmentChoiceId().equals(choice.getId())).map(SegmentChoiceArrangement::getId).toList();
      picksForChoice.put(choice.getId(), getPicks().stream()
        .filter(p -> arrangementIds.contains(p.getSegmentChoiceArrangementId()))
        .sorted(Comparator.comparing(SegmentChoiceArrangementPick::getStartAtSegmentMicros)).toList());
    }
    return picksForChoice.get(choice.getId());
  }

  @Override
  public Optional<InstrumentAudio> getPreferredAudio(String parentIdent, String ident) {
    String cacheKey = String.format(KEY_VOICE_NOTE_TEMPLATE, parentIdent, ident);

    if (preferredAudios.containsKey(cacheKey)) return Optional.of(preferredAudios.get(cacheKey));

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
      Map<Double, ProgramSequenceChord> chordForPosition = new HashMap<>();
      Map<Double, Integer> validVoicingsForPosition = new HashMap<>();
      for (ProgramSequenceChord chord : sourceMaterial.getChords(programSequence)) {
        int validVoicings = sourceMaterial.getVoicings(chord).stream().map(V -> CsvUtils.split(V.getNotes()).size()).reduce(0, Integer::sum);
        if (!validVoicingsForPosition.containsKey(chord.getPosition()) || validVoicingsForPosition.get(chord.getPosition()) < validVoicings) {
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
    var cacheKey = String.format("%s__%s", programId, instrumentType);

    if (!rangeForChoice.containsKey(cacheKey)) {
      rangeForChoice.put(cacheKey, computeProgramRange(programId, instrumentType));
    }

    return rangeForChoice.get(cacheKey);
  }

  NoteRange computeProgramRange(UUID programId, InstrumentType instrumentType) {
    return NoteRange.ofStrings(
      sourceMaterial.getEvents(programId).stream()
        .filter(event -> sourceMaterial.getVoice(event).map(voice -> Objects.equals(voice.getType(), instrumentType)).orElse(false)
          && !Objects.equals(Note.of(event.getTones()).getPitchClass(), PitchClass.None))
        .flatMap(programSequencePatternEvent -> CsvUtils.split(programSequencePatternEvent.getTones()).stream())
        .collect(Collectors.toList()));
  }

  @Override
  public int getProgramRangeShiftOctaves(InstrumentType type, NoteRange sourceRange, NoteRange targetRange) throws NexusException {
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
  public int getProgramTargetShift(InstrumentType instrumentType, Chord fromChord, Chord toChord) {
    if (!fromChord.isPresent()) return 0;
    var cacheKey = String.format("%s__%s__%s", instrumentType.toString(), fromChord, toChord.toString());
    if (!targetShift.containsKey(cacheKey)) {
      if (instrumentType.equals(InstrumentType.Bass)) {
        targetShift.put(cacheKey, fromChord.getRoot().delta(toChord.getSlashRoot()));
      } else {
        targetShift.put(cacheKey, fromChord.getRoot().delta(toChord.getRoot()));
      }
    }

    return targetShift.get(cacheKey);
  }

  @Override
  public ProgramType getProgramType(ProgramVoice voice) throws NexusException {
    return sourceMaterial.getProgram(voice.getProgramId()).orElseThrow(() -> new NexusException("Could not get program!")).getType();
  }

  @Override
  public InstrumentType getProgramVoiceType(ProgramSequenceChordVoicing voicing) throws NexusException {
    return sourceMaterial.getProgramVoice(voicing.getProgramVoiceId()).orElseThrow(() -> new NexusException("Could not get voice!")).getType();
  }

  @Override
  public NoteRange getProgramVoicingNoteRange(InstrumentType type) {
    if (!voicingNoteRange.containsKey(type)) {
      voicingNoteRange.put(type, NoteRange.ofStrings(getChordVoicings().stream().filter(SegmentUtils::containsAnyValidNotes).filter(segmentChordVoicing -> Objects.equals(segmentChordVoicing.getType(), type.toString())).flatMap(segmentChordVoicing -> getNotes(segmentChordVoicing).stream()).collect(Collectors.toList())));
    }

    return voicingNoteRange.get(type);
  }

  @Override
  public Optional<ProgramSequence> getRandomlySelectedSequence(Program program) {
    var bag = MarbleBag.empty();
    sourceMaterial.getProgramSequences().stream().filter(s -> Objects.equals(s.getProgramId(), program.getId())).forEach(sequence -> bag.add(1, sequence.getId()));
    if (bag.isEmpty()) return Optional.empty();
    return sourceMaterial.getProgramSequence(bag.pick());
  }

  @Override
  public Optional<ProgramSequenceBinding> getRandomlySelectedSequenceBindingAtOffset(Program program, Integer offset) {
    var bag = MarbleBag.empty();
    for (ProgramSequenceBinding sequenceBinding : sourceMaterial.getBindingsAtOffset(program, offset))
      bag.add(1, sequenceBinding.getId());
    if (bag.isEmpty()) return Optional.empty();
    return sourceMaterial.getProgramSequenceBinding(bag.pick());
  }

  @Override
  public Optional<ProgramSequencePattern> getRandomlySelectedPatternOfSequenceByVoiceAndType(SegmentChoice choice) {
    var bag = MarbleBag.empty();
    sourceMaterial.getProgramSequencePatterns().stream().filter(pattern -> Objects.equals(pattern.getProgramSequenceId(), choice.getProgramSequenceId())).filter(pattern -> Objects.equals(pattern.getProgramVoiceId(), choice.getProgramVoiceId())).forEach(pattern -> bag.add(1, pattern.getId()));
    if (bag.isEmpty()) return Optional.empty();
    return sourceMaterial.getProgramSequencePattern(bag.pick());
  }

  @Override
  public Optional<Note> getRootNoteMidRange(String voicingNotes, Chord chord) {
    return rootNotesByVoicingAndChord.computeIfAbsent(String.format("%s_%s", voicingNotes, chord.getName()),
      (String key) -> NoteRange.ofStrings(CsvUtils.split(voicingNotes)).getNoteNearestMedian(chord.getSlashRoot()));
  }

  @Override
  public void putStickyBun(StickyBun bun) throws JsonProcessingException, NexusException {
    store.put(new SegmentMeta()
      .id(UUID.randomUUID())
      .segmentId(getSegment().getId())
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
    } catch (NexusException e) {
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
    return Objects.requireNonNull(getSegment().getDurationMicros());
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
    if (ValueUtils.isSet(choice.getProgramSequenceBindingId())) {
      var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());
      if (sequenceBinding.isPresent())
        return sourceMaterial.getProgramSequence(sequenceBinding.get().getProgramSequenceId());
    }

    if (!sequenceForChoice.containsKey(choice))
      getRandomlySelectedSequence(program.get()).ifPresent(programSequence -> sequenceForChoice.put(choice, programSequence));

    return Optional.of(sequenceForChoice.get(choice));
  }

  @Override
  public Integer getSequenceBindingOffsetForChoice(SegmentChoice choice) {
    if (ValueUtils.isEmpty(choice.getProgramSequenceBindingId())) return 0;
    var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());
    return sequenceBinding.map(ProgramSequenceBinding::getOffset).orElse(0);
  }

  @Override
  public String getTrackName(ProgramSequencePatternEvent event) {
    return sourceMaterial().getTrack(event).map(ProgramVoiceTrack::getName).orElse(UNKNOWN_KEY);
  }

  @Override
  public SegmentType getType() throws NexusException {
    if (ValueUtils.isEmpty(type)) type = computeType();
    return type;
  }

  @Override
  public Optional<SegmentChordVoicing> chooseVoicing(SegmentChord chord, InstrumentType type) {
    Collection<SegmentChordVoicing> voicings = store.readAll(segmentId, SegmentChordVoicing.class);
    return MarbleBag.quickPick(voicings.stream()
      .filter(SegmentUtils::containsAnyValidNotes)
      .filter(voicing -> Objects.equals(type.toString(), voicing.getType()))
      .filter(voicing -> Objects.equals(chord.getId(), voicing.getSegmentChordId()))
      .collect(Collectors.toList()));
  }

  @Override
  public boolean hasMoreSequenceBindingOffsets(SegmentChoice choice, int N) {
    if (ValueUtils.isEmpty(choice.getProgramSequenceBindingId())) return false;
    var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());

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
  public boolean isContinuationOfMacroProgram() throws NexusException {
    return SegmentType.CONTINUE.equals(getType()) || SegmentType.NEXT_MAIN.equals(getType());
  }

  @Override
  public boolean isDirectlyBound(Program program) {
    return boundProgramIds.contains(program.getId());
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
    return boundInstrumentIds.contains(instrument.getId());
  }

  @Override
  public boolean isDirectlyBound(InstrumentAudio instrumentAudio) {
    return boundInstrumentIds.contains(instrumentAudio.getInstrumentId());
  }

  @Override
  public Boolean isInitialSegment() {
    return 0L == getSegment().getId();
  }

  @Override
  public <N> N put(N entity, boolean force) throws NexusException {
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

    } catch (ManagerFatalException | ManagerExistenceException | ManagerPrivilegeException |
             ManagerValidationException e) {
      LOG.warn("Failed to update Segment", e);
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
    var offsets = sourceMaterial.getSequenceBindingsForProgram(macroProgram.getId()).stream()
      .map(ProgramSequenceBinding::getOffset)
      .collect(Collectors.toSet()).stream().sorted().toList();
    return offsets.size() > 1 ? offsets.get(1) : offsets.get(0);
  }

  @Override
  public MemeTaxonomy getMemeTaxonomy() {
    return templateConfig.getMemeTaxonomy();
  }

  @Override
  public double getTempo() throws NexusException {
    return getSegment().getTempo();
  }

  /**
   Get the choices of the current segment of the given type

   @param programType of choices to get
   @return choices of the current segment of the given type
   */
  private Optional<SegmentChoice> getChoiceOfType(ProgramType programType) {
    return getChoices().stream().filter(c -> Objects.equals(c.getProgramType(), programType)).findFirst();
  }

  /**
   Get the choices of the current segment of the given type

   @return choices of the current segment of the given type
   */
  private List<SegmentChoice> getBeatChoices() {
    return getChoices().stream().filter(c -> Objects.equals(c.getProgramType(), ProgramType.Beat)).toList();
  }

  /**
   Compute the lowest optimal range shift octaves

   @param sourceRange from
   @param targetRange to
   @return lowest optimal range shift octaves
   */
  private int computeLowestOptimalRangeShiftOctaves(NoteRange sourceRange, NoteRange targetRange) throws NexusException {
    var shiftOctave = 0; // search for optimal value
    var baselineDelta = 100; // optimal is the lowest possible integer zero or above
    for (var o = 10; o >= -10; o--) {
      int d = targetRange.getLow().orElseThrow(() -> new NexusException("can't get low end of target range")).delta(sourceRange.getLow().orElse(Note.atonal()).shiftOctave(o));
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
    String chainName = StringUtils.isNullOrEmpty(chain.getShipKey()) ? "chain" + NAME_SEPARATOR + chain.getId() : chain.getShipKey();
    String segmentName = String.valueOf(segment.getBeginAtChainMicros());
    return chainName + NAME_SEPARATOR + segmentName;
  }

  /**
   Format a message with the segmentId as prefix

   @param message to format
   @return formatted message with segmentId as prefix
   */
  private String formatLog(String message) {
    return String.format("[segId=%s] %s", getSegment().getId(), message);
  }

  /**
   Ensure the current segment has a storage key; if not, add a storage key to this Segment
   */
  private void ensureShipKey() throws NexusException {
    if (ValueUtils.isEmpty(getSegment().getStorageKey()) || getSegment().getStorageKey().isEmpty()) {
      var seg = getSegment();
      seg.setStorageKey(computeShipKey(store.readChain().orElseThrow(() -> new NexusException("No chain")), getSegment()));
      LOG.debug("[segId={}] Generated ship key {}", getSegment().getId(), getSegment().getStorageKey());
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
    var previousMainChoice = getPreviousMainChoice();

    if (previousMainChoice.isPresent() && hasOneMoreSequenceBindingOffset(previousMainChoice.get())
      && getTemplateConfig().getMainProgramLengthMaxDelta() > getPreviousSegmentDelta())
      return SegmentType.CONTINUE;

    // previous macro choice having at least two more patterns?
    var previousMacroChoice = getMacroChoiceOfPreviousSegment();

    if (previousMacroChoice.isPresent() && hasTwoMoreSequenceBindingOffsets(previousMacroChoice.get()))
      return SegmentType.NEXT_MAIN;

    return SegmentType.NEXT_MACRO;
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
   For a SegmentChoice, add memes from program, program sequence binding, and instrument if present https://www.pivotaltracker.com/story/show/181336704

   @param choice    to test for validity, and add its memes
   @param memeStack to use for validation
   @param force     whether to force the addition of this choice
   @return true if valid and adding memes was successful
   */
  private boolean isValidChoiceAndMemesHaveBeenAdded(SegmentChoice choice, MemeStack memeStack, boolean force) throws NexusException {
    Set<String> names = new HashSet<>();

    if (Objects.nonNull(choice.getProgramId()))
      sourceMaterial().getMemesForProgramId(choice.getProgramId())
        .forEach(meme -> names.add(StringUtils.toMeme(meme.getName())));

    if (Objects.nonNull(choice.getProgramSequenceBindingId()))
      sourceMaterial().getMemesForProgramSequenceBindingId(choice.getProgramSequenceBindingId())
        .forEach(meme -> names.add(StringUtils.toMeme(meme.getName())));

    if (Objects.nonNull(choice.getInstrumentId()))
      sourceMaterial().getMemesForInstrumentId(choice.getInstrumentId())
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
      segmentMeme.setSegmentId(getSegment().getId());
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
