// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.typesafe.config.Config;
import io.xj.lib.music.Chord;
import io.xj.lib.music.MusicalException;
import io.xj.lib.music.Note;
import io.xj.lib.music.Tuning;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.Chance;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.dao.ChainBindingDAO;
import io.xj.service.hub.dao.ChainConfigDAO;
import io.xj.service.hub.dao.ChainDAO;
import io.xj.service.hub.entity.Entity;
import io.xj.service.hub.entity.MemeEntity;
import io.xj.service.hub.persistence.AmazonProvider;
import io.xj.service.hub.ingest.Ingest;
import io.xj.service.hub.ingest.IngestCacheProvider;
import io.xj.service.hub.isometry.EntityRank;
import io.xj.service.hub.isometry.Isometry;
import io.xj.service.hub.isometry.MemeIsometry;
import io.xj.service.hub.model.Chain;
import io.xj.service.hub.model.ChainBinding;
import io.xj.service.hub.model.ChainConfig;
import io.xj.service.hub.model.ChainConfigType;
import io.xj.service.hub.model.InstrumentAudio;
import io.xj.service.hub.model.Program;
import io.xj.service.hub.model.ProgramSequence;
import io.xj.service.hub.model.ProgramSequenceBinding;
import io.xj.service.hub.model.ProgramSequencePattern;
import io.xj.service.hub.model.ProgramSequencePatternType;
import io.xj.service.hub.model.ProgramType;
import io.xj.service.hub.model.ProgramVoice;
import io.xj.service.hub.model.Segment;
import io.xj.service.hub.model.SegmentChoice;
import io.xj.service.hub.model.SegmentChoiceArrangement;
import io.xj.service.hub.model.SegmentChoiceArrangementPick;
import io.xj.service.hub.model.SegmentChord;
import io.xj.service.hub.model.SegmentMeme;
import io.xj.service.hub.model.SegmentMessage;
import io.xj.service.hub.model.SegmentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.time.Duration;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
class FabricatorImpl implements Fabricator {
  private static final double NANOS_PER_SECOND = 1000000000.0;
  private final Access access;
  private final Config config;
  private final AmazonProvider amazonProvider;
  private final Chain chain;
  private final Collection<ChainConfig> chainConfigs;
  private final Collection<ChainBinding> chainBindings;
  private final Ingest sourceMaterial;
  private final Logger log = LoggerFactory.getLogger(FabricatorImpl.class);
  private final long startTime;
  private final Map<SegmentChoice, ProgramSequence> sequenceForChoice = Maps.newHashMap();
  private final SegmentWorkbench workbench;
  private final SegmentRetrospective retrospective;
  private final TimeComputerFactory timeComputerFactory;
  private final Tuning tuning;
  private final double tuningRootPitch;
  private final String tuningRootNote;
  private SegmentType type;
  private String workTempFilePathPrefix;

  @AssistedInject
  public FabricatorImpl(
    @Assisted("access") Access access,
    @Assisted("segment") Segment segment,
    IngestCacheProvider ingestCacheProvider,
    ChainDAO chainDAO,
    ChainBindingDAO chainBindingDAO,
    ChainConfigDAO chainConfigDAO,
    TimeComputerFactory timeComputerFactory,
    SegmentWorkbenchFactory segmentWorkbenchFactory,
    AmazonProvider amazonProvider,
    SegmentRetrospectiveFactory retrospectiveFactory,
    Config config
  ) throws HubException {
    // FUTURE: [#165815496] Chain fabrication access control
    this.access = access;
    this.config = config;
    log.info("[segId={}] Access {}", segment.getId(), access);

    this.amazonProvider = amazonProvider;
    this.timeComputerFactory = timeComputerFactory;

    tuningRootPitch = config.getDouble("tuning.rootPitchHz");
    tuningRootNote = config.getString("tuning.rootNote");
    workTempFilePathPrefix = config.getString("work.tempFilePathPrefix");

    // get the current segment on the workbench
    workbench = segmentWorkbenchFactory.workOn(access, segment);

    // tuning
    tuning = computeTuning();
    log.info("[segId={}] Tuning {}", segment.getId(), tuning);

    // time
    startTime = System.nanoTime();
    log.info("[segId={}] StartTime {}ns since epoch zulu", segment.getId(), startTime);

    // read the chain, bindings, and configs
    chain = chainDAO.readOne(access, segment.getChainId());
    chainConfigs = chainConfigDAO.readMany(access, ImmutableList.of(chain.getId()));
    chainBindings = chainBindingDAO.readMany(access, ImmutableList.of(chain.getId()));
    log.info("[segId={}] Chain {}", segment.getId(), chain);

    // read the source material
    sourceMaterial = ingestCacheProvider.ingest(access, chainBindings);
    log.info("[segId={}] SourceMaterial {}", segment.getId(), sourceMaterial);

    // setup the segment retrospective
    retrospective = retrospectiveFactory.workOn(access, segment, sourceMaterial);

    // final pre-flight check
    ensureWaveformKey();
  }

