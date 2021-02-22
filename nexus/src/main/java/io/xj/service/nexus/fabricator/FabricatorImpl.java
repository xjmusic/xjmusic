// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.google.protobuf.MessageLite;
import com.typesafe.config.Config;
import io.xj.Chain;
import io.xj.ChainBinding;
import io.xj.Instrument;
import io.xj.InstrumentAudio;
import io.xj.InstrumentAudioEvent;
import io.xj.Program;
import io.xj.ProgramSequence;
import io.xj.ProgramSequenceBinding;
import io.xj.ProgramSequenceChordVoicing;
import io.xj.ProgramSequencePattern;
import io.xj.ProgramSequencePatternEvent;
import io.xj.ProgramVoice;
import io.xj.ProgramVoiceTrack;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.SegmentChoiceArrangement;
import io.xj.SegmentChoiceArrangementPick;
import io.xj.SegmentChord;
import io.xj.SegmentChordVoicing;
import io.xj.SegmentMeme;
import io.xj.SegmentMessage;
import io.xj.lib.entity.Entities;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.jsonapi.JsonApiException;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.lib.music.AdjSymbol;
import io.xj.lib.music.Key;
import io.xj.lib.music.MusicalException;
import io.xj.lib.music.Note;
import io.xj.lib.music.NoteRange;
import io.xj.lib.music.Tuning;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Chance;
import io.xj.lib.util.Value;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.client.HubClient;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.hub.client.HubClientException;
import io.xj.service.hub.client.HubContent;
import io.xj.service.hub.dao.InstrumentConfig;
import io.xj.service.hub.dao.ProgramConfig;
import io.xj.service.nexus.dao.ChainBindingDAO;
import io.xj.service.nexus.dao.ChainConfig;
import io.xj.service.nexus.dao.ChainDAO;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static io.xj.Instrument.Type.UNRECOGNIZED;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
class FabricatorImpl implements Fabricator {
  private static final double MICROS_PER_SECOND = 1000000.0F;
  private static final double NANOS_PER_SECOND = 1000.0F * MICROS_PER_SECOND;
  private static final String KEY_VOICE_TRACK_TEMPLATE = "voice-%s_track-%s";
  private static final String KEY_VOICE_NOTE_TEMPLATE = "voice-%s_note-%s";
  private static final String EXTENSION_SEPARATOR = ".";
  private static final String EXTENSION_JSON = "json";
  private static final String NAME_SEPARATOR = "-";
  private static final String UNKNOWN_KEY = "unknown";
  private final HubClientAccess access;
  private final FileStoreProvider fileStoreProvider;
  private final Chain chain;
  private final HubContent sourceMaterial;
  private final Logger log = LoggerFactory.getLogger(FabricatorImpl.class);
  private final long startTime;
  private final Map<SegmentChoice, ProgramSequence> sequenceForChoice = Maps.newHashMap();
  private final SegmentWorkbench workbench;
  private final SegmentRetrospective retrospective;
  private final Tuning tuning;
  private final double tuningRootPitch;
  private final String tuningRootNote;
  private final Set<String> boundProgramIds;
  private final Set<String> boundInstrumentIds;
  private final Config config;
  private final Collection<ChainBinding> chainBindings;
  private Segment.Type type;
  private final String workTempFilePathPrefix;
  private final DecimalFormat segmentNameFormat;
  private final ChainConfig chainConfig;
  private final SegmentDAO segmentDAO;
  private final FabricatorFactory fabricatorFactory;
  private Map<String, InstrumentAudio> previousInstrumentAudio;
  private final Map<String, Collection<Note>> voicingNotesForSegmentChordInstrumentType = Maps.newHashMap();
  private final Map<Instrument.Type, NoteRange> voicingNoteRange = Maps.newHashMap();
  private final Map<String, Collection<InstrumentAudioEvent>> firstEventsOfAudiosOfInstrument = Maps.newHashMap();
  private final PayloadFactory payloadFactory;

