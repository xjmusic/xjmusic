// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Streams;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.typesafe.config.Config;
import io.xj.api.Chain;
import io.xj.api.ContentBindingType;
import io.xj.api.Instrument;
import io.xj.api.InstrumentAudio;
import io.xj.api.InstrumentType;
import io.xj.api.Program;
import io.xj.api.ProgramMeme;
import io.xj.api.ProgramSequence;
import io.xj.api.ProgramSequenceBinding;
import io.xj.api.ProgramSequenceBindingMeme;
import io.xj.api.ProgramSequenceChord;
import io.xj.api.ProgramSequenceChordVoicing;
import io.xj.api.ProgramSequencePattern;
import io.xj.api.ProgramSequencePatternEvent;
import io.xj.api.ProgramSequencePatternType;
import io.xj.api.ProgramType;
import io.xj.api.ProgramVoice;
import io.xj.api.ProgramVoiceTrack;
import io.xj.api.Segment;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentChoiceArrangement;
import io.xj.api.SegmentChoiceArrangementPick;
import io.xj.api.SegmentChord;
import io.xj.api.SegmentChordVoicing;
import io.xj.api.SegmentMeme;
import io.xj.api.SegmentMessage;
import io.xj.api.SegmentMessageType;
import io.xj.api.SegmentType;
import io.xj.api.TemplateBinding;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityStoreException;
import io.xj.lib.entity.common.InstrumentConfig;
import io.xj.lib.entity.common.ProgramConfig;
import io.xj.lib.entity.common.TemplateConfig;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.music.AdjSymbol;
import io.xj.lib.music.Chord;
import io.xj.lib.music.Key;
import io.xj.lib.music.Note;
import io.xj.lib.music.NoteRange;
import io.xj.lib.music.PitchClass;
import io.xj.lib.music.Voicing;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Chance;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.nexus.NexusException;
import io.xj.nexus.dao.ChainDAO;
import io.xj.nexus.dao.Chains;
import io.xj.nexus.dao.SegmentDAO;
import io.xj.nexus.dao.Segments;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.dao.exception.DAOValidationException;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.hub_client.client.HubClientException;
import io.xj.nexus.hub_client.client.HubContent;
import org.glassfish.jersey.internal.guava.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.text.DecimalFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
class FabricatorImpl implements Fabricator {
  private static final double MICROS_PER_SECOND = 1000000.0F;
  private static final double NANOS_PER_SECOND = 1000.0F * MICROS_PER_SECOND;
  private static final String KEY_VOICE_NOTE_TEMPLATE = "voice-%s_note-%s";
  private static final String KEY_VOICE_TRACK_TEMPLATE = "voice-%s_track-%s";
  private static final String NAME_SEPARATOR = "-";
  private static final String UNKNOWN_KEY = "unknown";
  private final Logger LOG = LoggerFactory.getLogger(FabricatorImpl.class);
  private final Chain chain;
  private final TemplateConfig templateConfig;
  private final Collection<TemplateBinding> templateBindings;
  private final Config config;
  private final DecimalFormat segmentNameFormat;
  private final FabricatorFactory fabricatorFactory;
  private final FileStoreProvider fileStoreProvider;
  private final HubClientAccess access;
  private final HubContent sourceMaterial;
  private final int workBufferAheadSeconds;
  private final int workBufferBeforeSeconds;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final long startTime;
  private final Map<Double, Optional<SegmentChord>> chordAtPosition;
  private final Map<InstrumentType, NoteRange> voicingNoteRange;
  private final Map<SegmentChoice, ProgramSequence> sequenceForChoice = Maps.newHashMap();
  private final Map<UUID, Collection<ProgramSequenceChord>> completeChordsForProgramSequence;
  private final Map<String, Integer> rangeShiftOctave;
  private final Map<String, Integer> targetShift;
  private final Map<String, NoteRange> rangeForChoice;
  private final Map<String, Set<String>> preferredNotes;
  private final SegmentDAO segmentDAO;
  private final SegmentRetrospective retrospective;
  private final SegmentWorkbench workbench;
  private final Set<UUID> boundInstrumentIds;
  private final Set<UUID> boundProgramIds;
  private final String workTempFilePathPrefix;
  private final Map<String, InstrumentAudio> preferredAudios;
  private SegmentType type;