  /**
   CSV string of the ids of a list of entities

   @param entities to get ids of
   @param <E>      type of entity
   @return CSV list of entity ids
   */
  static <E extends Entity> String fromIdsOf(Collection<E> entities) {
    if (Objects.isNull(entities) || entities.isEmpty()) {
      return "";
    }
    Iterator<E> it = entities.iterator();
    StringBuilder result = new StringBuilder(it.next().getId().toString());
    while (it.hasNext()) {
      result.append(",").append(it.next().getId());
    }
    return result.toString();
  }

  @Override
  public SegmentChoiceArrangement add(SegmentChoiceArrangement arrangement) {
    return workbench.getSegmentArrangements().add(arrangement);
  }

  @Override
  public SegmentChoice add(SegmentChoice choice) {
    return workbench.getSegmentChoices().add(choice);
  }

  @Override
  public SegmentChoiceArrangementPick add(SegmentChoiceArrangementPick pick) {
    return workbench.getSegmentPicks().add(pick);
  }

  @Override
  public SegmentChord add(SegmentChord segmentChord) {
    return workbench.getSegmentChords().add(segmentChord);
  }

  @Override
  public SegmentMeme add(SegmentMeme segmentMeme) {
    return workbench.getSegmentMemes().add(segmentMeme);
  }

  @Override
  public SegmentMessage add(SegmentMessage segmentMessage) {
    return workbench.getSegmentMessages().add(segmentMessage);
  }

  @Override
  public Double computeSecondsAtPosition(double p) {
    return getTimeComputer().getSecondsAtPosition(p);
  }

  @Override
  public Access getAccess() {
    return access;
  }

  @Override
  public Collection<InstrumentAudio> getPickedAudios() throws HubException {
    Collection<InstrumentAudio> audios = Lists.newArrayList();
    for (SegmentChoiceArrangementPick pick : workbench.getSegmentPicks().getAll()) {
      audios.add(sourceMaterial.getAudio(pick));
    }
    return audios;
  }

  @Override
  public Chain getChain() {
    return chain;
  }

  @Override
  public Collection<ChainConfig> getChainConfigs() {
    return chainConfigs;
  }

  @Override
  public Collection<ChainBinding> getChainBindings() {
    return chainBindings;
  }

  @Override
  public ChainConfig getChainConfig(ChainConfigType chainConfigType) throws HubException {
    Optional<ChainConfig> config = getChainConfigs().stream()
      .filter(c -> c.getType().equals(chainConfigType)).findFirst();
    if (config.isPresent()) return config.get();

    try {
      return new ChainConfig()
        .setChainId(getChainId())
        .setTypeEnum(chainConfigType)
        .setValue(getDefaultValue(chainConfigType));
    } catch (HubException e) {
      throw exception(String.format("No default value for chainConfigType=%s", chainConfigType), e);
    }
  }

  @Override
  public UUID getChainId() {
    return chain.getId();
  }

  @Override
  public Chord getChordAt(int position) {
    // default to returning a chord based on the segment key, if nothing else is found
    String foundChordText = workbench.getSegment().getKey();
    Double foundPosition = null;

    // we assume that these entities are in order of position ascending
    for (SegmentChord segmentChord : workbench.getSegmentChords().getAll()) {
      // if it's a better match (or no match has yet been found) then use it
      if (Objects.isNull(foundPosition) ||
        segmentChord.getPosition() > foundPosition && segmentChord.getPosition() < position) {
        foundPosition = segmentChord.getPosition();
        foundChordText = segmentChord.getName();
      }
    }

    return Chord.of(foundChordText);
  }

  @Override
  public SegmentChoice getCurrentMacroChoice() throws HubException {
    return workbench.getChoiceOfType(ProgramType.Macro);
  }

  @Override
  public ProgramSequence getCurrentMacroSequence() throws HubException {
    return getSequence(getCurrentMacroChoice());
  }

