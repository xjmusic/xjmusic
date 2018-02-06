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
import io.xj.core.dao.ChainPatternDAO;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.dao.LinkChordDAO;
import io.xj.core.dao.LinkDAO;
import io.xj.core.dao.LinkMemeDAO;
import io.xj.core.dao.LinkMessageDAO;
import io.xj.craft.ingest.Ingest;
import io.xj.core.exception.BusinessException;
import io.xj.craft.isometry.MemeIsometry;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.chain_config.ChainConfig;
import io.xj.core.model.chain_config.ChainConfigType;
import io.xj.core.model.chain_instrument.ChainInstrument;
import io.xj.core.model.chain_library.ChainLibrary;
import io.xj.core.model.chain_pattern.ChainPattern;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.entity.Entity;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.library.Library;
import io.xj.core.model.link.Link;
import io.xj.core.model.link_chord.LinkChord;
import io.xj.core.model.link_meme.LinkMeme;
import io.xj.core.model.link_message.LinkMessage;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase.PhaseType;
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
 [#214] If a Chain has Patterns associated with it directly, prefer those choices to any in the Library
 */
public class BasisImpl implements Basis {
  private static final int MICROSECONDS_PER_SECOND = 1000000;
  private static final double COMPUTE_INTEGRAL_DX = 0.25d; // # beats granularity to compute tempo change integral
  private static final double NANOS_PER_SECOND = 1000000000.0;
  private final ArrangementDAO arrangementDAO;
  private final ChainConfigDAO chainConfigDAO;
  private final ChainLibraryDAO chainLibraryDAO;
  private final ChainPatternDAO chainPatternDAO;
  private final ChainInstrumentDAO chainInstrumentDAO;
  private final ChoiceDAO choiceDAO;
  private final IngestCacheProvider ingestProvider;
  private final LinkChordDAO linkChordDAO;
  private final LinkDAO linkDAO;
  private final LinkMemeDAO linkMemeDAO;
  private final LinkMessageDAO linkMessageDAO;
  private final Logger log = LoggerFactory.getLogger(BasisImpl.class);
  private final Map<String, Object> report = Maps.newConcurrentMap();
  private final Tuning tuning;
  private final long startTime;
  private final List<Pick> _picks = Lists.newArrayList();
  private final Map<BigInteger, Collection<Arrangement>> _choiceArrangements = Maps.newConcurrentMap();
  private final Map<BigInteger, Collection<LinkMeme>> _linkMemes = Maps.newConcurrentMap();
  private final Map<BigInteger, Map<BigInteger, Link>> _linksByOffset = Maps.newConcurrentMap();
  private final Map<BigInteger, Map<PatternType, Choice>> _linkChoicesByType = Maps.newConcurrentMap();
  private final Map<Double, Double> _positionSeconds = Maps.newConcurrentMap();
  private BasisType _type;
  private Boolean _sentReport = false;
  private Link _link;
  private Collection<LinkChord> _linkChords;
  private Map<ChainConfigType, ChainConfig> _chainConfigs;
  private Map<BigInteger, Audio> _audiosFromPicks;
  private MemeIsometry _currentMacroMemeIsometry;
  private MemeIsometry _previousMacroMemeIsometry;
  private MemeIsometry _currentLinkMemeIsometry;
  private Collection<LinkMeme> _currentLinkMemes;
  private Ingest _ingest;
  private Ingest _libraryIngest;

  @Inject
  public BasisImpl(
    @Assisted("link") Link link,
    ArrangementDAO arrangementDAO,
    ChainConfigDAO chainConfigDAO,
    ChainLibraryDAO chainLibraryDAO,
    ChainPatternDAO chainPatternDAO,
    ChainInstrumentDAO chainInstrumentDAO,
    ChoiceDAO choiceDAO,
    IngestCacheProvider ingestProvider,
    LinkChordDAO linkChordDAO,
    LinkDAO linkDAO,
    LinkMemeDAO linkMemeDAO,
    LinkMessageDAO linkMessageDAO
  /*-*/) throws BusinessException {
    _link = link;
    this.arrangementDAO = arrangementDAO;
    this.chainConfigDAO = chainConfigDAO;
    this.chainLibraryDAO = chainLibraryDAO;
    this.chainPatternDAO = chainPatternDAO;
    this.chainInstrumentDAO = chainInstrumentDAO;
    this.choiceDAO = choiceDAO;
    this.ingestProvider = ingestProvider;
    this.linkChordDAO = linkChordDAO;
    this.linkDAO = linkDAO;
    this.linkMemeDAO = linkMemeDAO;
    this.linkMessageDAO = linkMessageDAO;

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
    if (Objects.isNull(link().getWaveformKey()))
      throw new BusinessException("Link has no waveform key!");

    return Config.workTempFilePathPrefix() + link().getWaveformKey();
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
        if (isInitialLink())
          _type = BasisType.Initial;
        else if (previousMainChoice().hasOneMorePhase())
          _type = BasisType.Continue;
        else if (previousMacroChoice().hasTwoMorePhases())
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
    result.addAll(patternsBoundToChain());
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
   Get all patterns bound to chain.
   CACHES results.

   @return patterns bound to chain.
   */
  private Collection<Pattern> patternsBoundToChain() throws Exception {
    ImmutableList.Builder<Pattern> builder = ImmutableList.builder();
    chainPatterns().forEach(chainPattern -> builder.add(new Pattern(chainPattern.getPatternId())));
    return builder.build();
  }

  /**
   Get ChainPattern bindings for the current chain

   @return collection of ChainPattern
   */
  private Collection<ChainPattern> chainPatterns() throws Exception {
    return chainPatternDAO.readAll(Access.internal(), ImmutableList.of(chainId()));
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
    patternsBoundToChain().forEach(pattern -> result.put(pattern.getLibraryId(), new Library(pattern.getLibraryId())));
    instrumentsBoundToChain().forEach(instrument -> result.put(instrument.getLibraryId(), new Library(instrument.getLibraryId())));
    return result.values();
  }

  @Override
  public Link link() {
    return _link;
  }

  @Override
  public Boolean isInitialLink() {
    return _link.isInitial();
  }

  @Override
  public BigInteger chainId() {
    return _link.getChainId();
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
  public Timestamp linkBeginAt() {
    return _link.getBeginAt();
  }

  @Override
  public Link previousLink() throws Exception {
    if (isInitialLink()) return null;

    return linkByOffset(chainId(), Value.inc(_link.getOffset(), -1));
  }

  @Override
  public Collection<LinkMeme> previousLinkMemes() throws Exception {
    if (isInitialLink()) return null;

    Link previousLink = previousLink();
    if (Objects.isNull(previousLink)) return Lists.newArrayList();

    return linkMemes(previousLink.getId());
  }

  @Override
  public Choice previousMacroChoice() throws Exception {
    return isInitialLink() ? null : linkChoiceByType(previousLink().getId(), PatternType.Macro);
  }

  @Override
  public Choice previousMainChoice() throws Exception {
    return isInitialLink() ? null : linkChoiceByType(previousLink().getId(), PatternType.Main);
  }

  @Override
  public Choice previousRhythmChoice() throws Exception {
    return isInitialLink() ? null : linkChoiceByType(previousLink().getId(), PatternType.Rhythm);
  }

  @Override
  public Collection<Arrangement> previousPercussiveArrangements() throws Exception {
    return isInitialLink() ? null : choiceArrangements(previousRhythmChoice().getId());
  }

  @Override
  public Choice currentMacroChoice() throws Exception {
    return linkChoiceByType(link().getId(), PatternType.Macro);
  }

  @Override
  public Choice currentMainChoice() throws Exception {
    return linkChoiceByType(link().getId(), PatternType.Main);
  }

  @Override
  public Choice currentRhythmChoice() throws Exception {
    return linkChoiceByType(link().getId(), PatternType.Rhythm);
  }

  @Override
  public Phase currentMacroPhase() throws Exception {
    return ingest().phaseAtOffset(
      currentMacroChoice().getPatternId(),
      currentMacroChoice().getPhaseOffset(),
      PhaseType.Macro);
  }

  @Override
  public Phase previousMacroNextPhase() throws Exception {
    return isInitialLink() ? null : ingest().phaseAtOffset(
      previousMacroChoice().getPatternId(),
      previousMacroChoice().nextPhaseOffset(),
      PhaseType.Macro);
  }

  @Override
  public Map<ChainConfigType, ChainConfig> chainConfigs() throws Exception {
    if (Objects.isNull(_chainConfigs) || _chainConfigs.isEmpty()) {
      _chainConfigs = Maps.newConcurrentMap();
      chainConfigDAO.readAll(Access.internal(), ImmutableList.of(link().getId()))
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
    // default to returning a chord based on the link key, if nothing else is found
    Chord foundChord = Chord.of(link().getKey());
    Double foundPosition = null;

    // we assume that these chords are in order of position ascending (see: LinkChordDAO.readAllExpectedWork)
    for (LinkChord linkChord : linkChords()) {
      // if it's a better match (or no match has yet been found) then use it
      if (Objects.isNull(foundPosition) ||
        linkChord.getPosition() > foundPosition && linkChord.getPosition() < position) {
        foundPosition = linkChord.getPosition();
        foundChord = Chord.of(linkChord.getName());
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
  public Collection<LinkMeme> linkMemes(BigInteger linkId) throws Exception {
    if (!_linkMemes.containsKey(linkId))
      _linkMemes.put(linkId, linkMemeDAO.readAll(Access.internal(), ImmutableList.of(linkId)));

    return _linkMemes.get(linkId);
  }

  @Override
  public Audio linkAudio(BigInteger audioId) throws Exception {
    if (linkAudios().containsKey(audioId))
      return linkAudios().get(audioId);

    throw new BusinessException(String.format("Audio #%s is not in link picks!", audioId.toString()));
  }

  @Override
  public Map<BigInteger, Audio> linkAudios() throws Exception {
    if (Objects.isNull(_audiosFromPicks) || _audiosFromPicks.isEmpty()) {
      _audiosFromPicks = Maps.newConcurrentMap();
      for (Pick pick : _picks) {
        _audiosFromPicks.put(pick.getAudioId(), ingest().audio(pick.getAudioId()));
      }
    }

    return Collections.unmodifiableMap(_audiosFromPicks);
  }

  @Override
  public Collection<BigInteger> linkAudioIds() throws Exception {
    return ImmutableList.copyOf(linkAudios().keySet());
  }

  @Override
  public Collection<LinkChord> linkChords() throws Exception {
    if (Objects.isNull(_linkChords) || _linkChords.isEmpty()) {
      _linkChords = linkChordDAO.readAll(Access.internal(), ImmutableList.of(link().getId()));
    }

    return Collections.unmodifiableCollection(_linkChords);
  }

  @Override
  public Collection<LinkMeme> linkMemes() throws Exception {
    if (Objects.nonNull(_currentLinkMemes)) {
      return Collections.unmodifiableCollection(_currentLinkMemes);
    }

    return linkMemes(link().getId());
  }

  @Override
  public void setLinkMemes(Collection<LinkMeme> memes) {
    _currentLinkMemes = Lists.newArrayList(memes);
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
  public Duration linkTotalLength() throws Exception {
    if (Objects.isNull(link().getEndAt()))
      throw new BusinessException("Cannot compute total length of link with no end!");

    return Duration.ofMillis(link().getEndAt().getTime() - link().getBeginAt().getTime());
  }

  @Override
  public Link linkByOffset(BigInteger chainId, BigInteger offset) throws Exception {
    if (!_linksByOffset.containsKey(chainId))
      _linksByOffset.put(chainId, Maps.newConcurrentMap());

    if (!_linksByOffset.get(chainId).containsKey(offset)) {
      Link link = linkDAO.readOneAtChainOffset(Access.internal(), chainId, offset);
      if (Objects.nonNull(link)) _linksByOffset.get(chainId).put(offset, link);
    }

    return _linksByOffset.get(chainId).getOrDefault(offset, null);
  }

  @Override
  public Choice linkChoiceByType(BigInteger linkId, PatternType patternType) throws Exception {
    if (!_linkChoicesByType.containsKey(linkId))
      _linkChoicesByType.put(linkId, Maps.newConcurrentMap());

    if (!_linkChoicesByType.get(linkId).containsKey(patternType)) {
      Choice choice = choiceDAO.readOneLinkTypeWithAvailablePhaseOffsets(Access.internal(), linkId, patternType);
      if (Objects.nonNull(choice)) _linkChoicesByType.get(linkId).put(patternType, choice);
    }

    return _linkChoicesByType.get(linkId).getOrDefault(patternType, null);
  }

  @Override
  public void updateLink(Link link) throws Exception {
    _link = link;
    linkDAO.update(Access.internal(), link.getId(), link);
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
      linkMessageDAO.create(Access.internal(),
        new LinkMessage()
          .setType(MessageType.Info.toString())
          .setLinkId(link().getId())
          .setBody(body));
      log.info("Completed work and sent basis report for Link #{} in {}s", link().getId(), totalSeconds);

    } catch (Exception e) {
      log.warn("Failed to send final craft report message for Link {} Message {}", _link, body, e);
    }
  }

  @Override
  public Long atMicros(Double seconds) {
    return (long) (seconds * MICROSECONDS_PER_SECOND);
  }

  @Override
  public MemeIsometry previousMacroNextPhaseMemeIsometry() throws Exception {
    if (Objects.isNull(_previousMacroMemeIsometry)) {
      _previousMacroMemeIsometry = MemeIsometry.of(ingest().patternAndPhaseMemes(
        previousMacroChoice().getPatternId(),
        Value.inc(previousMacroChoice().getPhaseOffset(), 1),
        PhaseType.Macro));
    }

    return _previousMacroMemeIsometry;
  }

  @Override
  public MemeIsometry currentMacroMemeIsometry() throws Exception {
    if (Objects.isNull(_currentMacroMemeIsometry)) {
      _currentMacroMemeIsometry = MemeIsometry.of(ingest().patternAndPhaseMemes(
        currentMacroChoice().getPatternId(),
        currentMacroChoice().getPhaseOffset(),
        PhaseType.Macro));
    }

    return _currentMacroMemeIsometry;
  }

  @Override
  public MemeIsometry currentLinkMemeIsometry() throws Exception {
    if (Objects.isNull(_currentLinkMemeIsometry)) {
      _currentLinkMemeIsometry = MemeIsometry.of(linkMemes());
    }

    return _currentLinkMemeIsometry;
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
  public LinkMeme create(LinkMeme linkMeme) throws Exception {
    return linkMemeDAO.create(Access.internal(), linkMeme);
  }

  @Override
  public LinkChord create(LinkChord linkChord) throws Exception {
    return linkChordDAO.create(Access.internal(), linkChord);
  }

  /**
   Compute using an integral
   the seconds from start for any given position in beats
   [#153542275] Link wherein tempo changes expect perfectly smooth sound from previous link through to following link

   @param B position in beats
   @return seconds from start
   */
  private Double computeIntegralSecondsAtPosition(double B) throws Exception {
    Double sum = 0.0d;
    Double x = 0.0d;
    Double dx = COMPUTE_INTEGRAL_DX;

    Double T = link().getTotal().doubleValue();
    double v2 = BPM.velocity(link().getTempo()); // velocity at current link tempo
    double v1 = isInitialLink() ? v2 :
      BPM.velocity(previousLink().getTempo()); // velocity at previous link tempo

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