  @AssistedInject
  public FabricatorImpl(
    @Assisted("access") HubClientAccess access,
    @Assisted("segment") Segment segment,
    Config config,
    HubClient hubClient,
    ChainDAO chainDAO,
    ChainBindingDAO chainBindingDAO,
    FileStoreProvider fileStoreProvider,
    FabricatorFactory fabricatorFactory,
    SegmentDAO segmentDAO,
    PayloadFactory payloadFactory
  ) throws FabricationException {
    this.segmentDAO = segmentDAO;
    this.payloadFactory = payloadFactory;
    try {
      // FUTURE: [#165815496] Chain fabrication access control
      this.access = access;
      log.info("[segId={}] HubClientAccess {}", segment.getId(), access);

      this.fileStoreProvider = fileStoreProvider;
      this.fabricatorFactory = fabricatorFactory;

      this.config = config;
      tuningRootPitch = config.getDouble("tuning.rootPitchHz");
      tuningRootNote = config.getString("tuning.rootNote");
      workTempFilePathPrefix = config.getString("work.tempFilePathPrefix");
      segmentNameFormat = new DecimalFormat(config.getString("work.segmentNameFormat"));

      // tuning
      tuning = computeTuning();
      log.info("[segId={}] Tuning {}", segment.getId(), tuning);

      // time
      startTime = System.nanoTime();
      log.info("[segId={}] StartTime {}ns since epoch zulu", segment.getId(), startTime);

      // read the chain, configs, and bindings
      chain = chainDAO.readOne(access, segment.getChainId());
      chainConfig = new ChainConfig(chain, config);
      chainBindings = chainBindingDAO.readMany(access, ImmutableList.of(chain.getId()));
      Set<String> boundLibraryIds = targetIdsOfType(chainBindings, ChainBinding.Type.Library);
      boundProgramIds = targetIdsOfType(chainBindings, ChainBinding.Type.Program);
      boundInstrumentIds = targetIdsOfType(chainBindings, ChainBinding.Type.Instrument);
      log.info("[segId={}] Chain {} configured with {} and bound to {} ", segment.getId(), chain.getId(),
        chainConfig,
        CSV.prettyFrom(chainBindings, "and"));

      // read the source material
      sourceMaterial = hubClient.ingest(access, boundLibraryIds, boundProgramIds, boundInstrumentIds);
      log.info("[segId={}] SourceMaterial loaded {} entities", segment.getId(), sourceMaterial.size());

      // setup the segment retrospective
      retrospective = fabricatorFactory.loadRetrospective(access, segment, sourceMaterial);

      // get the current segment on the workbench
      workbench = fabricatorFactory.setupWorkbench(access, chain, segment);

      // final pre-flight check
      ensureStorageKey();

    } catch (DAOFatalException | DAOExistenceException | DAOPrivilegeException | HubClientException | ValueException e) {
      throw new FabricationException("Failed to instantiate Fabricator!", e);
    }
  }

  @Override
  public <N extends MessageLite> N add(N entity) throws FabricationException {
    return workbench.add(entity);
  }

  @Override
  public Double computeSecondsAtPosition(double p) throws FabricationException {
    return getTimeComputer().getSecondsAtPosition(p);
  }

