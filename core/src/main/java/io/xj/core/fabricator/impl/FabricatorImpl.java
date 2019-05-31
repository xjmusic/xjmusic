// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.fabricator.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.ChainConfigDAO;
import io.xj.core.dao.ChainInstrumentDAO;
import io.xj.core.dao.ChainLibraryDAO;
import io.xj.core.dao.ChainSequenceDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorType;
import io.xj.core.ingest.Ingest;
import io.xj.core.ingest.cache.IngestCacheProvider;
import io.xj.core.isometry.Isometry;
import io.xj.core.isometry.MemeIsometry;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.chain_config.ChainConfig;
import io.xj.core.model.chain_config.ChainConfigType;
import io.xj.core.model.chain_instrument.ChainInstrument;
import io.xj.core.model.chain_library.ChainLibrary;
import io.xj.core.model.chain_sequence.ChainSequence;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.entity.EntityRank;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.library.Library;
import io.xj.core.model.meme.Meme;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.segment_message.SegmentMessage;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.sequence_pattern.SequencePattern;
import io.xj.core.transport.CSV;
import io.xj.core.util.Chance;
import io.xj.core.util.Value;
import io.xj.music.BPM;
import io.xj.music.Chord;
import io.xj.music.MusicalException;
import io.xj.music.Note;
import io.xj.music.Tuning;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioFormat;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class FabricatorImpl implements Fabricator {
  private static final double COMPUTE_INTEGRAL_DX = 0.25d; // # beats granularity to compute tempo change integral
  private static final double NANOS_PER_SECOND = 1000000000.0;
  private final ChainConfigDAO chainConfigDAO;
  private final ChainInstrumentDAO chainInstrumentDAO;
  private final ChainLibraryDAO chainLibraryDAO;
  private final ChainSequenceDAO chainSequenceDAO;
  private final Collection<Segment> cachedPreviousSegmentsWithThisMainSequence = Lists.newArrayList();
  private final IngestCacheProvider ingestProvider;
  private final Logger log = LoggerFactory.getLogger(FabricatorImpl.class);
  private final long startTime;

  private final Map<UUID, Collection<Arrangement>> presetChoiceArrangements = Maps.newConcurrentMap();

  private final SegmentDAO segmentDAO;
  private final Tuning tuning;
  // FUTURE: [#165815496] Chain fabrication access control
  private final Access access = Access.internal();
  private final Segment segment;
  private final Map<String, Segment> segmentByOffset = Maps.newConcurrentMap();
  private Ingest sourceMaterial;
  private Map<ChainConfigType, ChainConfig> allChainConfigs;
  private Collection<Segment> previousSegmentsWithSameMainSequence;

  @Inject
  public FabricatorImpl(
    @Assisted("segment") Segment segment,
    ChainConfigDAO chainConfigDAO,
    ChainLibraryDAO chainLibraryDAO,
    ChainSequenceDAO chainSequenceDAO,
    ChainInstrumentDAO chainInstrumentDAO,
    IngestCacheProvider ingestProvider,
    SegmentDAO segmentDAO
  ) throws CoreException {
    this.chainConfigDAO = chainConfigDAO;
    this.chainLibraryDAO = chainLibraryDAO;
    this.chainSequenceDAO = chainSequenceDAO;
    this.chainInstrumentDAO = chainInstrumentDAO;
    this.ingestProvider = ingestProvider;
    this.segmentDAO = segmentDAO;

    // Ingest Segment
    this.segment = segment;
    tuning = computeTuning();
    startTime = System.nanoTime();
  }

  @Override
  public Arrangement add(Arrangement arrangement) throws CoreException {
    return getSegment().add(arrangement);
  }

  @Override
  public Choice add(Choice choice) throws CoreException {
    return getSegment().add(choice);
  }

  @Override
  public Pick add(Pick pick) throws CoreException {
    return getSegment().add(pick);
  }

  @Override
  public SegmentChord add(SegmentChord segmentChord) throws CoreException {
    return getSegment().add(segmentChord);
  }

  @Override
  public SegmentMeme add(SegmentMeme segmentMeme) throws CoreException {
    return getSegment().add(segmentMeme);
  }

  @Override
  public SegmentMessage add(SegmentMessage segmentMessage) throws CoreException {
    return getSegment().add(segmentMessage);
  }

  @Override
  public Double computeSecondsAtPosition(double p) {
    double T = getSegment().getTotal().doubleValue();
    double v2 = BPM.velocity(getSegment().getTempo()); // velocity at current segment tempo

    double v1;
    try {
      Segment previous = getPreviousSegment();
      // velocity at previous segment tempo
      v1 = BPM.velocity(previous.getTempo());
    } catch (Exception ignored) {
      v1 = v2;
    }

    if (0 > p) {
      // before beginning, at tempo of previous segment
      return p * v1;

    } else if (T < p) {
      // after end, at tempo of current segment
      // recursively use same function to get end of current segment; can't infinitely recurse because T < T impossible
      return computeSecondsAtPosition(T) + ((p - T) * v2);

    } else {
      // TODO test the fuck out of this because it might be responsible for [#166370833] Segment should *never* be fabricated longer than its total beats.


      // computed by integral, smoothly fading from previous segment tempo to current
      double sum = 0.0d;
      Double x = 0.0d;
      Double dx = COMPUTE_INTEGRAL_DX;
      while (x < p) {
        sum += Math.min(dx, p - x) * // increment by dx, unless in the last (less than p-x) segment
          (v1 + (v2 - v1) * x / T);
        x += dx;
      }
      return sum;
    }
  }

  @Override
  public Access getAccess() {
    return access;
  }

  @Override
  public Collection<Entity> getAllAvailableEntities() throws CoreException {
    Collection<Entity> out = Lists.newArrayList();
    out.addAll(getAllAvailableLibraries());
    out.addAll(getAllAvailableSequences());
    out.addAll(getAllAvailableInstruments());
    return out;
  }

  @Override
  public Map<ChainConfigType, ChainConfig> getAllChainConfigs() {
    // this DAO read and subsequent mapping is cached, so it only happens once
    if (Objects.isNull(allChainConfigs)) {
      Map<ChainConfigType, ChainConfig> chainConfigs = Maps.newConcurrentMap();
      try {
        chainConfigDAO.readAll(getAccess(), ImmutableList.of(getChainId()))
          .forEach(record -> chainConfigs.put(
            record.getType(),
            record));
      } catch (CoreException e) {
        log.warn("Cannot read Chain configurations.", e);
      }
      allChainConfigs = Collections.unmodifiableMap(chainConfigs);
    }

    return allChainConfigs;
  }

  @Override
  public Collection<BigInteger> getAllSegmentAudioIds() throws CoreException {
    return ImmutableList.copyOf(getAllSegmentAudios().keySet());
  }

  @Override
  public Map<BigInteger, Audio> getAllSegmentAudios() throws CoreException {
    Map<BigInteger, Audio> segmentAudios = Maps.newConcurrentMap();
    for (Pick pick : getSegment().getPicks()) {
      segmentAudios.put(pick.getAudioId(), getSourceMaterial().getAudio(pick.getAudioId()));
    }
    return Collections.unmodifiableMap(segmentAudios);
  }

  @Override
  public ChainConfig getChainConfig(ChainConfigType chainConfigType) throws CoreException {
    if (getAllChainConfigs().containsKey(chainConfigType))
      return getAllChainConfigs().get(chainConfigType);

    try {
      return new ChainConfig()
        .setChainId(getChainId())
        .setTypeEnum(chainConfigType)
        .setValue(chainConfigType.defaultValue());
    } catch (CoreException e) {
      throw exception(String.format("No default value for chainConfigType=%s", chainConfigType), e);
    }
  }

  @Override
  public BigInteger getChainId() {
    return getSegment().getChainId();
  }

  @Override
  public Chord getChordAt(int position) {
    // default to returning a chord based on the segment key, if nothing else is found
    String foundChordText = getSegment().getKey();
    Double foundPosition = null;

    // we assume that these entities are in order of position ascending
    for (SegmentChord segmentChord : getSegment().getChords()) {
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
  public Choice getCurrentMacroChoice() throws CoreException {
    return getSegment().getChoiceOfType(SequenceType.Macro);
  }

  @Override
  public Pattern getCurrentMacroOffset() throws CoreException {
    return getSourceMaterial().fetchOnePattern(getRandomSequencePatternAtOffset(
      getSequenceOfChoice(getCurrentMacroChoice()).getId(),
      getSequencePatternOffsetForChoice(getCurrentMacroChoice())
    ).getPatternId());
  }

  @Override
  public Choice getCurrentMainChoice() throws CoreException {
    return getSegment().getChoiceOfType(SequenceType.Main);
  }

  @Override
  public Choice getCurrentRhythmChoice() throws CoreException {
    return getSegment().getChoiceOfType(SequenceType.Rhythm);
  }

  @Override
  public Double getElapsedSeconds() {
    return (System.nanoTime() - startTime) / NANOS_PER_SECOND;
  }

  @Override
  public Ingest getSourceMaterial() throws CoreException {
    if (Objects.isNull(sourceMaterial))
      sourceMaterial = ingestProvider.evaluate(getAccess(), getAllAvailableEntities());

    return sourceMaterial;
  }

  @Override
  public BigInteger getMaxAvailableSequencePatternOffset(Choice choice) throws CoreException {
    if (Objects.isNull(choice.getSequencePatternId()))
      throw exception("Cannot whether choice with no SequencePattern has two more available Sequence Pattern offsets");
    SequencePattern sequencePattern = getSourceMaterial().getSequencePattern(choice.getSequencePatternId());

    BigInteger maxOffset = BigInteger.ZERO;
    for (BigInteger offset : getSourceMaterial().getAvailableSequencePatternOffsets(sequencePattern.getSequenceId())) {
      if (0 < offset.compareTo(maxOffset)) {
        maxOffset = offset;
      }
    }
    return maxOffset;
  }

  @Override
  public Map<String, Collection<Arrangement>> getMemeConstellationArrangementsOfPreviousSegment() {
    Map<String, Collection<Arrangement>> out = Maps.newConcurrentMap();
    getMemeConstellationChoicesOfPreviousSegment().forEach((constellation, previousChoices) -> {
      out.put(constellation, Lists.newArrayList());
      previousChoices.forEach(choice -> getSegment().getArrangementsForChoice(choice).forEach(arrangement -> out.get(constellation).add(arrangement)));
    });
    return out;
  }

  @Override
  public Map<String, Collection<Choice>> getMemeConstellationChoicesOfPreviousSegment() {
    Map<String, Collection<Choice>> out = Maps.newConcurrentMap();
    for (Segment seg : getPreviousSegmentsWithSameMainSequence()) {
      Isometry iso = MemeIsometry.ofMemes(seg.getMemes());
      String con = iso.getConstellation();
      out.put(con, seg.getChoices());
    }
    return out;
  }

  @Override
  public Map<String, Collection<Pick>> getMemeConstellationPicksOfPreviousSegment() {
    Map<String, Collection<Pick>> out = Maps.newConcurrentMap();
    for (Segment seg : getPreviousSegmentsWithSameMainSequence()) {
      Isometry iso = MemeIsometry.ofMemes(seg.getMemes());
      String con = iso.getConstellation();
      out.put(con, seg.getPicks());
    }
    return out;
  }

  @Override
  public MemeIsometry getMemeIsometryOfCurrentMacro() throws CoreException {
    return MemeIsometry.ofMemes(
      getSourceMaterial().getMemesAtBeginningOfSequence(
        getSequenceOfChoice(getCurrentMacroChoice()).getId()
      ));
  }

  @Override
  public MemeIsometry getMemeIsometryOfNextPatternInPreviousMacro() {
    try {
      BigInteger previousSequenceId = getSequenceOfChoice(getPreviousMacroChoice()).getId();
      return MemeIsometry.ofMemes(getSourceMaterial().getMemesAtBeginningOfSequence(previousSequenceId));
    } catch (Exception ignored) {
      return new MemeIsometry();
    }
  }

  @Override
  public MemeIsometry getMemeIsometryOfSegment() {
    return MemeIsometry.ofMemes(getSegment().getMemes());
  }

  @Override
  public Collection<Meme> getMemesOfChoice(Choice choice) throws CoreException {
    Collection<Meme> result = Lists.newArrayList();
    switch (choice.getType()) {
      case Macro:
      case Main:
        result.addAll(getSourceMaterial().getSequenceMemesOfSequence(getSourceMaterial().getSequencePattern(choice.getSequencePatternId()).getSequenceId()));
        result.addAll(getSourceMaterial().getSequencePatternMemesOfSequencePattern(choice.getSequencePatternId()));
        break;

      case Rhythm:
      case Detail:
        result.addAll(getSourceMaterial().getSequenceMemesOfSequence(choice.getSequenceId()));
        break;

      default:
        throw exception(String.format("Cannot get Sequence sequence of unknown Choice.type=", choice.getType()));
    }
    return result;
  }

  @Override
  public BigInteger getNextSequencePatternOffset(Choice choice) throws CoreException {
    if (Objects.isNull(choice.getSequencePatternId()))
      throw exception("Cannot determine next available SequencePattern offset of choice with no SequencePattern.");

    SequencePattern sequencePattern = getSourceMaterial().getSequencePattern(choice.getSequencePatternId());
    BigInteger sequencePatternOffset = getSequencePatternOffsetForChoice(choice);
    BigInteger offset = null;
    for (BigInteger availableOffset : getSourceMaterial().getAvailableSequencePatternOffsets(sequencePattern.getSequenceId()))
      if (0 < availableOffset.compareTo(sequencePatternOffset))
        if (Objects.isNull(offset) ||
          0 > availableOffset.compareTo(offset))
          offset = availableOffset;

    // if none found, loop back around to zero
    return Objects.nonNull(offset) ? offset : BigInteger.valueOf(0L);
  }

  @Override
  public Note getNoteAtPitch(Double pitch) {
    return tuning.note(pitch);
  }

  @Override
  public AudioFormat getOutputAudioFormat() throws CoreException {
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
  public String getOutputFilePath() throws CoreException {
    if (Objects.isNull(getSegment().getWaveformKey()))
      throw exception("Segment has no waveform key!");

    return Config.workTempFilePathPrefix() + getSegment().getWaveformKey();
  }

  @Override
  public Double getPitch(Note note) {
    return tuning.pitch(note);
  }

  @Override
  public Map<UUID, Collection<Arrangement>> getPresetChoiceArrangements() {
    return presetChoiceArrangements;
  }

  @Override
  public Choice getPreviousMacroChoice() throws CoreException {
    return getPreviousSegment().getChoiceOfType(SequenceType.Macro);
  }

  @Override
  public Pattern getPreviousMacroNextOffset() throws CoreException {
    Choice previousChoice = getPreviousMacroChoice();
    if (Objects.isNull(previousChoice))
      throw exception("Previous segment macro-choice has no next offset!");
    return getSourceMaterial().fetchOnePattern(getRandomSequencePatternAtOffset(
      getSequenceOfChoice(previousChoice).getId(),
      getNextSequencePatternOffset(previousChoice)
    ).getPatternId());
  }

  @Override
  public Choice getPreviousMainChoice() throws CoreException {
    return getPreviousSegment().getChoiceOfType(SequenceType.Main);
  }

  @Override
  public Segment getPreviousSegment() throws CoreException {
    if (isInitialSegment()) throw exception("Initial Segment has no previous Segment");

    return getSegmentByOffset(getChainId(), Value.inc(getSegment().getOffset(), -1));
  }

  @Override
  public Collection<Segment> getPreviousSegmentsWithSameMainSequence() {
    try {
      // if there isn't a main choice yet, don't even get to the part that might be cached
      Choice mainChoice = getCurrentMainChoice();
      if (Objects.isNull(mainChoice)) {
        return Lists.newArrayList();
      }

      // now this DAO read is cached, so it only happens once
      if (Objects.isNull(previousSegmentsWithSameMainSequence)) {
        BigInteger sequencePatternOffset = getSequencePatternOffsetForChoice(mainChoice);
        if (0 < sequencePatternOffset.compareTo(BigInteger.ZERO)) {
          BigInteger oF = getSegment().getOffset().subtract(sequencePatternOffset);
          BigInteger oT = getSegment().getOffset().subtract(BigInteger.ONE);
          if (0 > oF.compareTo(BigInteger.ZERO) || 0 > oT.compareTo(BigInteger.ZERO)) {
            previousSegmentsWithSameMainSequence = Lists.newArrayList();
          }
          previousSegmentsWithSameMainSequence = segmentDAO.readAllFromToOffset(getAccess(), getChainId(), oF, oT);
        } else {
          previousSegmentsWithSameMainSequence = Lists.newArrayList();
        }
      }
      return previousSegmentsWithSameMainSequence;

    } catch (CoreException e) {
      return Lists.newArrayList();
    }
  }

  @Override
  public Pattern getRandomPatternByType(BigInteger sequenceId, PatternType patternType) throws CoreException {
    EntityRank<Pattern> entityRank = new EntityRank<>();
    for (Pattern pattern : getSourceMaterial().getPatternsOfSequence(sequenceId)) {
      if (Objects.nonNull(pattern) && pattern.getType() == patternType) {
        entityRank.add(pattern, Chance.normallyAround(0.0, 1.0));
      }
    }
    try {
      return entityRank.getTop();
    } catch (CoreException e) {
      throw exception(String.format("No candidate Pattern of sequenceId=%s by patternType=%s", sequenceId, patternType), e);
    }
  }

  @Override
  public SequencePattern getRandomSequencePatternAtOffset(BigInteger sequenceId, BigInteger sequencePatternOffset) throws CoreException {
    EntityRank<SequencePattern> entityRank = new EntityRank<>();
    for (SequencePattern sequencePattern : getSourceMaterial().getSequencePatternsOfSequenceAtOffset(sequenceId, sequencePatternOffset)) {
      entityRank.add(sequencePattern, Chance.normallyAround(0.0, 1.0));
    }
    try {
      return entityRank.getTop();
    } catch (CoreException e) {
      throw exception(String.format("No candidate SequencePattern of sequenceId=%s at sequencePatternOffset=%s", sequenceId, sequencePatternOffset), e);
    }
  }

  @Override
  public Segment getSegment() {
    return segment;
  }

  @Override
  public Audio getSegmentAudio(BigInteger audioId) throws CoreException {
    if (getAllSegmentAudios().containsKey(audioId))
      return getAllSegmentAudios().get(audioId);

    throw exception(String.format("Audio #%s is not in picks!", audioId.toString()));
  }

  @Override
  public Timestamp getSegmentBeginAt() {
    return getSegment().getBeginAt();
  }

  @Override
  public Segment getSegmentByOffset(BigInteger chainId, BigInteger offset) throws CoreException {
    String key = String.format("%s_%s", chainId, offset);
    if (!segmentByOffset.containsKey(key)) {
      try {
        segmentByOffset.put(key, segmentDAO.readOneAtChainOffset(getAccess(), chainId, offset));
      } catch (CoreException e) {
        throw exception(String.format("Could not retrieve segment at chainId=%s, offset=%s", chainId, offset), e);
      }
    }

    return segmentByOffset.get(key);
  }

  @Override
  public Duration getSegmentTotalLength() throws CoreException {
    if (Objects.isNull(getSegment().getEndAt()))
      throw exception("Cannot compute total length of segment with no end!");

    return Duration.ofMillis(getSegment().getEndAt().getTime() - getSegment().getBeginAt().getTime());
  }

  @Override
  public Sequence getSequenceOfChoice(Choice choice) throws CoreException {
    switch (choice.getType()) {
      case Macro:
      case Main:
        return getSourceMaterial().getSequence(getSourceMaterial().getSequencePattern(choice.getSequencePatternId()).getSequenceId());
      case Rhythm:
      case Detail:
        return getSourceMaterial().getSequence(choice.getSequenceId());

      default:
        throw exception(String.format("Cannot get Sequence sequence of unknown Choice.type=", choice.getType()));
    }
  }

  @Override
  public BigInteger getSequencePatternOffsetForChoice(Choice choice) throws CoreException {
    if (Objects.isNull(choice.getSequencePatternId()))
      throw exception("Cannot determine SequencePattern offset of choice with no SequencePattern.");
    return getSourceMaterial().getSequencePattern(choice.getSequencePatternId()).getOffset();
  }

  @Override
  public FabricatorType getType() throws CoreException {
    if (isInitialSegment()) {
      return FabricatorType.Initial;
    }

    // previous main choice having at least one more pattern?
    Choice previousMainChoice;
    try {
      previousMainChoice = getPreviousMainChoice();
    } catch (CoreException e) {
      return FabricatorType.Initial;
    }
    if (Objects.nonNull(previousMainChoice) && hasOneMoreSequencePatternOffset(previousMainChoice)) {
      return FabricatorType.Continue;
    }

    // previous macro choice having at least two more patterns?
    Choice previousMacroChoice;
    try {
      previousMacroChoice = getPreviousMacroChoice();
    } catch (CoreException e) {
      return FabricatorType.Initial;
    }
    if (Objects.nonNull(previousMacroChoice) && hasTwoMoreSequencePatternOffsets(previousMacroChoice)) {
      return FabricatorType.NextMain;
    }

    return FabricatorType.NextMacro;
  }

  @Override
  public boolean hasOneMoreSequencePatternOffset(Choice choice) throws CoreException {
    if (Objects.isNull(choice.getSequencePatternId()))
      throw exception("Cannot whether choice with no SequencePattern has one more available Sequence Pattern offset");
    SequencePattern sequencePattern = getSourceMaterial().getSequencePattern(choice.getSequencePatternId());

    return getSourceMaterial().getAvailableSequencePatternOffsets(sequencePattern.getSequenceId()).stream()
      .anyMatch(availableOffset -> 0 < availableOffset.compareTo(sequencePattern.getOffset()));
  }

  @Override
  public boolean hasTwoMoreSequencePatternOffsets(Choice choice) throws CoreException {
    if (Objects.isNull(choice.getSequencePatternId()))
      throw exception("Cannot whether choice with no SequencePattern has two more available Sequence Pattern offsets");
    SequencePattern sequencePattern = getSourceMaterial().getSequencePattern(choice.getSequencePatternId());

    int num = 0;
    for (BigInteger availableOffset : getSourceMaterial().getAvailableSequencePatternOffsets(sequencePattern.getSequenceId()))
      if (0 < availableOffset.compareTo(sequencePattern.getOffset())) {
        num++;
        if (2 <= num)
          return true;
      }
    return false;
  }

  @Override
  public Boolean isInitialSegment() {
    return getSegment().isInitial();
  }

  @Override
  public void putReport(String key, String value) {
    getSegment().getReport().put(key, value);
  }

  @Override
  public void setPreArrangementsForChoice(Choice choice, Collection<Arrangement> arrangements) {
    presetChoiceArrangements.put(choice.getUuid(), arrangements);
  }

  @Override
  public void updateSegment() throws CoreException {
    Segment updatedSegment = getSegment();
    updatedSegment.setTypeEnum(getType());
    try {
      segmentDAO.update(getAccess(), getSegment().getId(), updatedSegment);
    } catch (CoreException e) {
      throw exception("Could not update Segment", e);
    }
    switch (getType()) {
      case Continue:
        // transitions only once, from empty to non-empty
        cachedPreviousSegmentsWithThisMainSequence.clear();
        cachedPreviousSegmentsWithThisMainSequence.addAll(getPreviousSegmentsWithSameMainSequence());
        log.info("[segId={}] continues main sequence of previous segments: {}",
          segment.getId(),
          CSV.fromIdsOf(cachedPreviousSegmentsWithThisMainSequence));
        break;
      case Initial:
      case NextMain:
      case NextMacro:
        break;
    }
  }

  /**
   [#255] Tuning based on root note configured in environment parameters.
   */
  private Tuning computeTuning() throws CoreException {
    try {
      return Tuning.at(
        Note.of(Config.tuningRootNote()),
        Config.tuningRootPitch());
    } catch (MusicalException e) {
      throw exception("Could not tune XJ!", e);
    }
  }

  /**
   Create a new CoreException prefixed with a segment id

   @param message to include in exception
   @return CoreException to throw
   */
  public CoreException exception(String message) {
    return new CoreException(formatLog(message));
  }

  /**
   Create a new CoreException prefixed with a segment id, including sub-exception

   @param message to include in exception
   @param e       Exception to include in exception
   @return CoreException to throw
   */
  public CoreException exception(String message, Exception e) {
    return new CoreException(formatLog(message), e);
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
   Get all instruments bound to chain.
   CACHES results.

   @return instruments bound to chain.
   */
  private Collection<Instrument> getAllAvailableInstruments() throws CoreException {
    ImmutableList.Builder<Instrument> builder = ImmutableList.builder();
    getAllChainInstruments().forEach(chainInstrument -> builder.add(new Instrument(chainInstrument.getInstrumentId())));
    return builder.build();
  }

  /**
   Get all libraries bound to chain.
   CACHES results.

   @return libraries bound to chain.
   */
  private Collection<Library> getAllAvailableLibraries() throws CoreException {
    ImmutableList.Builder<Library> builder = ImmutableList.builder();
    getAllChainLibraries().forEach(chainLibrary -> builder.add(new Library(chainLibrary.getLibraryId())));
    return builder.build();
  }

  /**
   Get all sequences bound to chain.
   CACHES results.

   @return sequences bound to chain.
   */
  private Collection<Sequence> getAllAvailableSequences() throws CoreException {
    ImmutableList.Builder<Sequence> builder = ImmutableList.builder();
    getAllChainSequences().forEach(chainSequence -> builder.add(new Sequence(chainSequence.getSequenceId())));
    return builder.build();
  }

  /**
   Get ChainInstrument bindings for the current chain
   We don't bother caching these, because they're only called once anyway, after their product (ingest) is cached.

   @return collection of ChainInstrument
   */
  private Collection<ChainInstrument> getAllChainInstruments() throws CoreException {
    try {
      return chainInstrumentDAO.readAll(getAccess(), ImmutableList.of(getChainId()));
    } catch (CoreException e) {
      throw exception("Could not read Chain Instruments", e);
    }
  }

  /**
   Get ChainLibrary bindings for the current chain
   We don't bother caching these, because they're only called once anyway, after their product (ingest) is cached.

   @return collection of ChainLibrary
   */
  private Collection<ChainLibrary> getAllChainLibraries() throws CoreException {
    try {
      return chainLibraryDAO.readAll(getAccess(), ImmutableList.of(getChainId()));
    } catch (CoreException e) {
      throw exception("Could not read Chain Libraries", e);
    }
  }

  /**
   Get ChainSequence bindings for the current chain
   We don't bother caching these, because they're only called once anyway, after their product (ingest) is cached.

   @return collection of ChainSequence
   */
  private Collection<ChainSequence> getAllChainSequences() throws CoreException {
    try {
      return chainSequenceDAO.readAll(getAccess(), ImmutableList.of(getChainId()));
    } catch (CoreException e) {
      throw exception("Could not read Chain Sequences", e);
    }
  }

  /**
   real output channels based on chain configs

   @return output channels
   */
  private int getOutputChannels() throws CoreException {
    return Integer.parseInt(getChainConfig(ChainConfigType.OutputChannels).getValue());
  }

  /**
   output encoding based on chain configs

   @return output encoding
   */
  private AudioFormat.Encoding getOutputEncoding() throws CoreException {
    return new AudioFormat.Encoding(getChainConfig(ChainConfigType.OutputEncoding).getValue());
  }

  /**
   real output frame rate based on chain configs

   @return output frame rate, per second
   */
  private float getOutputFrameRate() throws CoreException {
    return Integer.parseInt(getChainConfig(ChainConfigType.OutputFrameRate).getValue());
  }

  /**
   real output sample bits based on chain configs

   @return output sample bits
   */
  private int getOutputSampleBits() throws CoreException {
    return Integer.parseInt(getChainConfig(ChainConfigType.OutputSampleBits).getValue());
  }

}
