// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.basis.impl;

import io.xj.core.access.impl.Access;
import io.xj.craft.basis.Basis;
import io.xj.craft.basis.BasisType;
import io.xj.craft.ingest.cache.IngestCacheProvider;
import io.xj.core.config.Config;
import io.xj.core.dao.ArrangementDAO;
import io.xj.core.dao.ChainConfigDAO;
import io.xj.core.dao.ChainInstrumentDAO;
import io.xj.core.dao.ChainLibraryDAO;
import io.xj.core.dao.ChainSequenceDAO;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.dao.SegmentChordDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.dao.SegmentMemeDAO;
import io.xj.core.dao.SegmentMessageDAO;
import io.xj.craft.ingest.Ingest;
import io.xj.core.exception.BusinessException;
import io.xj.craft.isometry.MemeIsometry;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.chain_config.ChainConfig;
import io.xj.core.model.chain_config.ChainConfigType;
import io.xj.core.model.chain_instrument.ChainInstrument;
import io.xj.core.model.chain_library.ChainLibrary;
import io.xj.core.model.chain_sequence.ChainSequence;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.library.Library;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment_chord.SegmentChord;
import io.xj.core.model.segment_meme.SegmentMeme;
import io.xj.core.model.segment_message.SegmentMessage;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.sequence.Sequence;
import io.xj.core.model.sequence.SequenceType;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.pick.Pick;
import io.xj.core.util.Value;
import io.xj.music.BPM;
import io.xj.music.Chord;
import io.xj.music.MusicalException;
import io.xj.music.Note;
import io.xj.music.Tuning;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.sound.sampled.AudioFormat;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 [#214] If a Chain has Sequences associated with it directly, prefer those choices to any in the Library
 */
public class BasisImpl implements Basis {
  private static final int MICROSECONDS_PER_SECOND = 1000000;
  private static final double COMPUTE_INTEGRAL_DX = 0.25d; // # beats granularity to compute tempo change integral
  private static final double NANOS_PER_SECOND = 1000000000.0;
  private final ArrangementDAO arrangementDAO;
  private final ChainConfigDAO chainConfigDAO;
  private final ChainLibraryDAO chainLibraryDAO;
  private final ChainSequenceDAO chainSequenceDAO;
  private final ChainInstrumentDAO chainInstrumentDAO;
  private final ChoiceDAO choiceDAO;
  private final IngestCacheProvider ingestProvider;
  private final SegmentChordDAO segmentChordDAO;
  private final SegmentDAO segmentDAO;
  private final SegmentMemeDAO segmentMemeDAO;
  private final SegmentMessageDAO segmentMessageDAO;
  private final Logger log = LoggerFactory.getLogger(BasisImpl.class);
  private final Map<String, Object> report = Maps.newConcurrentMap();
  private final Tuning tuning;
  private final long startTime;
  private final List<Pick> _picks = Lists.newArrayList();
  private final Map<BigInteger, Collection<Arrangement>> _choiceArrangements = Maps.newConcurrentMap();
  private final Map<BigInteger, Collection<SegmentMeme>> _segmentMemes = Maps.newConcurrentMap();
  private final Map<BigInteger, Map<BigInteger, Segment>> _segmentsByOffset = Maps.newConcurrentMap();
  private final Map<BigInteger, Map<SequenceType, Choice>> _segmentChoicesByType = Maps.newConcurrentMap();
  private final Map<Double, Double> _positionSeconds = Maps.newConcurrentMap();
  private BasisType _type;
  private Boolean _sentReport = false;
  private Segment _segment;
  private Collection<SegmentChord> _segmentChords;
  private Map<ChainConfigType, ChainConfig> _chainConfigs;
  private Map<BigInteger, Audio> _audiosFromPicks;
  private MemeIsometry _currentMacroMemeIsometry;
  private MemeIsometry _previousMacroMemeIsometry;
  private MemeIsometry _currentSegmentMemeIsometry;
  private Collection<SegmentMeme> _currentSegmentMemes;
  private Ingest _ingest;
  private Ingest _libraryIngest;

  @Inject
  public BasisImpl(
    @Assisted("segment") Segment segment,
    ArrangementDAO arrangementDAO,
    ChainConfigDAO chainConfigDAO,
    ChainLibraryDAO chainLibraryDAO,
    ChainSequenceDAO chainSequenceDAO,
    ChainInstrumentDAO chainInstrumentDAO,
    ChoiceDAO choiceDAO,
    IngestCacheProvider ingestProvider,
    SegmentChordDAO segmentChordDAO,
    SegmentDAO segmentDAO,
    SegmentMemeDAO segmentMemeDAO,
    SegmentMessageDAO segmentMessageDAO
  /*-*/) throws BusinessException {
    _segment = segment;
    this.arrangementDAO = arrangementDAO;
    this.chainConfigDAO = chainConfigDAO;
    this.chainLibraryDAO = chainLibraryDAO;
    this.chainSequenceDAO = chainSequenceDAO;
    this.chainInstrumentDAO = chainInstrumentDAO;
    this.choiceDAO = choiceDAO;
    this.ingestProvider = ingestProvider;
    this.segmentChordDAO = segmentChordDAO;
    this.segmentDAO = segmentDAO;
    this.segmentMemeDAO = segmentMemeDAO;
    this.segmentMessageDAO = segmentMessageDAO;

    // [#255] Tuning based on root note configured in environment parameters.
    try {
      tuning = Tuning.at(
        Note.of(Config.tuningRootNote()),
        Config.tuningRootPitch());
    } catch (MusicalException e) {
      throw new BusinessException("Could not tune XJ!", e);
    }

    startTime = System.nanoTime();
  }

  @Override
  public String outputFilePath() throws BusinessException {
    if (Objects.isNull(segment().getWaveformKey()))
      throw new BusinessException("Segment has no waveform key!");

    return Config.workTempFilePathPrefix() + segment().getWaveformKey();
  }

  @Override
  public AudioFormat outputAudioFormat() throws Exception {
    return new AudioFormat(
      outputEncoding(),
      outputFrameRate(),
      outputSampleBits(),
      outputChannels(),
      outputChannels() * outputSampleBits() / 8,
      outputFrameRate(),
      false);
  }


  @Override
  public BasisType type() {
    if (Objects.isNull(_type))
      try {
        if (isInitialSegment())
          _type = BasisType.Initial;
        else if (previousMainChoice().hasOneMorePattern())
          _type = BasisType.Continue;
        else if (previousMacroChoice().hasTwoMorePatterns())
          _type = BasisType.NextMain;
        else
          _type = BasisType.NextMacro;

      } catch (Exception e) {
        log.warn("Failed to determine type! {}", e.getMessage(), e);
      }

    return _type;
  }

  @Override
  public Ingest ingest() throws Exception {
    if (Objects.isNull(_ingest))
      _ingest = ingestProvider.evaluate(Access.internal(), entitiesBoundToChain());

    return _ingest;
  }

  @Override
  public Ingest libraryIngest() throws Exception {
    if (Objects.isNull(_libraryIngest))
      _libraryIngest = ingestProvider.evaluate(Access.internal(), entitiesBoundToChainOrInferred());

    return _libraryIngest;
  }

  /**
   Get all entities bound to chain.

   @return collection of all entities bound to chain
   */
  private Collection<Entity> entitiesBoundToChain() throws Exception {
    Collection<Entity> result = Lists.newArrayList();
    result.addAll(librariesBoundToChain());
    result.addAll(sequencesBoundToChain());
    result.addAll(instrumentsBoundToChain());
    return result;
  }

  /**
   Get all libraries bound to chain.
   CACHES results.

   @return libraries bound to chain.
   */
  private Collection<Library> librariesBoundToChain() throws Exception {
    ImmutableList.Builder<Library> builder = ImmutableList.builder();
    chainLibraries().forEach(chainLibrary -> builder.add(new Library(chainLibrary.getLibraryId())));
    return builder.build();
  }

  /**
   Get ChainLibrary bindings for the current chain

   @return collection of ChainLibrary
   */
  private Collection<ChainLibrary> chainLibraries() throws Exception {
    return chainLibraryDAO.readAll(Access.internal(), ImmutableList.of(chainId()));
  }

  /**
   Get all sequences bound to chain.
   CACHES results.

   @return sequences bound to chain.
   */
  private Collection<Sequence> sequencesBoundToChain() throws Exception {
    ImmutableList.Builder<Sequence> builder = ImmutableList.builder();
    chainSequences().forEach(chainSequence -> builder.add(new Sequence(chainSequence.getSequenceId())));
    return builder.build();
  }

  /**
   Get ChainSequence bindings for the current chain

   @return collection of ChainSequence
   */
  private Collection<ChainSequence> chainSequences() throws Exception {
    return chainSequenceDAO.readAll(Access.internal(), ImmutableList.of(chainId()));
  }

  /**
   Get all instruments bound to chain.
   CACHES results.

   @return instruments bound to chain.
   */
  private Collection<Instrument> instrumentsBoundToChain() throws Exception {
    ImmutableList.Builder<Instrument> builder = ImmutableList.builder();
    chainInstruments().forEach(chainInstrument -> builder.add(new Instrument(chainInstrument.getInstrumentId())));
    return builder.build();
  }

  /**
   Get ChainInstrument bindings for the current chain

   @return collection of ChainInstrument
   */
  private Collection<ChainInstrument> chainInstruments() throws Exception {
    return chainInstrumentDAO.readAll(Access.internal(), ImmutableList.of(chainId()));
  }

  /**
   Get all entities bound to chain.
   NOT cached, because we assume that this information will only need to be used once, in order to compute an Ingest, which is cached.

   @return collection of all entities bound to chain
   */
  private Collection<Entity> entitiesBoundToChainOrInferred() throws Exception {
    Map<BigInteger, Entity> result = Maps.newConcurrentMap();
    librariesBoundToChain().forEach(library -> result.put(library.getId(), library));
    sequencesBoundToChain().forEach(sequence -> result.put(sequence.getLibraryId(), new Library(sequence.getLibraryId())));
    instrumentsBoundToChain().forEach(instrument -> result.put(instrument.getLibraryId(), new Library(instrument.getLibraryId())));
    return result.values();
  }

  @Override
  public Segment segment() {
    return _segment;
  }

  @Override
  public Boolean isInitialSegment() {
    return _segment.isInitial();
  }

  @Override
  public BigInteger chainId() {
    return _segment.getChainId();
  }

  @Override
  public ChainConfig chainConfig(ChainConfigType chainConfigType) throws Exception {
    if (chainConfigs().containsKey(chainConfigType))
      return chainConfigs().get(chainConfigType);

    return new ChainConfig()
      .setChainId(chainId())
      .setTypeEnum(chainConfigType)
      .setValue(chainConfigType.defaultValue());
  }

  @Override
  public Timestamp segmentBeginAt() {
    return _segment.getBeginAt();
  }

  @Override
  public Segment previousSegment() throws Exception {
    if (isInitialSegment()) return null;

    return segmentByOffset(chainId(), Value.inc(_segment.getOffset(), -1));
  }

  @Override
  public Collection<SegmentMeme> previousSegmentMemes() throws Exception {
    if (isInitialSegment()) return null;

    Segment previousSegment = previousSegment();
    if (Objects.isNull(previousSegment)) return Lists.newArrayList();

    return segmentMemes(previousSegment.getId());
  }

  @Override
  public Choice previousMacroChoice() throws Exception {
    return isInitialSegment() ? null : segmentChoiceByType(previousSegment().getId(), SequenceType.Macro);
  }

  @Override
  public Choice previousMainChoice() throws Exception {
    return isInitialSegment() ? null : segmentChoiceByType(previousSegment().getId(), SequenceType.Main);
  }

  @Override
  public Choice previousRhythmChoice() throws Exception {
    return isInitialSegment() ? null : segmentChoiceByType(previousSegment().getId(), SequenceType.Rhythm);
  }

  @Override
  public Collection<Arrangement> previousPercussiveArrangements() throws Exception {
    return isInitialSegment() ? null : choiceArrangements(previousRhythmChoice().getId());
  }

  @Override
  public Choice currentMacroChoice() throws Exception {
    return segmentChoiceByType(segment().getId(), SequenceType.Macro);
  }

  @Override
  public Choice currentMainChoice() throws Exception {
    return segmentChoiceByType(segment().getId(), SequenceType.Main);
  }

  @Override
  public Choice currentRhythmChoice() throws Exception {
    return segmentChoiceByType(segment().getId(), SequenceType.Rhythm);
  }

  @Override
  public Pattern currentMacroPattern() throws Exception {
    return ingest().patternAtOffset(
      currentMacroChoice().getSequenceId(),
      currentMacroChoice().getSequencePatternOffset(),
      PatternType.Macro);
  }

  @Override
  public Pattern previousMacroNextPattern() throws Exception {
    return isInitialSegment() ? null : ingest().patternAtOffset(
      previousMacroChoice().getSequenceId(),
      previousMacroChoice().nextPatternOffset(),
      PatternType.Macro);
  }

  @Override
  public Map<ChainConfigType, ChainConfig> chainConfigs() throws Exception {
    if (Objects.isNull(_chainConfigs) || _chainConfigs.isEmpty()) {
      _chainConfigs = Maps.newConcurrentMap();
      chainConfigDAO.readAll(Access.internal(), ImmutableList.of(chainId()))
        .forEach(record -> _chainConfigs.put(
          record.getType(),
          record));
    }

    return Collections.unmodifiableMap(_chainConfigs);
  }

  @Override
  public Collection<Arrangement> choiceArrangements(BigInteger choiceId) throws Exception {
    if (!_choiceArrangements.containsKey(choiceId)) {
      _choiceArrangements.put(choiceId, arrangementDAO.readAll(Access.internal(), ImmutableList.of(choiceId)));
    }

    return Collections.unmodifiableCollection(_choiceArrangements.get(choiceId));
  }

  @Override
  public void setChoiceArrangements(BigInteger choiceId, Collection<Arrangement> arrangements) {
    _choiceArrangements.put(choiceId, arrangements);
  }

  @Override
  public Chord chordAt(int position) throws Exception {
    // default to returning a chord based on the segment key, if nothing else is found
    Chord foundChord = Chord.of(segment().getKey());
    Double foundPosition = null;

    // we assume that these entities are in order of position ascending (see: SegmentChordDAO.readAllExpectedWork)
    for (SegmentChord segmentChord : segmentChords()) {
      // if it's a better match (or no match has yet been found) then use it
      if (Objects.isNull(foundPosition) ||
        segmentChord.getPosition() > foundPosition && segmentChord.getPosition() < position) {
        foundPosition = segmentChord.getPosition();
        foundChord = Chord.of(segmentChord.getName());
      }
    }

    return foundChord;
  }

  @Override
  public Double pitch(Note note) {
    return tuning.pitch(note);
  }

  @Override
  public Note note(Double pitch) {
    return tuning.note(pitch);
  }

  @Override
  public Double secondsAtPosition(double p) throws Exception {
    if (!_positionSeconds.containsKey(p))
      _positionSeconds.put(p, computeIntegralSecondsAtPosition(p));

    return _positionSeconds.get(p);
  }

  @Override
  public Collection<SegmentMeme> segmentMemes(BigInteger segmentId) throws Exception {
    if (!_segmentMemes.containsKey(segmentId))
      _segmentMemes.put(segmentId, segmentMemeDAO.readAll(Access.internal(), ImmutableList.of(segmentId)));

    return _segmentMemes.get(segmentId);
  }

  @Override
  public Audio segmentAudio(BigInteger audioId) throws Exception {
    if (segmentAudios().containsKey(audioId))
      return segmentAudios().get(audioId);

    throw new BusinessException(String.format("Audio #%s is not in segment picks!", audioId.toString()));
  }

  @Override
  public Map<BigInteger, Audio> segmentAudios() throws Exception {
    if (Objects.isNull(_audiosFromPicks) || _audiosFromPicks.isEmpty()) {
      _audiosFromPicks = Maps.newConcurrentMap();
      for (Pick pick : _picks) {
        _audiosFromPicks.put(pick.getAudioId(), ingest().audio(pick.getAudioId()));
      }
    }

    return Collections.unmodifiableMap(_audiosFromPicks);
  }

  @Override
  public Collection<BigInteger> segmentAudioIds() throws Exception {
    return ImmutableList.copyOf(segmentAudios().keySet());
  }

  @Override
  public Collection<SegmentChord> segmentChords() throws Exception {
    if (Objects.isNull(_segmentChords) || _segmentChords.isEmpty()) {
      _segmentChords = segmentChordDAO.readAll(Access.internal(), ImmutableList.of(segment().getId()));
    }

    return Collections.unmodifiableCollection(_segmentChords);
  }

  @Override
  public Collection<SegmentMeme> segmentMemes() throws Exception {
    if (Objects.nonNull(_currentSegmentMemes)) {
      return Collections.unmodifiableCollection(_currentSegmentMemes);
    }

    return segmentMemes(segment().getId());
  }

  @Override
  public void setSegmentMemes(Collection<SegmentMeme> memes) {
    _currentSegmentMemes = Lists.newArrayList(memes);
  }

  @Override
  public void pick(Pick pick) {
    _picks.add(pick);
  }

  @Override
  public Collection<Pick> picks() throws Exception {
    return Collections.unmodifiableList(_picks);
  }

  @Override
  public Duration segmentTotalLength() throws Exception {
    if (Objects.isNull(segment().getEndAt()))
      throw new BusinessException("Cannot compute total length of segment with no end!");

    return Duration.ofMillis(segment().getEndAt().getTime() - segment().getBeginAt().getTime());
  }

  @Override
  public Segment segmentByOffset(BigInteger chainId, BigInteger offset) throws Exception {
    if (!_segmentsByOffset.containsKey(chainId))
      _segmentsByOffset.put(chainId, Maps.newConcurrentMap());

    if (!_segmentsByOffset.get(chainId).containsKey(offset)) {
      Segment segment = segmentDAO.readOneAtChainOffset(Access.internal(), chainId, offset);
      if (Objects.nonNull(segment)) _segmentsByOffset.get(chainId).put(offset, segment);
    }

    return _segmentsByOffset.get(chainId).getOrDefault(offset, null);
  }

  @Override
  public Choice segmentChoiceByType(BigInteger segmentId, SequenceType sequenceType) throws Exception {
    if (!_segmentChoicesByType.containsKey(segmentId))
      _segmentChoicesByType.put(segmentId, Maps.newConcurrentMap());

    if (!_segmentChoicesByType.get(segmentId).containsKey(sequenceType)) {
      Choice choice = choiceDAO.readOneSegmentTypeWithAvailablePatternOffsets(Access.internal(), segmentId, sequenceType);
      if (Objects.nonNull(choice)) _segmentChoicesByType.get(segmentId).put(sequenceType, choice);
    }

    return _segmentChoicesByType.get(segmentId).getOrDefault(sequenceType, null);
  }

  @Override
  public void updateSegment(Segment segment) throws Exception {
    _segment = segment;
    segmentDAO.update(Access.internal(), segment.getId(), segment);
  }

  @Override
  public void report(String key, String value) {
    report.put(key, value);
  }

  @Override
  public void sendReport() {
    if (_sentReport)
      log.warn("Report has already been sent!");
    _sentReport = true;

    if (report.isEmpty())
      return;

    double totalSeconds = (System.nanoTime() - startTime) / NANOS_PER_SECOND;
    report.put("totalNanos", totalSeconds);

    String body = new Yaml().dumpAsMap(report);
    try {
      segmentMessageDAO.create(Access.internal(),
        new SegmentMessage()
          .setType(MessageType.Info.toString())
          .setSegmentId(segment().getId())
          .setBody(body));
      log.info("Completed work and sent basis report for Segment #{} in {}s", segment().getId(), totalSeconds);

    } catch (Exception e) {
      log.warn("Failed to send final craft report message for Segment {} Message {}", _segment, body, e);
    }
  }

  @Override
  public Long atMicros(Double seconds) {
    return (long) (seconds * MICROSECONDS_PER_SECOND);
  }

  @Override
  public MemeIsometry previousMacroNextPatternMemeIsometry() throws Exception {
    if (Objects.isNull(_previousMacroMemeIsometry)) {
      _previousMacroMemeIsometry = MemeIsometry.of(ingest().sequenceAndPatternMemes(
        previousMacroChoice().getSequenceId(),
        Value.inc(previousMacroChoice().getSequencePatternOffset(), 1),
        PatternType.Macro));
    }

    return _previousMacroMemeIsometry;
  }

  @Override
  public MemeIsometry currentMacroMemeIsometry() throws Exception {
    if (Objects.isNull(_currentMacroMemeIsometry)) {
      _currentMacroMemeIsometry = MemeIsometry.of(ingest().sequenceAndPatternMemes(
        currentMacroChoice().getSequenceId(),
        currentMacroChoice().getSequencePatternOffset(),
        PatternType.Macro));
    }

    return _currentMacroMemeIsometry;
  }

  @Override
  public MemeIsometry currentSegmentMemeIsometry() throws Exception {
    if (Objects.isNull(_currentSegmentMemeIsometry)) {
      _currentSegmentMemeIsometry = MemeIsometry.of(segmentMemes());
    }

    return _currentSegmentMemeIsometry;
  }

  @Override
  public Choice create(Choice choice) throws Exception {
    return choiceDAO.create(Access.internal(), choice);
  }

  @Override
  public Arrangement create(Arrangement arrangement) throws Exception {
    return arrangementDAO.create(Access.internal(), arrangement);
  }

  @Override
  public SegmentMeme create(SegmentMeme segmentMeme) throws Exception {
    return segmentMemeDAO.create(Access.internal(), segmentMeme);
  }

  @Override
  public SegmentChord create(SegmentChord segmentChord) throws Exception {
    return segmentChordDAO.create(Access.internal(), segmentChord);
  }

  /**
   Compute using an integral
   the seconds from start for any given position in beats
   [#153542275] Segment wherein tempo changes expect perfectly smooth sound from previous segment through to following segment

   @param B position in beats
   @return seconds from start
   */
  private Double computeIntegralSecondsAtPosition(double B) throws Exception {
    Double sum = 0.0d;
    Double x = 0.0d;
    Double dx = COMPUTE_INTEGRAL_DX;

    Double T = segment().getTotal().doubleValue();
    double v2 = BPM.velocity(segment().getTempo()); // velocity at current segment tempo
    double v1 = isInitialSegment() ? v2 :
      BPM.velocity(previousSegment().getTempo()); // velocity at previous segment tempo

    while (x < B) {
      sum += Math.min(dx, B - x) * // increment by dx, unless in the last (less than B-x) segment
        (v1 + (v2 - v1) * x / T);
      x += dx;
    }
    return sum;
  }

  /**
   real output channels based on chain configs

   @return output channels
   */
  private int outputChannels() throws Exception {
    return Integer.parseInt(chainConfig(ChainConfigType.OutputChannels).getValue());
  }

  /**
   real output sample bits based on chain configs

   @return output sample bits
   */
  private int outputSampleBits() throws Exception {
    return Integer.parseInt(chainConfig(ChainConfigType.OutputSampleBits).getValue());
  }

  /**
   real output frame rate based on chain configs

   @return output frame rate, per second
   */
  private float outputFrameRate() throws Exception {
    return Integer.parseInt(chainConfig(ChainConfigType.OutputFrameRate).getValue());
  }

  /**
   output encoding based on chain configs

   @return output encoding
   */
  private AudioFormat.Encoding outputEncoding() throws Exception {
    return new AudioFormat.Encoding(chainConfig(ChainConfigType.OutputEncoding).getValue());
  }

}