  @Override
  public HubClientAccess getAccess() {
    return access;
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
  public Chain getChain() {
    return chain;
  }

  @Override
  public ChainConfig getChainConfig() {
    return chainConfig;
  }

  @Override
  public String getChainId() {
    return chain.getId();
  }

  @Override
  public Optional<SegmentChord> getChordAt(int position) {
    Optional<SegmentChord> foundChord = Optional.empty();
    Double foundPosition = null;

    // we assume that these entities are in order of position ascending
    for (SegmentChord segmentChord : workbench.getSegmentChords()) {
      // if it's a better match (or no match has yet been found) then use it
      if (Objects.isNull(foundPosition) ||
        segmentChord.getPosition() > foundPosition && segmentChord.getPosition() < position) {
        foundPosition = segmentChord.getPosition();
        foundChord = Optional.of(segmentChord);
      }
    }

    return foundChord;
  }

  @Override
  public Collection<SegmentMessage> getSegmentMessages() {
    return workbench.getSegmentMessages();
  }

  @Override
  public Optional<SegmentChoice> getCurrentMacroChoice() {
    return workbench.getChoiceOfType(Program.Type.Macro);
  }

  @Override
  public Optional<SegmentChoice> getCurrentMainChoice() {
    return workbench.getChoiceOfType(Program.Type.Main);
  }

  @Override
  public Optional<SegmentChoice> getCurrentRhythmChoice() {
    return workbench.getChoiceOfType(Program.Type.Rhythm);
  }

  @Override
  public Collection<SegmentChoice> getCurrentDetailChoices() {
    return workbench.getChoicesOfType(Program.Type.Rhythm);
  }

  @Override
  public Double getElapsedSeconds() {
    return (System.nanoTime() - startTime) / NANOS_PER_SECOND;
  }

  @Override
  public Key getKeyForChoice(SegmentChoice choice) throws FabricationException {
    Optional<Program> program = getProgram(choice);
    if (Value.isSet(choice.getProgramSequenceBindingId())) {
      var sequence = getSequence(choice);
      if (sequence.isPresent())
        return Key.of(sequence.get().getKey());
    }

    return Key.of(program
      .orElseThrow(() -> new FabricationException("Cannot get key for nonexistent choice!"))
      .getKey());
  }

  @Override
  public Key getKeyForArrangement(SegmentChoiceArrangement arrangement) throws FabricationException {
    return getKeyForChoice(getChoice(arrangement).orElseThrow(() ->
      new FabricationException(String.format("No key found for Arrangement[%s]", arrangement.getId()))));
  }

  @Override
  public Long getMaxAvailableSequenceBindingOffset(SegmentChoice choice) throws FabricationException {
    if (Value.isEmpty(choice.getProgramSequenceBindingId()))
      throw exception("Cannot determine whether choice with no SequenceBinding has two more available Sequence Pattern offsets");
    var sequenceBinding = getSequenceBinding(choice);
    if (sequenceBinding.isEmpty()) return 0L;

    Optional<Long> max = sourceMaterial.getAvailableOffsets(sequenceBinding.get()).stream().max(Long::compareTo);
    if (max.isEmpty()) throw exception("Cannot determine max available sequence binding offset");
    return max.get();

  }

  @Override
  public Map<String, Collection<SegmentChoiceArrangement>> getMemeConstellationArrangementsOfPreviousSegments() {
    Map<String, Collection<SegmentChoiceArrangement>> out = Maps.newHashMap();
    for (Map.Entry<String, Collection<SegmentChoice>> entry : getMemeConstellationChoicesOfPreviousSegments().entrySet()) {
      String con = entry.getKey();
      Collection<SegmentChoice> previousChoices = entry.getValue();
      if (!out.containsKey(con)) out.put(con, Lists.newArrayList());

      for (SegmentChoice choice : previousChoices) {
        for (SegmentChoiceArrangement arrangement : retrospective.getArrangements(choice)) {
          out.get(con).add(arrangement);
        }
      }
    }
    return out;
  }

  @Override
  public Collection<SegmentChoiceArrangement> getChoiceArrangementsOfPreviousSegments() throws FabricationException {
    Collection<SegmentChoiceArrangement> out = Lists.newArrayList();
    for (Segment seg : getPreviousSegmentsWithSameMainProgram())
      out.addAll(retrospective.getSegmentChoiceArrangements(seg));
    return out;
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getChoiceArrangementPicksOfPreviousSegments() throws FabricationException {
    Collection<SegmentChoiceArrangementPick> out = Lists.newArrayList();
    for (Segment seg : getPreviousSegmentsWithSameMainProgram())
      out.addAll(retrospective.getSegmentChoiceArrangementPicks(seg));
    return out;
  }

  @Override
  public Map<String, Collection<SegmentChoice>> getMemeConstellationChoicesOfPreviousSegments() {
    Map<String, Collection<SegmentChoice>> out = Maps.newHashMap();
    for (Segment seg : getPreviousSegmentsWithSameMainProgram()) {
      Isometry iso = MemeIsometry.ofMemes(Entities.namesOf(retrospective.getSegmentMemes(seg)));
      String con = iso.getConstellation();
      if (!out.containsKey(con)) out.put(con, Lists.newArrayList());
      out.get(con).addAll(retrospective.getSegmentChoices(seg));
    }
    return out;
  }

  @Override
  public Collection<SegmentChoice> getChoicesOfPreviousSegments() {
    Collection<SegmentChoice> out = Lists.newArrayList();
    for (Segment seg : getPreviousSegmentsWithSameMainProgram())
      out.addAll(retrospective.getSegmentChoices(seg));
    return out;
  }

  @Override
  public Map<String, Collection<SegmentChoiceArrangementPick>> getMemeConstellationPicksOfPreviousSegments() {
    Map<String, Collection<SegmentChoiceArrangementPick>> out = Maps.newHashMap();
    for (Segment seg : getPreviousSegmentsWithSameMainProgram()) {
      Isometry iso = MemeIsometry.ofMemes(Entities.namesOf(retrospective.getSegmentMemes(seg)));
      String con = iso.getConstellation();
      if (!out.containsKey(con)) out.put(con, Lists.newArrayList());
      out.get(con).addAll(retrospective.getSegmentChoiceArrangementPicks(seg));
    }
    return out;
  }

  @Override
  public Map<String, InstrumentAudio> getPreviousInstrumentAudio() {
    // this map is built once from the retrospective, and from then on is modified by its accessors--
    // FUTURE it's not great ^^ that we are modifying this map externally
    if (Objects.isNull(previousInstrumentAudio))
      try {
        previousInstrumentAudio = Maps.newHashMap();
        for (SegmentChoiceArrangementPick pick : getChoiceArrangementPicksOfPreviousSegments()) {
          getSourceMaterial().getInstrumentAudio(pick.getInstrumentAudioId())
            .ifPresent(audio -> previousInstrumentAudio.put(keyByVoiceTrack(pick), audio));
        }

      } catch (FabricationException e) {
        log.error("Unable to build map create previous instrument audio", e);
      }

    return previousInstrumentAudio;
  }


  @Override
  public String keyByVoiceTrack(SegmentChoiceArrangementPick pick) {
    String voiceId =
      getSourceMaterial().getProgramSequencePatternEvent(pick.getProgramSequencePatternEventId())
        .map(event -> getSourceMaterial().getTrack(event)
          .map(ProgramVoiceTrack::getProgramVoiceId)
          .orElse(UNKNOWN_KEY))
        .orElse(UNKNOWN_KEY);

    return String.format(KEY_VOICE_TRACK_TEMPLATE, voiceId, pick.getName());
  }

  @Override
  public String keyByVoiceTrack(ProgramSequencePatternEvent event) {
    String voiceId =
      getSourceMaterial().getVoice(event)
        .map(ProgramVoice::getId)
        .orElse(UNKNOWN_KEY);

    return String.format(KEY_VOICE_TRACK_TEMPLATE, voiceId, getTrackName(event));
  }

  @Override
  public String keyByTrackNote(String track, Note note) {
    return String.format(KEY_VOICE_NOTE_TEMPLATE, track, note.toString(AdjSymbol.Sharp));
  }

  @Override
  public String getTrackName(ProgramSequencePatternEvent event) {
    return getSourceMaterial().getTrack(event)
      .map(ProgramVoiceTrack::getName)
      .orElse(UNKNOWN_KEY);
  }

  @Override
  public Optional<String> getPreviousVoiceInstrumentId(String voiceId) {
    try {
      return getChoiceArrangementsOfPreviousSegments()
        .stream()
        .filter(arrangement -> voiceId.equals(arrangement.getProgramVoiceId()))
        .map(SegmentChoiceArrangement::getInstrumentId)
        .findFirst();

    } catch (FabricationException e) {
      log.warn(formatLog(String.format("Could not get previous voice instrumentId for voiceId=%s", voiceId)), e);
    }
    return Optional.empty();
  }

  @Override
  public MemeIsometry getMemeIsometryOfCurrentMacro() {
    var macroChoice = getCurrentMacroChoice();
    if (macroChoice.isEmpty()) return MemeIsometry.none();
    return MemeIsometry.ofMemes(toStrings(getMemesOfChoice(macroChoice.get())));
  }

  @Override
  public MemeIsometry getMemeIsometryOfNextSequenceInPreviousMacro() {
    var previousMacroChoice = getPreviousMacroChoice();
    if (previousMacroChoice.isEmpty()) return MemeIsometry.none();
    return MemeIsometry.ofMemes(toStrings(getMemesOfChoice(previousMacroChoice.get())));
  }

  @Override
  public MemeIsometry getMemeIsometryOfSegment() {
    return MemeIsometry.ofMemes(Entities.namesOf(workbench.getSegmentMemes()));
  }

  @Override
  public Collection<SegmentMeme> getMemesOfChoice(SegmentChoice choice) {
    Collection<SegmentMeme> result = Lists.newArrayList();
    var program = getProgram(choice);
    if (program.isEmpty())
      return ImmutableList.of();
    sourceMaterial.getMemes(program.get())
      .forEach(meme -> result.add(SegmentMeme.newBuilder()
        .setName(meme.getName())
        .setSegmentId(choice.getSegmentId())
        .build()));
    if (Value.isSet(choice.getProgramSequenceBindingId())) {
      var sequenceBinding = getSequenceBinding(choice);
      sequenceBinding.ifPresent(programSequenceBinding -> sourceMaterial.getMemes(programSequenceBinding)
        .forEach(meme -> result.add(SegmentMeme.newBuilder()
          .setName(meme.getName())
          .setSegmentId(choice.getSegmentId())
          .build())));
    }
    return result;

  }

  @Override
  public Long getNextSequenceBindingOffset(SegmentChoice choice) {
    if (Value.isEmpty(choice.getProgramSequenceBindingId()))
      return 0L;

    var sequenceBinding = getSequenceBinding(choice);
    Long sequenceBindingOffset = getSequenceBindingOffsetForChoice(choice);
    Long offset = null;
    if (sequenceBinding.isEmpty()) return 0L;
    for (Long availableOffset : sourceMaterial.getAvailableOffsets(sequenceBinding.get()))
      if (0 < availableOffset.compareTo(sequenceBindingOffset))
        if (Objects.isNull(offset) ||
          0 > availableOffset.compareTo(offset))
          offset = availableOffset;

    // if none found, loop back around to zero
    return Objects.nonNull(offset) ? offset : Long.valueOf(0L);

  }

  @Override
  public AudioFormat getOutputAudioFormat() {
    return new AudioFormat(
      chainConfig.getOutputEncoding(),
      chainConfig.getOutputFrameRate(),
      chainConfig.getOutputSampleBits(),
      chainConfig.getOutputChannels(),
      chainConfig.getOutputChannels() * chainConfig.getOutputSampleBits() / 8,
      chainConfig.getOutputFrameRate(),
      false);
  }

  @Override
  public String getFullQualityAudioOutputFilePath() {
    return String.format("%s%s", workTempFilePathPrefix, getSegmentOutputWaveformKey());
  }

  @Override
  public Double getPitch(Note note) {
    return tuning.pitch(note);
  }

  @Override
  public Collection<Segment> getPreviousSegmentsWithSameMainProgram() {
    return retrospective.getSegments();
  }

  @Override
  public Optional<Program> getProgram(SegmentChoice choice) {
    return sourceMaterial.getProgram(choice.getProgramId());

  }

  @Override
  public Optional<Program> getProgram(SegmentChoiceArrangement arrangement) {
    var choice = getChoice(arrangement);
    return choice.isPresent() ? getProgram(choice.get()) : Optional.empty();
  }

  @Override
  public Optional<SegmentChoice> getPreviousMacroChoice() {
    return retrospective.getPreviousChoiceOfType(Program.Type.Macro);
  }

  @Override
  public Optional<SegmentChoice> getPreviousMainChoice() {
    return retrospective.getPreviousChoiceOfType(Program.Type.Main);
  }

  @Override
  public Segment getPreviousSegment() throws FabricationException {
    if (isInitialSegment() || retrospective.getPreviousSegment().isEmpty())
      throw exception("Initial Segment has no previous Segment");

    return retrospective.getPreviousSegment().get();
  }

  @Override
  public Segment getSegment() {
    return workbench.getSegment();
  }

  @Override
  public void updateSegment(Segment segment) {
    try {
      segmentDAO.update(access, segment.getId(), segment);
      workbench.setSegment(segment);

    } catch (DAOFatalException | DAOExistenceException | DAOPrivilegeException | DAOValidationException e) {
      log.error("Failed to update Segment", e);
    }
  }

  @Override
  public String getSegmentOutputWaveformKey() {
    return getSegmentStorageKey(getSegment().getOutputEncoder().toLowerCase(Locale.ENGLISH));
  }

  @Override
  public String getSegmentOutputMetadataKey() {
    return getSegmentStorageKey(EXTENSION_JSON);
  }

  @Override
  public String getChainOutputMetadataKey() {
    return getChainStorageKey(EXTENSION_JSON);
  }

  @Override
  public String getSegmentStorageKey(String extension) {
    return String.format("%s%s%s", getSegment().getStorageKey(), EXTENSION_SEPARATOR, extension);
  }

  @Override
  public String getChainStorageKey(String extension) {
    String chainKey = Strings.isNullOrEmpty(getChain().getEmbedKey()) ?
      String.format("chain-%s", getChainId())
      : getChain().getEmbedKey();
    return String.format("%s%s%s", chainKey, EXTENSION_SEPARATOR, extension);
  }

  @Override
  public Duration getSegmentTotalLength() throws FabricationException {
    if (Value.isEmpty(workbench.getSegment().getEndAt()))
      throw exception("Cannot compute total length create segment with no end!");

    return Duration.between(
      Instant.parse(workbench.getSegment().getBeginAt()),
      Instant.parse(workbench.getSegment().getEndAt()))
      .plusNanos((long) (workbench.getSegment().getWaveformPreroll() * NANOS_PER_SECOND));
  }

  @Override
  public Optional<ProgramSequence> getSequence(SegmentChoice choice) {
    Optional<Program> program = getProgram(choice);
    if (program.isEmpty()) return Optional.empty();
    if (Value.isSet(choice.getProgramSequenceBindingId())) {
      var sequenceBinding = getSequenceBinding(choice);
      if (sequenceBinding.isPresent())
        return sourceMaterial.getProgramSequence(sequenceBinding.get().getProgramSequenceId());
    }

    if (!sequenceForChoice.containsKey(choice))
      randomlySelectSequence(program.get())
        .ifPresent(programSequence -> sequenceForChoice.put(choice, programSequence));

    return Optional.of(sequenceForChoice.get(choice));
  }

  @Override
  public Long getSequenceBindingOffsetForChoice(SegmentChoice choice) {
    if (Value.isEmpty(choice.getProgramSequenceBindingId()))
      return 0L;
    var sequenceBinding = getSequenceBinding(choice);
    return sequenceBinding.map(ProgramSequenceBinding::getOffset).orElse(0L);
  }

  @Override
  public HubContent getSourceMaterial() {
    return sourceMaterial;
  }

  @Override
  public Segment.Type getType() {
    if (Value.isEmpty(type))
      type = determineType();
    return type;
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
  public Boolean isInitialSegment() {
    return 0L == workbench.getSegment().getOffset();
  }

  @Override
  public void putReport(String key, Object value) {
    workbench.putReport(key, value);
  }

  @Override
  public void done() throws FabricationException {
    try {
      workbench.setSegment(workbench.getSegment().toBuilder()
        .setType(getType())
        .build());
      workbench.done();
    } catch (FabricationException | JsonApiException | ValueException e) {
      throw exception("Could not complete Segment fabrication", e);
    }
    switch (getType()) {
      case Continue:
        // transitions only once, of empty to non-empty
        log.info("[segId={}] continues main sequence create previous segments: {}",
          workbench.getSegment().getId(),
          Entities.csvIdsOf(getPreviousSegmentsWithSameMainProgram()));
        break;
      case Initial:
      case NextMain:
      case NextMacro:
        break;
    }
  }

  @Override
  public Optional<ProgramSequenceBinding> randomlySelectSequenceBindingAtOffset(Program program, Long offset) {
    EntityScorePicker<ProgramSequenceBinding> entityScorePicker = new EntityScorePicker<>();
    for (ProgramSequenceBinding sequenceBinding : sourceMaterial.getProgramSequenceBindingsAtOffset(program, offset))
      entityScorePicker.add(sequenceBinding, Chance.normallyAround(0.0, 1.0));

    return entityScorePicker.getTop();
  }

  @Override
  public Optional<ProgramSequence> randomlySelectSequence(Program program) {
    EntityScorePicker<ProgramSequence> entityScorePicker = new EntityScorePicker<>();
    sourceMaterial.getAllProgramSequences().stream()
      .filter(s -> s.getProgramId().equals(program.getId()))
      .forEach(sequence -> entityScorePicker.add(sequence, Chance.normallyAround(0.0, 1.0)));
    return entityScorePicker.getTop();

  }

  @Override
  public Optional<ProgramSequencePattern> randomlySelectPatternOfSequenceByVoiceAndType(ProgramSequence sequence, ProgramVoice voice, ProgramSequencePattern.Type patternType) {
    EntityScorePicker<ProgramSequencePattern> rank = new EntityScorePicker<>();
    sourceMaterial.getAllProgramSequencePatterns().stream()
      .filter(pattern -> pattern.getProgramSequenceId().equals(sequence.getId()))
      .filter(pattern -> pattern.getProgramVoiceId().equals(voice.getId()))
      .filter(pattern -> pattern.getType() == patternType)
      .forEach(pattern ->
        rank.add(pattern, Chance.normallyAround(0.0, 1.0)));
    if (Objects.equals(0, rank.size()))
      return Optional.empty();
    return rank.getTop();

  }

  @Override
  public String getSegmentMetadataJson() throws FabricationException {
    try {
      return payloadFactory.serialize(payloadFactory.newJsonapiPayload()
        .setDataOne(payloadFactory.toPayloadObject(workbench.getSegment()))
        .addAllToIncluded(payloadFactory.toPayloadObjects(workbench.getSegmentArrangements()))
        .addAllToIncluded(payloadFactory.toPayloadObjects(workbench.getSegmentChoices()))
        .addAllToIncluded(payloadFactory.toPayloadObjects(workbench.getSegmentChords()))
        .addAllToIncluded(payloadFactory.toPayloadObjects(workbench.getSegmentMemes()))
        .addAllToIncluded(payloadFactory.toPayloadObjects(workbench.getSegmentMessages()))
        .addAllToIncluded(payloadFactory.toPayloadObjects(workbench.getSegmentChoiceArrangementPicks())));

    } catch (JsonApiException e) {
      throw new FabricationException(e);
    }
  }

  @Override
  public String getChainMetadataJson() throws FabricationException {
    try {
      JsonapiPayload jsonapiPayload = new JsonapiPayload();
      jsonapiPayload.setDataOne(payloadFactory.toPayloadObject(chain));
      for (ChainBinding binding : chainBindings)
        jsonapiPayload.addToIncluded(payloadFactory.toPayloadObject(binding));
      for (Segment segment : segmentDAO.readManyFromSecondsUTC(access, getChainId(), Instant.now().getEpochSecond()))
        jsonapiPayload.addToIncluded(payloadFactory.toPayloadObject(segment));
      return payloadFactory.serialize(jsonapiPayload);

    } catch (JsonApiException | DAOPrivilegeException | DAOFatalException | DAOExistenceException e) {
      throw new FabricationException(e);
    }
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
  public ProgramConfig getProgramConfig(Program program) throws ValueException {
    return new ProgramConfig(program, config);
  }

  @Override
  public InstrumentConfig getInstrumentConfig(Instrument instrument) throws ValueException {
    return new InstrumentConfig(instrument, config);
  }

  @Override
  public List<Instrument.Type> getDistinctChordVoicingTypes() {
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
  public Tuning getTuning() {
    return tuning;
  }

  @Override
  public double getAmplitudeForInstrumentType(SegmentChoiceArrangementPick pick) {
    switch (getInstrument(pick).map(Instrument::getType).orElse(UNRECOGNIZED)) {
      case Percussive:
        return chainConfig.getDubMasterVolumeInstrumentTypePercussive();
      case Bass:
        return chainConfig.getDubMasterVolumeInstrumentTypeBass();
      case Pad:
        return chainConfig.getDubMasterVolumeInstrumentTypePad();
      case Sticky:
        return chainConfig.getDubMasterVolumeInstrumentTypeSticky();
      case Stripe:
        return chainConfig.getDubMasterVolumeInstrumentTypeStripe();
      case Stab:
        return chainConfig.getDubMasterVolumeInstrumentTypeStab();
      case UNRECOGNIZED:
      default:
        return 1.0;
    }
  }

  @Override
  public NoteRange getVoicingNoteRange(Instrument.Type type) {
    if (!voicingNoteRange.containsKey(type)) {
      var voicings = workbench.getSegmentChordVoicings();
      voicingNoteRange.put(type, new NoteRange(voicings.stream()
        .flatMap(segmentChordVoicing -> getNotes(segmentChordVoicing).stream())
        .collect(Collectors.toList())));
    }

    return voicingNoteRange.get(type);
  }

  @Override
  public Optional<Instrument> getInstrument(SegmentChoiceArrangementPick pick) {
    return sourceMaterial.getInstrumentAudio(pick.getInstrumentAudioId())
      .flatMap(audio -> sourceMaterial.getInstrument(audio.getInstrumentId()));
  }

  @Override
  public Optional<Instrument> getInstrument(SegmentChoiceArrangement arrangement) {
    return sourceMaterial.getInstrument(arrangement.getInstrumentId());
  }

  @Override
  public Collection<SegmentMeme> getSegmentMemes() {
    return workbench.getSegmentMemes();
  }

  @Override
  public Collection<SegmentChoice> getChoices() {
    return workbench.getSegmentChoices();
  }

  @Override
  public Optional<SegmentChoice> getChoice(SegmentChoiceArrangement arrangement) {
    return workbench.getSegmentChoices().stream()
      .filter(choice -> arrangement.getSegmentChoiceId().equals(choice.getId()))
      .findFirst();
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements() {
    return workbench.getSegmentArrangements();
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getPicks() {
    return workbench.getSegmentChoiceArrangementPicks();
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements(Collection<SegmentChoice> choices) {
    Collection<String> choiceIds = Entities.idsOf(choices);
    return getArrangements().stream()
      .filter(arrangement -> choiceIds.contains(String.valueOf(arrangement.getSegmentChoiceId())))
      .collect(Collectors.toList());
  }

  @Override
  public Collection<SegmentChord> getSegmentChords() {
    return workbench.getSegmentChords();
  }

  @Override
  public Optional<SegmentChordVoicing> getVoicing(SegmentChord chord, Instrument.Type type) {
    Collection<SegmentChordVoicing> voicings = workbench.getSegmentChordVoicings();
    return voicings.stream()
      .filter(voicing -> type.equals(voicing.getType()))
      .filter(voicing -> Objects.equals(chord.getId(), voicing.getSegmentChordId()))
      .findAny();
  }

  @Override
  public Collection<Note> getVoicingNotes(SegmentChord chord, Instrument.Type type) {
    var key = String.format("%s__%s", chord.getId(), type);

    if (!voicingNotesForSegmentChordInstrumentType.containsKey(key)) {
      var voicing = getVoicing(chord, type);
      if (voicing.isPresent())
        voicingNotesForSegmentChordInstrumentType.put(key, getNotes(voicing.get()));
      else
        voicingNotesForSegmentChordInstrumentType.put(key, ImmutableList.of());
    }

    return voicingNotesForSegmentChordInstrumentType.get(key);
  }

  @Override
  public Collection<Note> getNotes(SegmentChordVoicing voicing) {
    return CSV.split(voicing.getNotes()).stream().map(Note::of).collect(Collectors.toList());
  }

  @Override
  public NoteRange getRangeForArrangement(SegmentChoiceArrangement segmentChoiceArrangement) throws FabricationException {
    var program = getProgram(segmentChoiceArrangement);
    if (program.isEmpty())
      throw new FabricationException("Can't get note range for nonexistent program!");
    return new NoteRange(sourceMaterial.getEvents(program.get())
      .stream()
      .filter(programSequencePatternEvent -> sourceMaterial.getTrack(programSequencePatternEvent)
        .map(track -> programSequencePatternEvent.getProgramVoiceTrackId().equals(track.getId()))
        .orElse(false))
      .map(programSequencePatternEvent -> Note.of(programSequencePatternEvent.getNote()))
      .collect(Collectors.toList()));

  }

  @Override
  public Collection<InstrumentAudioEvent> getFirstEventsOfAudiosOfInstrument(Instrument instrument) {
    if (!firstEventsOfAudiosOfInstrument.containsKey(instrument.getId()))
      firstEventsOfAudiosOfInstrument.put(instrument.getId(),
        getSourceMaterial().getFirstEventsOfAudiosOfInstrument(instrument));

    return firstEventsOfAudiosOfInstrument.get(instrument.getId());
  }

  @Override
  public boolean continuesMacroProgram() {
    return
      Segment.Type.Continue.equals(getType()) ||
        Segment.Type.NextMain.equals(getType());
  }

  @Override
  public boolean hasMoreSequenceBindingOffsets(SegmentChoice choice, int N) {
    if (Value.isEmpty(choice.getProgramSequenceBindingId()))
      return false;
    var sequenceBinding = getSequenceBinding(choice);

    if (sequenceBinding.isEmpty())
      return false;
    List<Long> avlOfs = ImmutableList.copyOf(sourceMaterial.getAvailableOffsets(sequenceBinding.get()));

    // if we locate the target and still have two offsets remaining, result is true
    for (int i = 0; i < avlOfs.size(); i++)
      if (avlOfs.get(i).equals(sequenceBinding.get().getOffset()) && i < avlOfs.size() - N)
        return true;

    return false;
  }

  @Override
  public Segment.Type determineType() {
    if (isInitialSegment())
      return Segment.Type.Initial;

    // previous main choice having at least one more pattern?
    var previousMainChoice = getPreviousMainChoice();

    if (previousMainChoice.isPresent() && hasOneMoreSequenceBindingOffset(previousMainChoice.get()))
      return Segment.Type.Continue;

    // previous macro choice having at least two more patterns?
    var previousMacroChoice = getPreviousMacroChoice();

    if (previousMacroChoice.isPresent() && hasTwoMoreSequenceBindingOffsets(previousMacroChoice.get()))
      return Segment.Type.NextMain;

    return Segment.Type.NextMacro;
  }

  /**
   Collection Strings from collection of of Segment Memes

   @param memes to get strings of
   @return strings
   */
  private Collection<String> toStrings(Collection<SegmentMeme> memes) {
    return memes.stream().map(SegmentMeme::getName).collect(Collectors.toList());
  }

  /**
   General a Segment URL

   @param chain   to generate URL for
   @param segment to generate URL for
   @return URL as string
   */
  private String generateStorageKey(Chain chain, Segment segment) {
    String chainName = Strings.isNullOrEmpty(chain.getEmbedKey()) ?
      "chain" + NAME_SEPARATOR + chain.getId() :
      chain.getEmbedKey();
    String segmentName = segmentNameFormat.format(Instant.parse(segment.getBeginAt()).toEpochMilli());
    return fileStoreProvider.generateKey(chainName + NAME_SEPARATOR + segmentName);
  }

  /**
   Filter and map target ids of a specified type from a set of chain bindings

   @param chainBindings to filter and map from
   @param type          to include
   @return set of target ids of the specified type of chain binding targets
   */
  private Set<String> targetIdsOfType(Collection<ChainBinding> chainBindings, ChainBinding.Type type) {
    return chainBindings.stream().filter(chainBinding -> chainBinding.getType().equals(type))
      .map(ChainBinding::getTargetId).collect(Collectors.toSet());
  }

  /**
   Get a Sequence Binding for a given Choice

   @param choice to get sequence binding for
   @return Sequence Binding for the given Choice
   */
  private Optional<ProgramSequenceBinding> getSequenceBinding(SegmentChoice choice) {
    return sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());
  }

  /**
   [#255] Tuning based on root note configured in environment parameters.
   */
  private Tuning computeTuning() throws FabricationException {
    try {
      return Tuning.at(
        Note.of(tuningRootNote),
        tuningRootPitch);
    } catch (MusicalException e) {
      throw exception("Could not tune XJ!", e);
    }
  }

  /**
   Create a new CoreException prefixed with a segment id

   @param message to include in exception
   @return CoreException to throw
   */
  private FabricationException exception(String message) {
    return new FabricationException(formatLog(message));
  }

  /**
   Create a new CoreException prefixed with a segment id, including sub-exception

   @param message to include in exception
   @param e       Exception to include in exception
   @return CoreException to throw
   */
  private FabricationException exception(String message, Exception e) {
    return new FabricationException(formatLog(message), e);
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
   Get a time computer, configured for the current segment.
   Don't use it before this segment has enough choices to determine its time computer

   @return Time Computer
   */
  private TimeComputer getTimeComputer() throws FabricationException {
    double toTempo = workbench.getSegment().getTempo(); // velocity at current segment tempo
    double fromTempo;
    try {
      Segment previous = getPreviousSegment();
      // velocity at previous segment tempo
      fromTempo = previous.getTempo();
    } catch (Exception ignored) {
      fromTempo = toTempo;
    }
    double totalBeats = workbench.getSegment().getTotal();
    putReport("totalBeats", totalBeats);
    putReport("fromTempo", fromTempo);
    putReport("toTempo", toTempo);
    if (0 == totalBeats) throw new FabricationException("Can't instantiate time computer with zero total beats!");
    if (0 == fromTempo) throw new FabricationException("Can't instantiate time computer from zero tempo!");
    if (0 == toTempo) throw new FabricationException("Can't instantiate time computer to zero tempo!");
    return fabricatorFactory.createTimeComputer(totalBeats, fromTempo, toTempo);
  }

  /**
   Ensure the current segment has a storage key; if not, add a storage key to this Segment
   */
  private void ensureStorageKey() {
    if (Value.isEmpty(workbench.getSegment().getStorageKey()) || workbench.getSegment().getStorageKey().isEmpty()) {
      workbench.setSegment(
        workbench.getSegment().toBuilder()
          .setStorageKey(generateStorageKey(workbench.getChain(), workbench.getSegment()))
          .build());
      log.info("[segId={}] Generated storage key {}", workbench.getSegment().getId(), workbench.getSegment().getStorageKey());
    }
  }

}