  @Override
  public SegmentChoice getCurrentMainChoice() throws HubException {
    return workbench.getChoiceOfType(ProgramType.Main);
  }

  @Override
  public SegmentChoice getCurrentRhythmChoice() throws HubException {
    return workbench.getChoiceOfType(ProgramType.Rhythm);
  }

  @Override
  public Double getElapsedSeconds() {
    return (System.nanoTime() - startTime) / NANOS_PER_SECOND;
  }

  @Override
  public String getKeyForChoice(SegmentChoice choice) throws HubException {
    Program program = getProgram(choice);
    if (Objects.nonNull(choice.getProgramSequenceBindingId())) {
      return getSequence(choice).getKey();
    }
    return program.getKey();
  }

  @Override
  public Long getMaxAvailableSequenceBindingOffset(SegmentChoice choice) throws HubException {
    if (Objects.isNull(choice.getProgramSequenceBindingId()))
      throw exception("Cannot determine whether choice with no SequenceBinding has two more available Sequence Pattern offsets");
    ProgramSequenceBinding sequenceBinding = getSequenceBinding(choice);

    Optional<Long> max = sourceMaterial.getAvailableOffsets(sequenceBinding).stream().max(Long::compareTo);
    if (max.isEmpty()) throw exception("Cannot determine max available sequence binding offset");
    return max.get();
  }

  @Override
  public Map<String, Collection<SegmentChoiceArrangement>> getMemeConstellationArrangementsOfPreviousSegments() {
    Map<String, Collection<SegmentChoiceArrangement>> out = Maps.newHashMap();
    getMemeConstellationChoicesOfPreviousSegments().forEach((con, previousChoices) -> {
      if (!out.containsKey(con)) out.put(con, Lists.newArrayList());

      previousChoices.forEach(choice -> retrospective.getArrangements(choice).forEach(arrangement -> out.get(con).add(arrangement)));
    });
    return out;
  }

  @Override
  public Map<String, Collection<SegmentChoice>> getMemeConstellationChoicesOfPreviousSegments() {
    Map<String, Collection<SegmentChoice>> out = Maps.newHashMap();
    for (Segment seg : getPreviousSegmentsWithSameMainProgram()) {
      Isometry iso = MemeIsometry.ofMemes(retrospective.getSegmentMemes(seg));
      String con = iso.getConstellation();
      if (!out.containsKey(con)) out.put(con, Lists.newArrayList());
      out.get(con).addAll(retrospective.getSegmentChoices(seg));
    }
    return out;
  }

  @Override
  public Map<String, Collection<SegmentChoiceArrangementPick>> getMemeConstellationPicksOfPreviousSegments() {
    Map<String, Collection<SegmentChoiceArrangementPick>> out = Maps.newHashMap();
    for (Segment seg : getPreviousSegmentsWithSameMainProgram()) {
      Isometry iso = MemeIsometry.ofMemes(retrospective.getSegmentMemes(seg));
      String con = iso.getConstellation();
      if (!out.containsKey(con)) out.put(con, Lists.newArrayList());
      out.get(con).addAll(retrospective.getSegmentPicks(seg));
    }
    return out;
  }

  @Override
  public MemeIsometry getMemeIsometryOfCurrentMacro() throws HubException {
    return MemeIsometry.ofMemes(sourceMaterial.getMemesAtBeginning(getProgram(getCurrentMacroChoice())));
  }

  @Override
  public MemeIsometry getMemeIsometryOfNextSequenceInPreviousMacro() {
    try {
      return MemeIsometry.ofMemes(sourceMaterial.getMemesAtBeginning(getProgram(getPreviousMacroChoice())));
    } catch (Exception ignored) {
      return new MemeIsometry();
    }
  }

  @Override
  public MemeIsometry getMemeIsometryOfSegment() {
    return MemeIsometry.ofMemes(workbench.getSegmentMemes().getAll());
  }

  @Override
  public Collection<MemeEntity> getMemesOfChoice(SegmentChoice choice) throws HubException {
    Collection<MemeEntity> result = Lists.newArrayList();
    result.addAll(sourceMaterial.getMemes(getProgram(choice)));
    if (Objects.nonNull(choice.getProgramSequenceBindingId()))
      result.addAll(sourceMaterial.getMemes(getSequenceBinding(choice)));
    return result;
  }