  @AssistedInject
  public FabricatorImpl(
    @Assisted("access") HubClientAccess access,
    @Assisted("sourceMaterial") HubContent sourceMaterial,
    @Assisted("segment") Segment segment,
    Config config,
    Environment env,
    ChainDAO chainDAO,
    FileStoreProvider fileStoreProvider,
    FabricatorFactory fabricatorFactory,
    SegmentDAO segmentDAO,
    JsonapiPayloadFactory jsonapiPayloadFactory
  ) throws NexusException {
    this.segmentDAO = segmentDAO;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    try {
      // FUTURE: [#165815496] Chain fabrication access control
      this.access = access;
      LOG.debug("[segId={}] HubClientAccess {}", segment.getId(), access);

      this.fileStoreProvider = fileStoreProvider;
      this.fabricatorFactory = fabricatorFactory;
      this.sourceMaterial = sourceMaterial;

      this.config = config;
      workTempFilePathPrefix = env.getTempFilePathPrefix();
      segmentNameFormat = new DecimalFormat(config.getString("work.segmentNameFormat"));
      workBufferAheadSeconds = config.getInt("work.bufferAheadSeconds");
      workBufferBeforeSeconds = config.getInt("work.bufferBeforeSeconds");

      // caches
      voicingNoteRange = Maps.newHashMap();
      rangeForChoice = Maps.newHashMap();
      rangeShiftOctave = Maps.newHashMap();
      targetShift = Maps.newHashMap();
      chordAtPosition = Maps.newHashMap();
      completeChordsForProgramSequence = Maps.newHashMap();

      // time
      startTime = System.nanoTime();
      LOG.debug("[segId={}] StartTime {}ns since epoch zulu", segment.getId(), startTime);

      // read the chain, configs, and bindings
      chain = chainDAO.readOne(access, segment.getChainId());
      templateConfig = new TemplateConfig(sourceMaterial.getTemplate(), config);
      templateBindings = sourceMaterial.getAllTemplateBindings();
      boundProgramIds = Chains.targetIdsOfType(templateBindings, ContentBindingType.PROGRAM);
      boundInstrumentIds = Chains.targetIdsOfType(templateBindings, ContentBindingType.INSTRUMENT);
      LOG.debug("[segId={}] Chain {} configured with {} and bound to {} ", segment.getId(), chain.getId(),
        templateConfig,
        CSV.prettyFrom(templateBindings, "and"));

      // set up the segment retrospective
      retrospective = fabricatorFactory.loadRetrospective(access, segment, sourceMaterial);

      // digest previous picks
      preferredNotes = computePreferredNotes();

      // digest previous instrument audio
      preferredAudios = computePreferredInstrumentAudio();

      // get the current segment on the workbench
      workbench = fabricatorFactory.setupWorkbench(access, chain, segment);

      // final pre-flight check
      ensureStorageKey();

    } catch (DAOFatalException | DAOExistenceException | DAOPrivilegeException | ValueException | HubClientException e) {
      LOG.error("Failed to instantiate Fabricator!", e);
      throw new NexusException("Failed to instantiate Fabricator!", e);
    }
  }

  @Override
  public <N> N add(N entity) throws NexusException {
    return workbench.add(entity);
  }

  @Override
  public Program addMemes(Program p) throws NexusException {
    for (ProgramMeme meme : sourceMaterial().getMemes(p))
      add(new SegmentMeme()
        .id(UUID.randomUUID())
        .segmentId(getSegment().getId())
        .name(Text.toMeme(meme.getName())));
    return p;
  }

