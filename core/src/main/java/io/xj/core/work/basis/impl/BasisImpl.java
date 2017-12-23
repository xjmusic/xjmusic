// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.work.basis.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.ArrangementDAO;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.AudioEventDAO;
import io.xj.core.dao.ChainConfigDAO;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.dao.InstrumentMemeDAO;
import io.xj.core.dao.LinkChordDAO;
import io.xj.core.dao.LinkDAO;
import io.xj.core.dao.LinkMemeDAO;
import io.xj.core.dao.LinkMessageDAO;
import io.xj.core.dao.PatternDAO;
import io.xj.core.dao.PatternMemeDAO;
import io.xj.core.dao.PhaseDAO;
import io.xj.core.dao.PhaseMemeDAO;
import io.xj.core.dao.PickDAO;
import io.xj.core.dao.VoiceDAO;
import io.xj.core.dao.VoiceEventDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.isometry.MemeIsometry;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.model.chain_config.ChainConfig;
import io.xj.core.model.chain_config.ChainConfigType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.instrument_meme.InstrumentMeme;
import io.xj.core.model.link.Link;
import io.xj.core.model.link_chord.LinkChord;
import io.xj.core.model.link_meme.LinkMeme;
import io.xj.core.model.link_message.LinkMessage;
import io.xj.core.model.meme.Meme;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternType;
import io.xj.core.model.pattern_meme.PatternMeme;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase_meme.PhaseMeme;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.voice.Voice;
import io.xj.core.model.voice_event.VoiceEvent;
import io.xj.core.util.Value;
import io.xj.core.work.basis.Basis;
import io.xj.core.work.basis.BasisType;
import io.xj.music.BPM;
import io.xj.music.Chord;
import io.xj.music.MusicalException;
import io.xj.music.Note;
import io.xj.music.Tuning;
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
  private final ArrangementDAO arrangementDAO;
  private final AudioDAO audioDAO;
  private final AudioEventDAO audioEventDAO;
  private final ChainConfigDAO chainConfigDAO;
  private final ChoiceDAO choiceDAO;
  private final PatternDAO patternDAO;
  private final PatternMemeDAO patternMemeDAO;
  private final InstrumentMemeDAO instrumentMemeDAO;
  private final LinkChordDAO linkChordDAO;
  private final LinkDAO linkDAO;
  private final LinkMemeDAO linkMemeDAO;
  private final LinkMessageDAO linkMessageDAO;
  private final Logger log = LoggerFactory.getLogger(BasisImpl.class);
  private final Map<String, Object> report = Maps.newConcurrentMap();
  private final PhaseDAO phaseDAO;
  private final PhaseMemeDAO phaseMemeDAO;
  private final PickDAO pickDAO;
  private final Tuning tuning;
  private final VoiceDAO voiceDAO;
  private final VoiceEventDAO voiceEventDAO;
  private BasisType _type;
  private Boolean _sentReport = false;
  private Link _link;
  private Collection<Arrangement> _choiceArrangements;
  private Collection<LinkChord> _linkChords;
  private List<Pick> _picks;
  private Map<ChainConfigType, ChainConfig> _chainConfigs;
  private Map<BigInteger, Audio> _audiosFromPicks;
  private MemeIsometry _currentMacroMemeIsometry;
  private MemeIsometry _previousMacroMemeIsometry;
  private MemeIsometry _currentLinkMemeIsometry;
  private Collection<LinkMeme> _currentLinkMemes;
  private final Map<Double, Double> _positionSeconds = Maps.newConcurrentMap();
  private final Map<BigInteger, Pattern> _patterns = Maps.newConcurrentMap();
  private final Map<BigInteger, Collection<Audio>> _instrumentAudios = Maps.newConcurrentMap();
  private final Map<BigInteger, Collection<InstrumentMeme>> _instrumentMemes = Maps.newConcurrentMap();
  private final Map<BigInteger, Collection<AudioEvent>> _audioWithFirstEvent = Maps.newConcurrentMap();
  private final Map<BigInteger, Map<PatternType, Choice>> _linkChoicesByType = Maps.newConcurrentMap();
  private final Map<BigInteger, Map<BigInteger, Link>> _linksByOffset = Maps.newConcurrentMap();
  private final Map<BigInteger, Map<BigInteger, Phase>> _patternPhasesByOffset = Maps.newConcurrentMap();
  private final Map<BigInteger, Collection<PatternMeme>> _patternMemes = Maps.newConcurrentMap();
  private final Map<BigInteger, Collection<LinkMeme>> _linkMemes = Maps.newConcurrentMap();
  private final Map<BigInteger, Collection<PhaseMeme>> _phaseMemes = Maps.newConcurrentMap();
  private final Map<BigInteger, Collection<VoiceEvent>> _voiceEvents = Maps.newConcurrentMap();
  private final Map<BigInteger, Collection<Voice>> _voicesByPhase = Maps.newConcurrentMap();

  @Inject
  public BasisImpl(
    @Assisted("link") Link link,
    ArrangementDAO arrangementDAO,
    AudioDAO audioDAO,
    AudioEventDAO audioEventDAO,
    ChainConfigDAO chainConfigDAO,
    ChoiceDAO choiceDAO,
    PatternDAO patternDAO,
    PatternMemeDAO patternMemeDAO,
    InstrumentMemeDAO instrumentMemeDAO, LinkDAO linkDAO,
    LinkChordDAO linkChordDAO,
    LinkMemeDAO linkMemeDAO,
    LinkMessageDAO linkMessageDAO,
    PhaseDAO phaseDAO,
    PhaseMemeDAO phaseMemeDAO,
    PickDAO pickDAO,
    VoiceDAO voiceDAO,
    VoiceEventDAO voiceEventDAO
  /*-*/) throws BusinessException {
    _link = link;
    this.arrangementDAO = arrangementDAO;
    this.audioDAO = audioDAO;
    this.audioEventDAO = audioEventDAO;
    this.chainConfigDAO = chainConfigDAO;
    this.choiceDAO = choiceDAO;
    this.patternDAO = patternDAO;
    this.patternMemeDAO = patternMemeDAO;
    this.instrumentMemeDAO = instrumentMemeDAO;
    this.linkDAO = linkDAO;
    this.linkChordDAO = linkChordDAO;
    this.linkMemeDAO = linkMemeDAO;
    this.linkMessageDAO = linkMessageDAO;
    this.phaseDAO = phaseDAO;
    this.phaseMemeDAO = phaseMemeDAO;
    this.pickDAO = pickDAO;
    this.voiceDAO = voiceDAO;
    this.voiceEventDAO = voiceEventDAO;

    // [#255] Tuning based on root note configured in environment parameters.
    try {
      tuning = Tuning.at(
        Note.of(Config.tuningRootNote()),
        Config.tuningRootPitch());
    } catch (MusicalException e) {
      throw new BusinessException("Could not tune XJ!", e);
    }
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
  public Link link() {
    return _link;
  }

  @Override
  public Boolean isInitialLink() {
    return _link.isInitial();
  }

  @Override
  public BigInteger linkId() {
    return _link.getId();
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
    if (isInitialLink()) {
      return null;
    }

    return linkByOffset(chainId(), Value.inc(_link.getOffset(), -1));
  }

  @Override
  public Collection<LinkMeme> previousLinkMemes() throws Exception {
    if (isInitialLink()) {
      return null;
    }

    Link previousLink = previousLink();
    if (Objects.isNull(previousLink)) {
      return Lists.newArrayList();
    }

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
    return phaseByOffset(
      currentMacroChoice().getPatternId(),
      currentMacroChoice().getPhaseOffset());
  }

  @Override
  public Phase previousMacroNextPhase() throws Exception {
    return isInitialLink() ? null : phaseByOffset(
      previousMacroChoice().getPatternId(),
      previousMacroChoice().nextPhaseOffset());
  }

  @Override
  public Pattern pattern(BigInteger id) throws Exception {
    if (!_patterns.containsKey(id))
      _patterns.put(id, patternDAO.readOne(Access.internal(), id));

    return _patterns.get(id);
  }

  @Override
  public Map<ChainConfigType, ChainConfig> chainConfigs() throws Exception {
    if (Objects.isNull(_chainConfigs) || _chainConfigs.isEmpty()) {
      _chainConfigs = Maps.newConcurrentMap();
      chainConfigDAO.readAll(Access.internal(), link().getId())
        .forEach(record -> _chainConfigs.put(
          record.getType(),
          record));
    }

    return Collections.unmodifiableMap(_chainConfigs);
  }

  @Override
  public Collection<Arrangement> choiceArrangements(BigInteger choiceId) throws Exception {
    if (Objects.isNull(_choiceArrangements) || _choiceArrangements.isEmpty()) {
      _choiceArrangements = arrangementDAO.readAll(Access.internal(), choiceId);
    }

    return Collections.unmodifiableCollection(_choiceArrangements);
  }

  @Override
  public Chord chordAt(double position) throws Exception {
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
      _linkMemes.put(linkId, linkMemeDAO.readAll(Access.internal(), linkId));

    return _linkMemes.get(linkId);
  }

  @Override
  public Collection<PatternMeme> patternMemes(BigInteger patternId) throws Exception {
    if (!_patternMemes.containsKey(patternId))
      _patternMemes.put(patternId, patternMemeDAO.readAll(Access.internal(), patternId));

    return _patternMemes.get(patternId);
  }

  @Override
  public Collection<Meme> patternPhaseMemes(BigInteger patternId, BigInteger phaseOffset) throws Exception {
    Collection<Meme> baseMemes = Lists.newArrayList();

    // add pattern memes
    baseMemes.addAll(patternMemes(patternId));

    // add pattern phase memes
    Phase phase = phaseByOffset(patternId, phaseOffset);
    baseMemes.addAll(phaseMemes(phase.getId()));

    return baseMemes;
  }

  @Override
  public Collection<VoiceEvent> voiceEvents(BigInteger voiceId) throws Exception {
    if (!_voiceEvents.containsKey(voiceId))
      _voiceEvents.put(voiceId, voiceEventDAO.readAll(Access.internal(), voiceId));

    return _voiceEvents.get(voiceId);
  }

  @Override
  public Collection<AudioEvent> instrumentAudioEvents(BigInteger instrumentId) throws Exception {
    if (!_audioWithFirstEvent.containsKey(instrumentId))
      _audioWithFirstEvent.put(instrumentId, audioEventDAO.readAllFirstEventsForInstrument(Access.internal(), instrumentId));

    return _audioWithFirstEvent.get(instrumentId);
  }

  @Override
  public Collection<Audio> instrumentAudios(BigInteger instrumentId) throws Exception {
    if (!_instrumentAudios.containsKey(instrumentId)) {
      _instrumentAudios.put(instrumentId, Lists.newArrayList());
      audioDAO.readAll(Access.internal(), instrumentId)
        .forEach(model -> _instrumentAudios.get(instrumentId)
          .add(model));
    }

    return _instrumentAudios.get(instrumentId);
  }

  @Override
  public Collection<InstrumentMeme> instrumentMemes(BigInteger instrumentId) throws Exception {
    if (!_instrumentMemes.containsKey(instrumentId)) {
      _instrumentMemes.put(instrumentId, Lists.newArrayList());
      instrumentMemeDAO.readAll(Access.internal(), instrumentId)
        .forEach(model -> _instrumentMemes.get(instrumentId)
          .add(model));
    }

    return _instrumentMemes.get(instrumentId);
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
      audioDAO.readAllPickedForLink(Access.internal(), link().getId())
        .forEach(model -> _audiosFromPicks.put(
          model.getId(),
          model));
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
      _linkChords = linkChordDAO.readAll(Access.internal(), link().getId());
    }

    return Collections.unmodifiableCollection(_linkChords);
  }

  @Override
  public Collection<LinkMeme> linkMemes() throws Exception {
    if (Objects.nonNull(_currentLinkMemes)) {
      return Collections.unmodifiableCollection(_currentLinkMemes);
    }

    return linkMemes(linkId());
  }

  @Override
  public void setLinkMemes(Collection<LinkMeme> memes) {
    _currentLinkMemes = Lists.newArrayList(memes);
  }

  @Override
  public List<Pick> picks() throws Exception {
    if (Objects.isNull(_picks) || _picks.isEmpty()) {
      _picks = Lists.newArrayList();
      _picks.addAll(pickDAO.readAllInLink(Access.internal(), link().getId()));
    }

    return Collections.unmodifiableList(_picks);
  }

  @Override
  public Duration linkTotalLength() throws Exception {
    if (Objects.isNull(link().getEndAt()))
      throw new BusinessException("Cannot compute total length of link with no end!");

    return Duration.ofMillis(link().getEndAt().getTime() - link().getBeginAt().getTime());
  }

  @Override
  public Collection<PhaseMeme> phaseMemes(BigInteger phaseId) throws Exception {
    if (!_phaseMemes.containsKey(phaseId))
      _phaseMemes.put(phaseId, phaseMemeDAO.readAll(Access.internal(), phaseId));

    return _phaseMemes.get(phaseId);
  }

  @Override
  public Phase phaseByOffset(BigInteger patternId, BigInteger phaseOffset) throws Exception {
    if (!_patternPhasesByOffset.containsKey(patternId))
      _patternPhasesByOffset.put(patternId, Maps.newConcurrentMap());

    if (!_patternPhasesByOffset.get(patternId).containsKey(phaseOffset))
      _patternPhasesByOffset.get(patternId).put(phaseOffset,
        phaseDAO.readOneForPattern(Access.internal(), patternId, phaseOffset));

    return _patternPhasesByOffset.get(patternId).get(phaseOffset);
  }

  @Override
  public Link linkByOffset(BigInteger chainId, BigInteger offset) throws Exception {
    if (!_linksByOffset.containsKey(chainId))
      _linksByOffset.put(chainId, Maps.newConcurrentMap());

    if (!_linksByOffset.get(chainId).containsKey(offset))
      _linksByOffset.get(chainId).put(offset,
        linkDAO.readOneAtChainOffset(Access.internal(), chainId, offset));

    return _linksByOffset.get(chainId).get(offset);
  }

  @Override
  public Choice linkChoiceByType(BigInteger linkId, PatternType patternType) throws Exception {
    if (!_linkChoicesByType.containsKey(linkId))
      _linkChoicesByType.put(linkId, Maps.newConcurrentMap());

    if (!_linkChoicesByType.get(linkId).containsKey(patternType))
      _linkChoicesByType.get(linkId).put(patternType,
        choiceDAO.readOneLinkTypeWithAvailablePhaseOffsets(Access.internal(), linkId, patternType));

    return _linkChoicesByType.get(linkId).get(patternType);
  }

  @Override
  public Collection<Voice> voices(BigInteger phaseId) throws Exception {
    if (!_voicesByPhase.containsKey(phaseId))
      _voicesByPhase.put(phaseId,
        voiceDAO.readAll(Access.internal(), phaseId));

    return _voicesByPhase.get(phaseId);
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

    String body = new Yaml().dumpAsMap(report);
    try {
      linkMessageDAO.create(Access.internal(),
        new LinkMessage()
          .setType(MessageType.Info.toString())
          .setLinkId(linkId())
          .setBody(body));

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
      _previousMacroMemeIsometry = MemeIsometry.of(patternPhaseMemes(
        previousMacroChoice().getPatternId(),
        Value.inc(previousMacroChoice().getPhaseOffset(), 1)));
    }

    return _previousMacroMemeIsometry;
  }

  @Override
  public MemeIsometry currentMacroMemeIsometry() throws Exception {
    if (Objects.isNull(_currentMacroMemeIsometry)) {
      _currentMacroMemeIsometry = MemeIsometry.of(patternPhaseMemes(
        currentMacroChoice().getPatternId(),
        currentMacroChoice().getPhaseOffset()));
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
