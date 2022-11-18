// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import io.xj.hub.InstrumentConfig;
import io.xj.hub.ProgramConfig;
import io.xj.hub.TemplateConfig;
import io.xj.hub.client.HubClientException;
import io.xj.hub.client.HubContent;
import io.xj.hub.enums.ContentBindingType;
import io.xj.hub.enums.InstrumentMode;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.enums.ProgramType;
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
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.meme.MemeStack;
import io.xj.lib.music.AdjSymbol;
import io.xj.lib.music.Chord;
import io.xj.lib.music.Note;
import io.xj.lib.music.NoteRange;
import io.xj.lib.music.PitchClass;
import io.xj.lib.music.StickyBun;
import io.xj.lib.util.CSV;
import io.xj.lib.util.MarbleBag;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;
import io.xj.lib.util.Values;
import io.xj.nexus.NexusException;
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
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.Chains;
import io.xj.nexus.persistence.ManagerExistenceException;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.ManagerPrivilegeException;
import io.xj.nexus.persistence.ManagerValidationException;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.Segments;
import org.glassfish.jersey.internal.guava.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.sound.sampled.AudioFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
  private final HubContent sourceMaterial;
  private final int workBufferAheadSeconds;
  private final int workBufferBeforeSeconds;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final JsonProvider jsonProvider;
  private final Map<Double, Optional<SegmentChord>> chordAtPosition;
  private final Map<InstrumentType, NoteRange> voicingNoteRange;
  private final Map<SegmentChoice, ProgramSequence> sequenceForChoice;
  private final Map<String, InstrumentAudio> preferredAudios;
  private final Map<String, InstrumentConfig> instrumentConfigs;
  private final Map<String, InstrumentConfig> pickInstrumentConfigs;
  private final Map<String, Integer> rangeShiftOctave;
  private final Map<String, Integer> targetShift;
  private final Map<String, NoteRange> rangeForChoice;
  private final Map<String, Set<String>> preferredNotesByChordEvent;
  private final Map<String, Optional<Note>> rootNotesByVoicingAndChord;
  private final Map<UUID, Collection<ProgramSequenceChord>> completeChordsForProgramSequence;
  private final Map<UUID, List<SegmentChoiceArrangementPick>> picksForChoice;
  private final SegmentManager segmentManager;
  private final SegmentRetrospective retrospective;
  private final SegmentWorkbench workbench;
  private final Set<UUID> boundInstrumentIds;
  private final Set<UUID> boundProgramIds;
  private final String workTempFilePathPrefix;
  private final long startTime;
  private SegmentType type;
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private Optional<SegmentChoice> macroChoiceOfPreviousSegment;
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  private Optional<SegmentChoice> mainChoiceOfPreviousSegment;

  @Nullable
  private Double secondsPerBeat;

  @Nullable
  private Set<InstrumentType> distinctChordVoicingTypes;

  @AssistedInject
  public FabricatorImpl(
    @Assisted("sourceMaterial") HubContent sourceMaterial,
    @Assisted("segment") Segment segment,
    Environment env,
    ChainManager chainManager,
    FabricatorFactory fabricatorFactory,
    SegmentManager segmentManager,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    JsonProvider jsonProvider
  ) throws NexusException, FabricationFatalException {
    this.segmentManager = segmentManager;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.jsonProvider = jsonProvider;
    try {
      this.sourceMaterial = sourceMaterial;

      workTempFilePathPrefix = env.getTempFilePathPrefix();

      // caches
      chordAtPosition = Maps.newHashMap();
      completeChordsForProgramSequence = Maps.newHashMap();
      instrumentConfigs = Maps.newHashMap();
      pickInstrumentConfigs = Maps.newHashMap();
      picksForChoice = Maps.newHashMap();
      rangeForChoice = Maps.newHashMap();
      rangeShiftOctave = Maps.newHashMap();
      rootNotesByVoicingAndChord = Maps.newHashMap();
      sequenceForChoice = Maps.newHashMap();
      targetShift = Maps.newHashMap();
      voicingNoteRange = Maps.newHashMap();

      // time
      startTime = System.nanoTime();
      LOG.debug("[segId={}] StartTime {}ns since epoch zulu", segment.getId(), startTime);

      // read the chain, configs, and bindings
      chain = chainManager.readOne(segment.getChainId());
      templateConfig = new TemplateConfig(sourceMaterial.getTemplate());
      templateBindings = sourceMaterial.getTemplateBindings();
      boundProgramIds = Chains.targetIdsOfType(templateBindings, ContentBindingType.Program);
      boundInstrumentIds = Chains.targetIdsOfType(templateBindings, ContentBindingType.Instrument);
      LOG.debug("[segId={}] Chain {} configured with {} and bound to {} ", segment.getId(), chain.getId(), templateConfig, CSV.prettyFrom(templateBindings, "and"));

      // Buffer times from template
      workBufferAheadSeconds = templateConfig.getBufferAheadSeconds();
      workBufferBeforeSeconds = templateConfig.getBufferBeforeSeconds();

      // set up the segment retrospective
      retrospective = fabricatorFactory.loadRetrospective(segment, sourceMaterial);

      // digest previous picks for chord+event
      preferredNotesByChordEvent = computePreferredNotesByChordEvent();

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
  public void addMessage(SegmentMessageType messageType, String body) {
    try {
      var msg = new SegmentMessage();
      msg.setId(UUID.randomUUID());
      msg.setSegmentId(getSegment().getId());
      msg.setType(messageType);
      msg.setBody(body);
      put(msg);
    } catch (NexusException e) {
      LOG.error("Failed to add message!", e);
    }
  }

  @Override
  public void putMeta(String key, String value) throws NexusException {
    var msg = new SegmentMeta();
    msg.setId(UUID.randomUUID());
    msg.setSegmentId(getSegment().getId());
    msg.setKey(key);
    msg.setValue(value);
    put(msg);
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
  public <N> void delete(N entity) {
    workbench.delete(entity);
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
    return getArrangements().stream().filter(arrangement -> choiceIds.contains(arrangement.getSegmentChoiceId())).collect(Collectors.toList());
  }

  @Override
  public double getAudioVolume(SegmentChoiceArrangementPick pick) {
    return sourceMaterial().getInstrumentAudio(pick.getInstrumentAudioId()).stream().map(audio -> audio.getVolume() * sourceMaterial().getInstrument(audio.getInstrumentId()).orElseThrow().getVolume()).findAny().orElse(1.0f);
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
      return computeChainJson(segmentManager.readMany(ImmutableList.of(chain.getId())).stream().filter(segment -> Instant.parse(segment.getBeginAt()).isBefore(beforeThreshold) && Instant.parse(segment.getEndAt()).isAfter(afterThreshold)).collect(Collectors.toList()));

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
    if (Objects.isNull(distinctChordVoicingTypes)) {
      var mainChoice = getCurrentMainChoice();
      if (mainChoice.isEmpty()) return Set.of();
      var voicings = sourceMaterial.getProgramSequenceChordVoicings(mainChoice.get().getProgramId());
      distinctChordVoicingTypes = voicings.stream().flatMap(voicing -> {
        try {
          return Stream.of(getProgramVoiceType(voicing));
        } catch (NexusException e) {
          LOG.error("Failed to get distinct chord voicing type!", e);
          return Stream.empty();
        }
      }).collect(Collectors.toSet());
    }

    return distinctChordVoicingTypes;
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
  public InstrumentConfig getInstrumentConfig(SegmentChoiceArrangementPick pick) throws NexusException {
    if (!pickInstrumentConfigs.containsKey(pick.getId().toString()))
      pickInstrumentConfigs.put(pick.getId().toString(),
        getInstrumentConfig(sourceMaterial.getInstrument(sourceMaterial.getInstrumentAudio(pick.getInstrumentAudioId())
            .orElseThrow(() -> new NexusException("Failed to retrieve audio for pick")).getInstrumentId())
          .orElseThrow(() -> new NexusException("Failed to retrieve instrument for audio"))));
    return pickInstrumentConfigs.get(pick.getId().toString());

  }

  @Override
  public NoteRange getInstrumentRange(UUID instrumentId) {
    return NoteRange.ofNotes(sourceMaterial.getInstrumentAudios(instrumentId).stream()
      .map(InstrumentAudio::getTones)
      .map(Note::of)
      .toList());
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
        case INITIAL, NEXTMAIN, NEXTMACRO, PENDING -> Optional.empty();
        case CONTINUE -> retrospective.getChoices().stream().filter(choice -> Objects.equals(instrumentType.toString(), choice.getInstrumentType())).findFirst();
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
        case INITIAL, NEXTMAIN, NEXTMACRO, PENDING -> Optional.empty();
        case CONTINUE -> retrospective.getChoices().stream().filter(choice -> Objects.equals(instrumentType.toString(), choice.getInstrumentType()) && Objects.equals(instrumentMode.toString(), choice.getInstrumentMode())).findFirst();
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
        case CONTINUE -> retrospective.getChoices().stream().filter(choice -> Objects.equals(programType.toString(), choice.getProgramType())).findFirst();
      };

    } catch (Exception e) {
      LOG.warn(formatLog(String.format("Could not get previous choice for programType=%s", programType)), e);
      return Optional.empty();
    }
  }

  @Override
  public String getKeyByVoiceTrack(SegmentChoiceArrangementPick pick) {
    String key = sourceMaterial().getProgramSequencePatternEvent(pick.getProgramSequencePatternEventId()).flatMap(event -> sourceMaterial().getTrack(event).map(ProgramVoiceTrack::getProgramVoiceId)).map(UUID::toString).orElse(UNKNOWN_KEY);

    return String.format(KEY_VOICE_TRACK_TEMPLATE, key, pick.getEvent());
  }

  @Override
  public Chord getKeyForChoice(SegmentChoice choice) throws NexusException {
    Optional<Program> program = getProgram(choice);
    if (Values.isSet(choice.getProgramSequenceBindingId())) {
      var sequence = getSequence(choice);
      if (sequence.isPresent() && !Strings.isNullOrEmpty(sequence.get().getKey()))
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
        sourceMaterial.getProgram(getCurrentMainChoice().orElseThrow(() -> new NexusException("No current main choice!")).getProgramId())
          .orElseThrow(() -> new NexusException("Failed to retrieve current main program!")));

    } catch (ValueException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public Program getCurrentMainProgram() throws NexusException {
    return
      sourceMaterial.getProgram(getCurrentMainChoice().orElseThrow(() -> new NexusException("No current main choice!")).getProgramId())
        .orElseThrow(() -> new NexusException("Failed to retrieve current main program!"));
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
    try {
      var previousMacroChoice = getMacroChoiceOfPreviousSegment().orElseThrow(NexusException::new);
      var previousSequenceBinding = sourceMaterial().getProgramSequenceBinding(previousMacroChoice.getProgramSequenceBindingId()).orElseThrow(NexusException::new);

      var nextSequenceBinding = sourceMaterial().getBindingsAtOffset(previousMacroChoice.getProgramId(), previousSequenceBinding.getOffset() + 1);

      return MemeIsometry.of(templateConfig.getMemeTaxonomy(), Streams.concat(sourceMaterial.getProgramMemes(previousMacroChoice.getProgramId()).stream().map(ProgramMeme::getName), nextSequenceBinding.stream().flatMap(programSequenceBinding -> sourceMaterial.getMemesForProgramSequenceBindingId(programSequenceBinding.getId()).stream().map(ProgramSequenceBindingMeme::getName))).collect(Collectors.toList()));

    } catch (NexusException e) {
      LOG.warn("Could not get meme isometry of next sequence in previous macro", e);
      return MemeIsometry.none();
    }
  }

  @Override
  public MemeIsometry getMemeIsometryOfSegment() {
    return MemeIsometry.of(templateConfig.getMemeTaxonomy(), Entities.namesOf(workbench.getSegmentMemes()));
  }

  @Override
  public Integer getNextSequenceBindingOffset(SegmentChoice choice) {
    if (Values.isEmpty(choice.getProgramSequenceBindingId())) return 0;

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
    return new ArrayList<>(CSV.split(voicing.getNotes()));
  }

  @Override
  public AudioFormat getOutputAudioFormat() {
    return new AudioFormat(templateConfig.getOutputEncoding(), templateConfig.getOutputFrameRate(), templateConfig.getOutputSampleBits(), templateConfig.getOutputChannels(), templateConfig.getOutputChannels() * templateConfig.getOutputSampleBits() / 8, templateConfig.getOutputFrameRate(), false);
  }

  public Optional<SegmentChoice> getChoice(SegmentChoiceArrangement pick) {
    return workbench.getSegmentChoices().stream().filter(choice -> choice.getId().equals(pick.getSegmentChoiceId())).findFirst();
  }

  public Optional<SegmentChoiceArrangement> getArrangement(SegmentChoiceArrangementPick pick) {
    return workbench.getSegmentChoiceArrangements().stream().filter(choice -> choice.getId().equals(pick.getSegmentChoiceArrangementId())).findFirst();
  }

  @Override
  public Collection<InstrumentAudio> getPickedAudios() {
    Collection<InstrumentAudio> audios = Lists.newArrayList();
    for (SegmentChoiceArrangementPick pick : workbench.getSegmentChoiceArrangementPicks())
      sourceMaterial.getInstrumentAudio(pick.getInstrumentAudioId()).ifPresent(audios::add);
    return audios;
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPicks() {
    return workbench.getSegmentChoiceArrangementPicks();
  }

  @Override
  public List<SegmentChoiceArrangementPick> getPicks(SegmentChoice choice) {
    if (!picksForChoice.containsKey(choice.getId())) {
      var arrangementIds = workbench.getSegmentChoiceArrangements().stream().filter(a -> a.getSegmentChoiceId().equals(choice.getId())).map(SegmentChoiceArrangement::getId).toList();
      picksForChoice.put(choice.getId(), workbench.getSegmentChoiceArrangementPicks().stream()
        .filter(p -> arrangementIds.contains(p.getSegmentChoiceArrangementId()))
        .sorted(Comparator.comparing(SegmentChoiceArrangementPick::getStart)).toList());
    }
    return picksForChoice.get(choice.getId());
  }

  @Override
  public Optional<InstrumentAudio> getPreferredAudio(String parentIdent, String ident) {
    String key = String.format(KEY_VOICE_NOTE_TEMPLATE, parentIdent, ident);

    if (preferredAudios.containsKey(key)) return Optional.of(preferredAudios.get(key));

    return Optional.empty();
  }

  @Override
  public Optional<Set<String>> getPreferredNotes(String parentIdent, String chordName) {
    try {
      var key = computeNoteChordEventKey(parentIdent, chordName);
      if (preferredNotesByChordEvent.containsKey(key))
        return Optional.of(new HashSet<>(preferredNotesByChordEvent.get(key)));

    } catch (NexusException | EntityStoreException e) {
      LOG.debug("No notes cached for previous event and chord id", e);
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
        int validVoicings = sourceMaterial.getVoicings(chord).stream().map(V -> CSV.split(V.getNotes()).size()).reduce(0, Integer::sum);
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
    var key = String.format("%s__%s", programId, instrumentType);

    if (!rangeForChoice.containsKey(key)) {
      rangeForChoice.put(key, computeProgramRange(programId, instrumentType));
    }

    return rangeForChoice.get(key);
  }

  private NoteRange computeProgramRange(UUID programId, InstrumentType instrumentType) {
    return NoteRange.ofStrings(
      sourceMaterial.getEvents(programId).stream()
        .filter(event -> sourceMaterial.getVoice(event).map(voice -> Objects.equals(voice.getType(), instrumentType)).orElse(false)
          && !Objects.equals(Note.of(event.getTones()).getPitchClass(), PitchClass.None))
        .flatMap(programSequencePatternEvent -> CSV.split(programSequencePatternEvent.getTones()).stream())
        .collect(Collectors.toList()));
  }

  @Override
  public int getProgramRangeShiftOctaves(InstrumentType type, NoteRange sourceRange, NoteRange targetRange) throws NexusException {
    var key = String.format("%s__%s__%s", type, sourceRange.toString(AdjSymbol.None), targetRange.toString(AdjSymbol.None));

    if (!rangeShiftOctave.containsKey(key)) switch (type) {

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
  public int getProgramTargetShift(Chord fromChord, Chord toChord) {
    if (!fromChord.isPresent()) return 0;
    var key = String.format("%s__%s", fromChord, toChord.toString());
    if (!targetShift.containsKey(key)) targetShift.put(key, fromChord.getRoot().delta(toChord.getSlashRoot()));

    return targetShift.get(key);
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
      voicingNoteRange.put(type, NoteRange.ofStrings(workbench.getSegmentChordVoicings().stream().filter(Segments::containsAnyValidNotes).filter(segmentChordVoicing -> Objects.equals(segmentChordVoicing.getType(), type.toString())).flatMap(segmentChordVoicing -> getNotes(segmentChordVoicing).stream()).collect(Collectors.toList())));
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
  public Optional<ProgramVoice> getRandomlySelectedVoiceForProgramId(UUID programId, Collection<UUID> excludeVoiceIds) {
    var bag = MarbleBag.empty();
    for (ProgramVoice sequenceBinding : sourceMaterial.getProgramVoices().stream().filter(programVoice -> Objects.equals(programId, programVoice.getProgramId()) && !excludeVoiceIds.contains(programVoice.getId())).toList())
      bag.add(1, sequenceBinding.getId());
    if (bag.isEmpty()) return Optional.empty();
    return sourceMaterial.getProgramVoice(bag.pick());
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
      (String key) -> NoteRange.ofStrings(CSV.split(voicingNotes)).getNoteNearestMedian(chord.getSlashRoot()));
  }

  @Override
  public Optional<StickyBun> getStickyBun(UUID eventId) {
    if (!templateConfig.isStickyBunEnabled()) return Optional.empty();
    //
    var currentMeta = workbench.getSegmentMeta(StickyBun.computeMetaKey(eventId));
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
    var bun = new StickyBun(eventId, CSV.split(sourceMaterial.getProgramSequencePatternEvent(eventId).orElseThrow().getTones()).size());
    try {
      workbench.put(new SegmentMeta()
        .id(UUID.randomUUID())
        .segmentId(getSegment().getId())
        .key(bun.computeMetaKey())
        .value(jsonProvider.getMapper().writeValueAsString(bun)));
    } catch (NexusException e) {
      addErrorMessage(String.format("Failed to put StickyBun for Event[%s] because %s", eventId, e.getMessage()));
    } catch (JsonProcessingException e) {
      addErrorMessage(String.format("Failed to serialize segment meta value StickyBun JSON for Event[%s]", eventId));
    }
    return Optional.of(bun);
  }

  @Override
  public Double getSecondsAtPosition(double p) throws NexusException {
    return computeSecondsPerBeat() * p;
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
  public List<SegmentChord> getSegmentChords() {
    return workbench.getSegmentChords();
  }

  @Override
  public Collection<SegmentMeme> getSegmentMemes() {
    return workbench.getSegmentMemes();
  }

  @Override
  public String getSegmentJson() throws NexusException {
    try {
      return jsonapiPayloadFactory.serialize(
        jsonapiPayloadFactory.newJsonapiPayload()
          .setDataOne(jsonapiPayloadFactory.toPayloadObject(workbench.getSegment()))
          .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentChoiceArrangementPicks()))
          .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentChoiceArrangements()))
          .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentChoices()))
          .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentChords()))
          .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentMemes()))
          .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentMessages()))
          .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentMetas()))
      );

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
      getRandomlySelectedSequence(program.get()).ifPresent(programSequence -> sequenceForChoice.put(choice, programSequence));

    return Optional.of(sequenceForChoice.get(choice));
  }

  @Override
  public Integer getSequenceBindingOffsetForChoice(SegmentChoice choice) {
    if (Values.isEmpty(choice.getProgramSequenceBindingId())) return 0;
    var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());
    return sequenceBinding.map(ProgramSequenceBinding::getOffset).orElse(0);
  }

  @Override
  public String getTrackName(ProgramSequencePatternEvent event) {
    return sourceMaterial().getTrack(event).map(ProgramVoiceTrack::getName).orElse(UNKNOWN_KEY);
  }

  @Override
  public String getTrackName(SegmentChoiceArrangementPick pick) throws NexusException {
    return sourceMaterial().getTrack(sourceMaterial.getProgramSequencePatternEvent(pick.getProgramSequencePatternEventId()).orElseThrow(() -> new NexusException("Failed to get event from source material for pick!"))).map(ProgramVoiceTrack::getName).orElse(UNKNOWN_KEY);
  }

  @Override
  public SegmentType getType() throws NexusException {
    if (Values.isEmpty(type)) type = computeType();
    return type;
  }

  @Override
  public Optional<SegmentChordVoicing> getVoicing(SegmentChord chord, InstrumentType type) {
    Collection<SegmentChordVoicing> voicings = workbench.getSegmentChordVoicings();
    return voicings.stream().filter(Segments::containsAnyValidNotes).filter(voicing -> Objects.equals(type.toString(), voicing.getType())).filter(voicing -> Objects.equals(chord.getId(), voicing.getSegmentChordId())).findAny();
  }

  @Override
  public boolean hasMoreSequenceBindingOffsets(SegmentChoice choice, int N) {
    if (Values.isEmpty(choice.getProgramSequenceBindingId())) return false;
    var sequenceBinding = sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());

    if (sequenceBinding.isEmpty()) return false;
    List<Integer> avlOfs = ImmutableList.copyOf(sourceMaterial.getAvailableOffsets(sequenceBinding.get()));

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
    return SegmentType.CONTINUE.equals(getType()) || SegmentType.NEXTMAIN.equals(getType());
  }

  @Override
  public boolean isDirectlyBound(Program program) {
    return boundProgramIds.contains(program.getId());
  }

  @Override
  public boolean isOneShot(Instrument instrument, String trackName) throws NexusException {
    return isOneShot(instrument) && !getInstrumentConfig(instrument).getOneShotObserveLengthOfEvents().contains(trackName);
  }

  @Override
  public boolean isOneShot(Instrument instrument) throws NexusException {
    return getInstrumentConfig(instrument).isOneShot();
  }

  @Override
  public boolean isOneShotCutoffEnabled(Instrument instrument) throws NexusException {
    return getInstrumentConfig(instrument).isOneShotCutoffEnabled();
  }

  @Override
  public Integer getAttackMillis(SegmentChoiceArrangementPick pick) throws NexusException {
    return getInstrumentConfig(pick).getAttackMillis();
  }

  @Override
  public Integer getReleaseMillis(SegmentChoiceArrangementPick pick) throws NexusException {
    return getInstrumentConfig(pick).getReleaseMillis();
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
    return 0L == workbench.getSegment().getOffset();
  }

  @Override
  public <N> N put(N entity) throws NexusException {
    // For a SegmentChoice, add memes from program, program sequence binding, and instrument if present
    if (SegmentChoice.class.equals(entity.getClass()))
      if (!addMemes((SegmentChoice) entity))
        return entity;

    workbench.put(entity);

    return entity;
  }

  @Override
  public void putNotesPickedForChord(ProgramSequencePatternEvent event, String chordName, Set<String> notes) {
    try {
      preferredNotesByChordEvent.put(computeNoteChordEventKey(event.getId().toString(), chordName), new HashSet<>(notes));
    } catch (NexusException | EntityStoreException e) {
      LOG.warn("Can't cache notes picked for chord", e);
    }
  }

  @Override
  public void putPreferredAudio(String parentIdent, String ident, InstrumentAudio instrumentAudio) {
    String key = String.format(KEY_VOICE_NOTE_TEMPLATE, parentIdent, ident);

    preferredAudios.put(key, instrumentAudio);
  }

  @Override
  public void putPreferredNotes(String parentIdent, String ident, Set<String> instrumentNotes) {
    try {
      preferredNotesByChordEvent.put(computeNoteChordEventKey(parentIdent, ident), instrumentNotes);
    } catch (NexusException | EntityStoreException e) {
      LOG.warn("Can't cache preferred notes", e);
    }
  }

  @Override
  public void putReport(String key, Object value) {
    workbench.putReport(key, value);
  }

  @Override
  public void putSegment(Segment segment) {
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
  public HubContent sourceMaterial() {
    return sourceMaterial;
  }

  /**
   @return Chain base key
   */
  private String computeChainBaseKey() {
    return Strings.isNullOrEmpty(getChain().getShipKey()) ? String.format("chain-%s", chain.getId()) : getChain().getShipKey();
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
      int d = targetRange.getLow().orElseThrow(() -> new NexusException("can't get low end of target range")).delta(sourceRange.getLow().orElse(Note.atonal()).shiftOctave(o));
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
    if (sourceRange.getLow().isEmpty() || sourceRange.getHigh().isEmpty() || targetRange.getLow().isEmpty() || targetRange.getHigh().isEmpty())
      return 0;
    var shiftOctave = 0; // search for optimal value
    var baselineDelta = 100; // optimal is the lowest possible integer zero or above
    for (var o = 10; o >= -10; o--) {
      int dLow = targetRange.getLow().orElseThrow(() -> new NexusException("Can't find low end of target range")).delta(sourceRange.getLow().orElseThrow(() -> new NexusException("Can't find low end of source range")).shiftOctave(o));
      int dHigh = targetRange.getHigh().orElseThrow(() -> new NexusException("Can't find high end of target range")).delta(sourceRange.getHigh().orElseThrow(() -> new NexusException("Can't find high end of source range")).shiftOctave(o));
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
    String chainName = Strings.isNullOrEmpty(chain.getShipKey()) ? "chain" + NAME_SEPARATOR + chain.getId() : chain.getShipKey();
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

   @param parentIdent to get key for
   @param chordName   to get key for
   @return key for chord + event
   */
  private String computeNoteChordEventKey(@Nullable String parentIdent, @Nullable String chordName) throws NexusException, EntityStoreException {
    return String.format("%s__%s", Strings.isNullOrEmpty(chordName) ? UNKNOWN_KEY : chordName, Objects.isNull(parentIdent) ? UNKNOWN_KEY : parentIdent);
  }

  /**
   Compute and cache the # of seconds per beat

   @return seconds per beat
   */
  private Double computeSecondsPerBeat() throws NexusException {
    if (Objects.isNull(secondsPerBeat))
      secondsPerBeat = 60d / getCurrentMainProgram().getTempo();
    return secondsPerBeat;
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
    var previousMainChoice = getPreviousMainChoice();

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
  private Map<String, Set<String>> computePreferredNotesByChordEvent() {
    Map<String, Set<String>> notes = Maps.newHashMap();

    retrospective.getPicks().forEach(pick -> {
      try {
        var key = computeNoteChordEventKey(
          Objects.nonNull(pick.getProgramSequencePatternEventId()) ? pick.getProgramSequencePatternEventId().toString() : UNKNOWN_KEY,
          Objects.nonNull(pick.getSegmentChordVoicingId()) ? pick.getSegmentChordVoicingId().toString() : UNKNOWN_KEY
        );
        if (!notes.containsKey(key)) notes.put(key, Sets.newHashSet());
        notes.get(key).add(pick.getTones());

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

  /**
   For a SegmentChoice, add memes from program, program sequence binding, and instrument if present https://www.pivotaltracker.com/story/show/181336704

   @param choice for which to add memes
   @return true if adding memes was successful
   */
  private boolean addMemes(SegmentChoice choice) throws NexusException {
    Set<String> names = Sets.newHashSet();

    if (Objects.nonNull(choice.getProgramId()))
      sourceMaterial().getMemesForProgramId(choice.getProgramId())
        .forEach(meme -> names.add(Text.toMeme(meme.getName())));

    if (Objects.nonNull(choice.getProgramSequenceBindingId()))
      sourceMaterial().getMemesForProgramSequenceBindingId(choice.getProgramSequenceBindingId())
        .forEach(meme -> names.add(Text.toMeme(meme.getName())));

    if (Objects.nonNull(choice.getInstrumentId()))
      sourceMaterial().getMemesForInstrumentId(choice.getInstrumentId())
        .forEach(meme -> names.add(Text.toMeme(meme.getName())));

    var memeStack = MemeStack.from(templateConfig.getMemeTaxonomy(),
      getSegmentMemes().stream().map(SegmentMeme::toString).toList());

    if (!memeStack.isAllowed(names)) {
      addMessage(SegmentMessageType.ERROR, String.format("Refused to add Choice[%s] because adding Memes[%s] to MemeStack[%s] would result in an invalid meme stack theorem!",
        memeStack.getConstellation(),
        Segments.describe(choice), CSV.join(names.stream().toList())));
      return false;
    }

    for (String name : names) {
      var segMeme = new SegmentMeme();
      segMeme.setId(UUID.randomUUID());
      segMeme.setSegmentId(getSegment().getId());
      segMeme.setName(name);
      put(segMeme);
    }

    return true;
  }
}