  @Override
  public ProgramSequenceBinding addMemes(ProgramSequenceBinding psb) throws NexusException {
    for (ProgramSequenceBindingMeme meme : sourceMaterial().getMemes(psb))
      add(new SegmentMeme()
        .id(UUID.randomUUID())
        .segmentId(getSegment().getId())
        .name(Text.toMeme(meme.getName())));
    return psb;
  }

  @Override
  public void addMessage(SegmentMessageType messageType, String body) throws NexusException {
    add(new SegmentMessage()
      .id(UUID.randomUUID())
      .segmentId(getSegment().getId())
      .type(messageType)
      .body(body));
  }

  @Override
  public void addMessageError(String body) throws NexusException {
    addMessage(SegmentMessageType.ERROR, body);
  }

  @Override
  public void addMessageInfo(String body) throws NexusException {
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
  public HubClientAccess getAccess() {
    return access;
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements() {
    return workbench.getSegmentArrangements();
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
      .orElse(1.0);
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
  public String getChainMetadataFullJson() throws NexusException {
    try {
      return computeChainMetadataJson(segmentDAO.readMany(access, ImmutableList.of(chain.getId())));

    } catch (DAOPrivilegeException | DAOFatalException | DAOExistenceException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public String getChainMetadataFullKey() {
    return Chains.getStorageKey(Chains.getFullKey(computeChainBaseKey()), FileStoreProvider.EXTENSION_JSON);
  }

  @Override
  public String getChainMetadataJson() throws NexusException {
    try {
      var now = Instant.now();
      var beforeThreshold = now.plusSeconds(workBufferAheadSeconds);
      var afterThreshold = now.minusSeconds(workBufferBeforeSeconds);
      return computeChainMetadataJson(segmentDAO.readMany(access, ImmutableList.of(chain.getId())).stream()
        .filter(segment ->
          Instant.parse(segment.getBeginAt()).isBefore(beforeThreshold)
            && Instant.parse(segment.getEndAt()).isAfter(afterThreshold))
        .collect(Collectors.toList()));

    } catch (DAOPrivilegeException | DAOFatalException | DAOExistenceException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public String getChainMetadataKey() {
    return Chains.getStorageKey(computeChainBaseKey(), FileStoreProvider.EXTENSION_JSON);
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
    return workbench.getChoiceOfType(ProgramType.MAIN);
  }

  @Override
  public Collection<SegmentChoice> getCurrentDetailChoices() {
    return workbench.getChoicesOfType(ProgramType.RHYTHM);
  }

  @Override
  public Optional<SegmentChoice> getCurrentRhythmChoice() {
    return workbench.getChoiceOfType(ProgramType.RHYTHM);
  }

  @Override
  public List<InstrumentType> getDistinctChordVoicingTypes() {
    var mainChoice = getCurrentMainChoice();
    if (mainChoice.isEmpty()) return ImmutableList.of();
    var voicings = sourceMaterial
      .getProgramSequenceChordVoicings(mainChoice.get().getProgramId());
    return voicings.stream()
      .map(ProgramSequenceChordVoicing::getType)
      .distinct()
      .collect(Collectors.toList());
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
      return new InstrumentConfig(instrument, config);
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
          .filter(choice -> Objects.equals(instrumentType, choice.getInstrumentType()))
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
          .filter(choice -> Objects.equals(programType, choice.getProgramType()))
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

    return String.format(KEY_VOICE_TRACK_TEMPLATE, key, pick.getName());
  }

  @Override
  public Key getKeyForChoice(SegmentChoice choice) throws NexusException {
    Optional<Program> program = getProgram(choice);
    if (Value.isSet(choice.getProgramSequenceBindingId())) {
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
    return retrospective.getPreviousChoiceOfType(ProgramType.MACRO);
  }

  @Override
  public Optional<SegmentChoice> getMainChoiceOfPreviousSegment() {
    return retrospective.getPreviousChoiceOfType(ProgramType.MAIN);
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
    if (Value.isEmpty(choice.getProgramSequenceBindingId()))
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
      return new ProgramConfig(program, config);
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

        case BASS:
          rangeShiftOctave.put(key, computeLowestOptimalRangeShiftOctaves(sourceRange, targetRange));
          break;

        case DRUM:
          return 0;

        case PAD:
        case STICKY:
        case STRIPE:
        case STAB:
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
        .filter(Voicing::containsAnyValidNotes)
        .filter(segmentChordVoicing -> Objects.equals(segmentChordVoicing.getType(), type))
        .flatMap(segmentChordVoicing -> getNotes(segmentChordVoicing).stream())
        .collect(Collectors.toList())));
    }

    return voicingNoteRange.get(type);
  }

  @Override
  public Optional<ProgramSequence> getRandomlySelectedSequence(Program program) {
    EntityScorePicker<ProgramSequence> entityScorePicker = new EntityScorePicker<>();
    sourceMaterial.getAllProgramSequences().stream()
      .filter(s -> Objects.equals(s.getProgramId(), program.getId()))
      .forEach(sequence -> entityScorePicker.add(sequence, Chance.normallyAround(0.0, 1.0)));
    return entityScorePicker.getTop();

  }

  @Override
  public Optional<ProgramSequenceBinding> getRandomlySelectedSequenceBindingAtOffset(Program program, Integer offset) {
    EntityScorePicker<ProgramSequenceBinding> entityScorePicker = new EntityScorePicker<>();
    for (ProgramSequenceBinding sequenceBinding : sourceMaterial.getProgramSequenceBindingsAtOffset(program, offset))
      entityScorePicker.add(sequenceBinding, Chance.normallyAround(0.0, 1.0));

    return entityScorePicker.getTop();
  }

  @Override
  public Optional<ProgramVoice> getRandomlySelectedVoiceForProgramId(UUID programId, Collection<UUID> excludeVoiceIds) {
    EntityScorePicker<ProgramVoice> entityScorePicker = new EntityScorePicker<>();
    for (ProgramVoice sequenceBinding : sourceMaterial.getAllProgramVoices()
      .stream().filter(programVoice -> Objects.equals(programId, programVoice.getProgramId())
        && !excludeVoiceIds.contains(programVoice.getId()))
      .collect(Collectors.toList()))
      entityScorePicker.add(sequenceBinding, Chance.normallyAround(0.0, 1.0));

    return entityScorePicker.getTop();
  }

  @Override
  public Optional<ProgramSequencePattern> getRandomlySelectedPatternOfSequenceByVoiceAndType(SegmentChoice choice, ProgramSequencePatternType patternType) {
    EntityScorePicker<ProgramSequencePattern> rank = new EntityScorePicker<>();
    sourceMaterial.getAllProgramSequencePatterns().stream()
      .filter(pattern -> Objects.equals(pattern.getProgramSequenceId(), choice.getProgramSequenceId()))
      .filter(pattern -> Objects.equals(pattern.getProgramVoiceId(), choice.getProgramVoiceId()))
      .filter(pattern -> pattern.getType() == patternType)
      .forEach(pattern ->
        rank.add(pattern, Chance.normallyAround(0.0, 1.0)));
    if (Objects.equals(0, rank.size()))
      return Optional.empty();
    return rank.getTop();
  }

  @Override
  public Double getSecondsAtPosition(double p) throws NexusException {
    return buildTimeComputer().getSecondsAtPosition(p);
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
  public String getSegmentMetadataJson() throws NexusException {
    try {
      return jsonapiPayloadFactory.serialize(jsonapiPayloadFactory.newJsonapiPayload()
        .setDataOne(jsonapiPayloadFactory.toPayloadObject(workbench.getSegment()))
        .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentArrangements()))
        .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentChoices()))
        .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentChords()))
        .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentMemes()))
        .addAllToIncluded(jsonapiPayloadFactory.toPayloadObjects(workbench.getSegmentMessages())));

    } catch (JsonapiException e) {
      throw new NexusException(e);
    }
  }

  @Override
  public String getSegmentOutputMetadataKey() {
    return getSegmentStorageKey(FileStoreProvider.EXTENSION_JSON);
  }

  @Override
  public String getSegmentOutputWaveformKey() {
    return getSegmentStorageKey(getSegment().getOutputEncoder().toLowerCase(Locale.ENGLISH));
  }

  @Override
  public String getSegmentStorageKey(String extension) {
    return Segments.getStorageKey(getSegment().getStorageKey(), extension);
  }

  @Override
  public Optional<ProgramSequence> getSequence(SegmentChoice choice) {
    Optional<Program> program = getProgram(choice);
    if (program.isEmpty()) return Optional.empty();
    if (Value.isSet(choice.getProgramSequenceBindingId())) {
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
    if (Value.isEmpty(choice.getProgramSequenceBindingId()))
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
    if (Value.isEmpty(type))
      type = computeType();
    return type;
  }

  @Override
  public Optional<SegmentChordVoicing> getVoicing(SegmentChord chord, InstrumentType type) {
    Collection<SegmentChordVoicing> voicings = workbench.getSegmentChordVoicings();
    return voicings.stream()
      .filter(Voicing::containsAnyValidNotes)
      .filter(voicing -> Objects.equals(type, voicing.getType()))
      .filter(voicing -> Objects.equals(chord.getId(), voicing.getSegmentChordId()))
      .findAny();
  }

  @Override
  public boolean hasMoreSequenceBindingOffsets(SegmentChoice choice, int N) {
    if (Value.isEmpty(choice.getProgramSequenceBindingId()))
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
      segmentDAO.update(access, segment.getId(), segment);
      workbench.setSegment(segment);

    } catch (DAOFatalException | DAOExistenceException | DAOPrivilegeException | DAOValidationException e) {
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

  /**
   @return Chain base key
   */
  private String computeChainBaseKey() {
    return
      Strings.isNullOrEmpty(getChain().getEmbedKey()) ?
        String.format("chain-%s", chain.getId())
        : getChain().getEmbedKey();
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
        .delta(sourceRange.getLow().orElseThrow(() -> new NexusException("Can't find low end of source range")).shiftOctave(o));
      int dHigh = targetRange.getHigh().orElseThrow(() -> new NexusException("Can't find high end of target range"))
        .delta(sourceRange.getHigh().orElseThrow(() -> new NexusException("Can't find high end of source range")).shiftOctave(o));
      if (0 <= dLow && 0 >= dHigh && Math.abs(o) < baselineDelta) {
        baselineDelta = Math.abs(o);
        shiftOctave = o;
      }
    }
    return shiftOctave;
  }

  /**
   General a Segment URL

   @param chain   to generate URL for
   @param segment to generate URL for
   @return URL as string
   */
  private String computeStorageKey(Chain chain, Segment segment) {
    String chainName = Strings.isNullOrEmpty(chain.getEmbedKey()) ?
      "chain" + NAME_SEPARATOR + chain.getId() :
      chain.getEmbedKey();
    String segmentName = segmentNameFormat.format(Instant.parse(segment.getBeginAt()).toEpochMilli());
    return fileStoreProvider.generateKey(chainName + NAME_SEPARATOR + segmentName);
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
  private String computeChainMetadataJson(Collection<Segment> segments) throws NexusException {
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
  private String computeEventChordKey(UUID eventId, String chordName) throws NexusException, EntityStoreException {
    return String.format("%s__%s", chordName, eventId.toString());
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
  private void ensureStorageKey() {
    if (Value.isEmpty(workbench.getSegment().getStorageKey()) || workbench.getSegment().getStorageKey().isEmpty()) {
      workbench.setSegment(
        workbench.getSegment()
          .storageKey(computeStorageKey(workbench.getChain(), workbench.getSegment()))
      );
      LOG.debug("[segId={}] Generated storage key {}", workbench.getSegment().getId(), workbench.getSegment().getStorageKey());
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