  @Override
  public Long getNextSequenceBindingOffset(SegmentChoice choice) throws HubException {
    if (Objects.isNull(choice.getProgramSequenceBindingId()))
      throw exception("Cannot determine next available SequenceBinding offset create choice with no SequenceBinding.");

    ProgramSequenceBinding sequenceBinding = getSequenceBinding(choice);
    Long sequenceBindingOffset = getSequenceBindingOffsetForChoice(choice);
    Long offset = null;
    for (Long availableOffset : sourceMaterial.getAvailableOffsets(sequenceBinding))
      if (0 < availableOffset.compareTo(sequenceBindingOffset))
        if (Objects.isNull(offset) ||
          0 > availableOffset.compareTo(offset))
          offset = availableOffset;

    // if none found, loop back around to zero
    return Objects.nonNull(offset) ? offset : Long.valueOf(0L);
  }

  @Override
  public Note getNoteAtPitch(Double pitch) {
    return tuning.note(pitch);
  }

  @Override
  public AudioFormat getOutputAudioFormat() throws HubException {
    return new AudioFormat(
      getOutputEncoding(),
      getOutputFrameRate(),
      getOutputSampleBits(),
      getOutputChannels(),
      getOutputChannels() * getOutputSampleBits() / 8,
      getOutputFrameRate(),
      false);
  }

  @Override
  public String getOutputFilePath() throws HubException {
    if (Objects.isNull(workbench.getSegment().getWaveformKey()))
      throw exception("Segment has no waveform key!");

    return String.format("%s%s", workTempFilePathPrefix, workbench.getSegment().getWaveformKey());
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
  public Program getProgram(SegmentChoice choice) throws HubException {
    return sourceMaterial.getProgram(choice.getProgramId());
  }

  @Override
  public SegmentChoice getPreviousMacroChoice() throws HubException {
    return retrospective.getPreviousChoiceOfType(ProgramType.Macro);
  }

  @Override
  public SegmentChoice getPreviousMainChoice() throws HubException {
    return retrospective.getPreviousChoiceOfType(ProgramType.Main);
  }

  @Override
  public Segment getPreviousSegment() throws HubException {
    if (isInitialSegment() || retrospective.getPreviousSegment().isEmpty())
      throw exception("Initial Segment has no previous Segment");

    return retrospective.getPreviousSegment().get();
  }

  @Override
  public Segment getSegment() {
    return workbench.getSegment();
  }

  @Override
  public Duration getSegmentTotalLength() throws HubException {
    if (Objects.isNull(workbench.getSegment().getEndAt()))
      throw exception("Cannot compute total length create segment with no end!");

    return Duration.between(workbench.getSegment().getBeginAt(), workbench.getSegment().getEndAt())
      .plusNanos((long) (workbench.getSegment().getWaveformPreroll() * NANOS_PER_SECOND));
  }

  @Override
  public ProgramSequence getSequence(SegmentChoice choice) throws HubException {
    Program program = getProgram(choice);
    if (Objects.nonNull(choice.getProgramSequenceBindingId()))
      return sourceMaterial.getProgramSequence(getSequenceBinding(choice).getProgramSequenceId());

    if (!sequenceForChoice.containsKey(choice))
      sequenceForChoice.put(choice, randomlySelectSequence(program));

    return sequenceForChoice.get(choice);
  }

  @Override
  public Long getSequenceBindingOffsetForChoice(SegmentChoice choice) throws HubException {
    if (Objects.isNull(choice.getProgramSequenceBindingId()))
      throw exception("Cannot determine SequenceBinding offset create choice with no SequenceBinding.");
    return getSequenceBinding(choice).getOffset();
  }

  @Override
  public Ingest getSourceMaterial() {
    return sourceMaterial;
  }

  @Override
  public SegmentType getType() throws HubException {
    if (Objects.isNull(type))
      type = determineType();
    return type;
  }

  @Override
  public boolean hasOneMoreSequenceBindingOffset(SegmentChoice choice) throws HubException {
    return hasMoreSequenceBindingOffsets(choice, 1);
  }

  @Override
  public boolean hasTwoMoreSequenceBindingOffsets(SegmentChoice choice) throws HubException {
    return hasMoreSequenceBindingOffsets(choice, 2);
  }

  @Override
  public Boolean isInitialSegment() {
    return workbench.getSegment().isInitial();
  }

  @Override
  public void putReport(String key, Object value) {
    workbench.putReport(key, value);
  }

  @Override
  public void done() throws HubException {
    workbench.getSegment().setTypeEnum(getType());
    try {
      workbench.done();
    } catch (HubException | RestApiException | ValueException e) {
      throw exception("Could not complete Segment fabrication", e);
    }
    switch (getType()) {
      case Continue:
        // transitions only once, of empty to non-empty
        log.info("[segId={}] continues main sequence create previous segments: {}",
          workbench.getSegment().getId(),
          fromIdsOf(getPreviousSegmentsWithSameMainProgram()));
        break;
      case Initial:
      case NextMain:
      case NextMacro:
        break;
    }
  }

  @Override
  public ProgramSequenceBinding randomlySelectSequenceBindingAtOffset(Program program, Long offset) throws HubException {
    EntityRank<ProgramSequenceBinding> entityRank = new EntityRank<>();
    for (ProgramSequenceBinding sequenceBinding : sourceMaterial.getProgramSequenceBindingsAtOffset(program, offset)) {
      entityRank.add(sequenceBinding, Chance.normallyAround(0.0, 1.0));
    }
    return entityRank.getTop();
  }

  @Override
  public ProgramSequence randomlySelectSequence(Program program) throws HubException {
    EntityRank<ProgramSequence> entityRank = new EntityRank<>();
    sourceMaterial.getAllProgramSequences().stream()
      .filter(s -> s.getProgramId().equals(program.getId()))
      .forEach(sequence -> entityRank.add(sequence, Chance.normallyAround(0.0, 1.0)));
    return entityRank.getTop();
  }

  @Override
  public Optional<ProgramSequencePattern> randomlySelectPatternOfSequenceByVoiceAndType(ProgramSequence sequence, ProgramVoice voice, ProgramSequencePatternType patternType) throws HubException {
    EntityRank<ProgramSequencePattern> rank = new EntityRank<>();
    sourceMaterial.getAllProgramPatterns().stream()
      .filter(pattern -> pattern.getProgramSequenceId().equals(sequence.getId()))
      .filter(pattern -> pattern.getProgramVoiceId().equals(voice.getId()))
      .filter(pattern -> pattern.getType() == patternType)
      .forEach(pattern ->
        rank.add(pattern, Chance.normallyAround(0.0, 1.0)));
    if (Objects.equals(0, rank.size()))
      return Optional.empty();
    return Optional.of(rank.getTop());
  }

  @Override
  public Collection<SegmentChoiceArrangementPick> getSegmentPicks() {
    return workbench.getSegmentPicks().getAll();
  }

  @Override
  public Collection<SegmentMeme> getSegmentMemes() {
    return workbench.getSegmentMemes().getAll();
  }

  @Override
  public Collection<SegmentChoice> getSegmentChoices() {
    return workbench.getSegmentChoices().getAll();
  }

  @Override
  public Collection<SegmentChoiceArrangement> getSegmentArrangements() {
    return workbench.getSegmentArrangements().getAll();
  }

  @Override
  public Collection<SegmentChoiceArrangement> getArrangements(SegmentChoice choice) {
    return getSegmentArrangements().stream().filter(arrangement -> choice.getId().equals(arrangement.getSegmentChoiceId())).collect(Collectors.toList());
  }

  /**
   Does the program of the specified Choice have at least N more sequence binding offsets available?

   @param choice of which to check the program for next available sequence binding offsets
   @param N      more sequence offsets to check for
   @return true if N more sequence binding offsets are available
   @throws HubException on failure
   */
  private boolean hasMoreSequenceBindingOffsets(SegmentChoice choice, int N) throws HubException {
    if (Objects.isNull(choice.getProgramSequenceBindingId()))
      throw exception("Cannot determine whether choice with no SequenceBinding has one more available Sequence Pattern offset");
    ProgramSequenceBinding sequenceBinding = getSequenceBinding(choice);

    Optional<Long> max = sourceMaterial.getAvailableOffsets(sequenceBinding).stream().max(Long::compareTo);
    return max.filter(aLong -> 0 <= aLong.compareTo(sequenceBinding.getOffset() + N)).isPresent();
  }

  /**
   Get default value for a given configuration type

   @param chainConfigType to get default value for
   @return default value for configuration type
   */
  private String getDefaultValue(ChainConfigType chainConfigType) throws HubException {
    try {
      return config.getString(String.format("chainConfig.default%s", chainConfigType));
    } catch (Exception ignored) {
      throw new HubException(String.format("No default value for type %s", this));
    }
  }

  /**
   Determine the type of fabricator

   @return type of fabricator
   @throws HubException on failure to determine
   */
  private SegmentType determineType() throws HubException {
    if (isInitialSegment())
      return SegmentType.Initial;

    // previous main choice having at least one more pattern?
    SegmentChoice previousMainChoice;
    try {
      previousMainChoice = getPreviousMainChoice();
    } catch (HubException e) {
      return SegmentType.Initial;
    }

    if (Objects.nonNull(previousMainChoice) && hasOneMoreSequenceBindingOffset(previousMainChoice))
      return SegmentType.Continue;

    // previous macro choice having at least two more patterns?
    SegmentChoice previousMacroChoice;
    try {
      previousMacroChoice = getPreviousMacroChoice();
    } catch (HubException e) {
      return SegmentType.Initial;
    }

    if (Objects.nonNull(previousMacroChoice) && hasTwoMoreSequenceBindingOffsets(previousMacroChoice))
      return SegmentType.NextMain;

    return SegmentType.NextMacro;
  }

  /**
   General a Segment URL

   @param chainId to generate URL for
   @return URL as string
   */
  private String generateWaveformKey(UUID chainId) throws HubException {
    String prefix = String.format("chains-%s-segments", chainId);
    String extension = getChainConfig(ChainConfigType.OutputContainer).getValue().toLowerCase(Locale.ENGLISH);
    return amazonProvider.generateKey(prefix, extension);
  }

  /**
   Get a Sequence Binding for a given Choice

   @param choice to get sequence binding for
   @return Sequence Binding for the given Choice
   @throws HubException on failure to locate the sequence binding for the specified choice
   */
  private ProgramSequenceBinding getSequenceBinding(SegmentChoice choice) throws HubException {
    return sourceMaterial.getProgramSequenceBinding(choice.getProgramSequenceBindingId());
  }

  /**
   [#255] Tuning based on root note configured in environment parameters.
   */
  private Tuning computeTuning() throws HubException {
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
  private HubException exception(String message) {
    return new HubException(formatLog(message));
  }

  /**
   Create a new CoreException prefixed with a segment id, including sub-exception

   @param message to include in exception
   @param e       Exception to include in exception
   @return CoreException to throw
   */
  private HubException exception(String message, Exception e) {
    return new HubException(formatLog(message), e);
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
  private TimeComputer getTimeComputer() {
    double toTempo = workbench.getSegment().getTempo(); // velocity at current segment tempo
    double fromTempo;
    try {
      Segment previous = getPreviousSegment();
      // velocity at previous segment tempo
      fromTempo = previous.getTempo();
    } catch (Exception ignored) {
      fromTempo = toTempo;
    }
    double totalBeats = workbench.getSegment().getTotal().doubleValue();
    putReport("totalBeats", totalBeats);
    putReport("fromTempo", fromTempo);
    putReport("toTempo", toTempo);
    return timeComputerFactory.create(totalBeats, fromTempo, toTempo);
  }

  /**
   real output channels based on chain configs

   @return output channels
   */
  private int getOutputChannels() throws HubException {
    return Integer.parseInt(getChainConfig(ChainConfigType.OutputChannels).getValue());
  }

  /**
   output encoding based on chain configs

   @return output encoding
   */
  private AudioFormat.Encoding getOutputEncoding() throws HubException {
    return new AudioFormat.Encoding(getChainConfig(ChainConfigType.OutputEncoding).getValue());
  }

  /**
   real output frame rate based on chain configs

   @return output frame rate, per second
   */
  private float getOutputFrameRate() throws HubException {
    return Integer.parseInt(getChainConfig(ChainConfigType.OutputFrameRate).getValue());
  }

  /**
   real output sample bits based on chain configs

   @return output sample bits
   */
  private int getOutputSampleBits() throws HubException {
    return Integer.parseInt(getChainConfig(ChainConfigType.OutputSampleBits).getValue());
  }

  /**
   Ensure the current segment has a waveform key; if not, add a waveform key to this Segment

   @throws HubException on failure to ensure generate waveform key
   */
  private void ensureWaveformKey() throws HubException {
    if (Objects.isNull(workbench.getSegment().getWaveformKey()) || workbench.getSegment().getWaveformKey().isEmpty()) {
      workbench.getSegment().setWaveformKey(generateWaveformKey(workbench.getSegment().getChainId()));
      log.info("[segId={}] Generated Waveform Key {}", workbench.getSegment().getId(), workbench.getSegment().getWaveformKey());
    }
  }

}
