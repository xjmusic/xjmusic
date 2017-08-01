// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.work.basis.impl;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.config.Config;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.work.basis.Basis;
import io.xj.core.work.basis.BasisType;
import io.xj.core.cache.StreamCache;
import io.xj.core.dao.ArrangementDAO;
import io.xj.core.dao.AudioDAO;
import io.xj.core.dao.AudioEventDAO;
import io.xj.core.dao.ChainConfigDAO;
import io.xj.core.dao.ChoiceDAO;
import io.xj.core.dao.IdeaDAO;
import io.xj.core.dao.IdeaMemeDAO;
import io.xj.core.dao.LinkChordDAO;
import io.xj.core.dao.LinkDAO;
import io.xj.core.dao.LinkMemeDAO;
import io.xj.core.dao.LinkMessageDAO;
import io.xj.core.dao.PhaseDAO;
import io.xj.core.dao.PhaseMemeDAO;
import io.xj.core.dao.PickDAO;
import io.xj.core.dao.VoiceDAO;
import io.xj.core.dao.VoiceEventDAO;
import io.xj.core.external.amazon.AmazonProvider;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.model.chain_config.ChainConfig;
import io.xj.core.model.chain_config.ChainConfigType;
import io.xj.core.model.choice.Choice;
import io.xj.core.model.idea.Idea;
import io.xj.core.model.idea.IdeaType;
import io.xj.core.model.link.Link;
import io.xj.core.model.link_chord.LinkChord;
import io.xj.core.model.link_meme.LinkMeme;
import io.xj.core.model.link_message.LinkMessage;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.pick.Pick;
import io.xj.core.tables.records.IdeaMemeRecord;
import io.xj.core.tables.records.IdeaRecord;
import io.xj.core.tables.records.LinkRecord;
import io.xj.core.tables.records.PhaseMemeRecord;
import io.xj.core.tables.records.PhaseRecord;
import io.xj.core.tables.records.VoiceEventRecord;
import io.xj.core.tables.records.VoiceRecord;
import io.xj.core.util.Value;
import io.xj.music.BPM;
import io.xj.music.Chord;
import io.xj.music.MusicalException;
import io.xj.music.Note;
import io.xj.music.Tuning;

import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import javax.sound.sampled.AudioFormat;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * [#214] If a Chain has Ideas associated with it directly, prefer those choices to any in the Library
 */
public class BasisImpl implements Basis {
  private static final int MICROSECONDS_PER_SECOND = 1000000;
  private final AmazonProvider amazonProvider;
  private final ArrangementDAO arrangementDAO;
  private final AudioDAO audioDAO;
  private final AudioEventDAO audioEventDAO;
  private final ChainConfigDAO chainConfigDAO;
  private final ChoiceDAO choiceDAO;
  private final IdeaDAO ideaDAO;
  private final IdeaMemeDAO ideaMemeDAO;
  private final LinkChordDAO linkChordDAO;
  private final LinkDAO linkDAO;
  private final LinkMemeDAO linkMemeDAO;
  private final LinkMessageDAO linkMessageDAO;
  private final Logger log = LoggerFactory.getLogger(BasisImpl.class);
  private final Map<String, Object> report = Maps.newHashMap();
  private final PhaseDAO phaseDAO;
  private final PhaseMemeDAO phaseMemeDAO;
  private final PickDAO pickDAO;
  private final Tuning tuning;
  private final VoiceDAO voiceDAO;
  private final VoiceEventDAO voiceEventDAO;
  private BasisType _type;
  private Boolean _sentReport = false;
  private Link _link;
  private List<Arrangement> _choiceArrangements;
  private List<LinkChord> _linkChords;
  private List<LinkMeme> _linkMemes;
  private List<Pick> _picks;
  private Map<ChainConfigType, ChainConfig> _chainConfigs;
  private Map<ULong, Audio> _audiosFromPicks;
  private final Map<ULong, IdeaRecord> _ideas = Maps.newHashMap();
  private final Map<ULong, List<Audio>> _instrumentAudios = Maps.newHashMap();
  private final Map<ULong, List<AudioEvent>> _audioWithFirstEvent = Maps.newHashMap();
  private final Map<ULong, Map<IdeaType, Choice>> _linkChoicesByType = Maps.newHashMap();
  private final Map<ULong, Map<ULong, LinkRecord>> _linksByOffset = Maps.newHashMap();
  private final Map<ULong, Map<ULong, PhaseRecord>> _ideaPhasesByOffset = Maps.newHashMap();
  private final Map<ULong, Result<IdeaMemeRecord>> _ideaMemes = Maps.newHashMap();
  private final Map<ULong, Result<PhaseMemeRecord>> _phaseMemes = Maps.newHashMap();
  private final Map<ULong, Result<VoiceEventRecord>> _voiceEvents = Maps.newHashMap();
  private final Map<ULong, Result<VoiceRecord>> _voicesByPhase = Maps.newHashMap();

  @Inject
  public BasisImpl(
    @Assisted("link") Link link,
    AmazonProvider amazonProvider,
    ArrangementDAO arrangementDAO,
    AudioDAO audioDAO,
    AudioEventDAO audioEventDAO,
    ChainConfigDAO chainConfigDAO,
    ChoiceDAO choiceDAO,
    IdeaDAO ideaDAO,
    IdeaMemeDAO ideaMemeDAO,
    LinkDAO linkDAO,
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
    this.amazonProvider = amazonProvider;
    this.arrangementDAO = arrangementDAO;
    this.audioDAO = audioDAO;
    this.audioEventDAO = audioEventDAO;
    this.chainConfigDAO = chainConfigDAO;
    this.choiceDAO = choiceDAO;
    this.ideaDAO = ideaDAO;
    this.ideaMemeDAO = ideaMemeDAO;
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
  public InputStream streamAudioWaveform(Audio audio) throws Exception {
    String key = String.format("%s-%s", Config.audioFileBucket(), audio.getWaveformKey());

    if (!StreamCache.containsKey(key)) {
      InputStream newStream = amazonProvider.streamS3Object(
        Config.audioFileBucket(),
        audio.getWaveformKey());
      StreamCache.setContent(key, newStream);
      log.info(String.format("Loaded & cached new stream: %s", key));
    }

    // Return data from StreamCache
    log.info(String.format("Using cached stream: %s", key));
    return StreamCache.getContent(key);
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
  public ULong linkId() {
    return _link.getId();
  }

  @Override
  public ULong chainId() {
    return _link.getChainId();
  }

  @Override
  public ChainConfig chainConfig(ChainConfigType chainConfigType) throws Exception {
    if (chainConfigs().containsKey(chainConfigType))
      return chainConfigs().get(chainConfigType);

    return new ChainConfig()
      .setChainId(chainId().toBigInteger())
      .setTypeEnum(chainConfigType)
      .setValue(chainConfigType.defaultValue());
  }

  @Override
  public Timestamp linkBeginAt() {
    return _link.getBeginAt();
  }

  @Override
  public Link previousLink() throws Exception {
    return linkByOffset(chainId(), Value.inc(_link.getOffset(), -1));
  }

  @Override
  public Choice previousMacroChoice() throws Exception {
    return linkChoiceByType(previousLink().getId(), IdeaType.Macro);
  }

  @Override
  public Choice previousMainChoice() throws Exception {
    return linkChoiceByType(previousLink().getId(), IdeaType.Main);
  }

  @Override
  public Choice previousRhythmChoice() throws Exception {
    return linkChoiceByType(previousLink().getId(), IdeaType.Rhythm);
  }

  @Override
  public List<Arrangement> previousPercussiveArrangements() throws Exception {
    return choiceArrangements(previousRhythmChoice().getId());
  }

  @Override
  public Choice currentMacroChoice() throws Exception {
    return linkChoiceByType(link().getId(), IdeaType.Macro);
  }

  @Override
  public Choice currentMainChoice() throws Exception {
    return linkChoiceByType(link().getId(), IdeaType.Main);
  }

  @Override
  public Choice currentRhythmChoice() throws Exception {
    return linkChoiceByType(link().getId(), IdeaType.Rhythm);
  }

  @Override
  public PhaseRecord previousMacroPhase() throws Exception {
    return phaseByOffset(
      previousMacroChoice().getIdeaId(),
      previousMacroChoice().getPhaseOffset());
  }

  @Override
  public PhaseRecord previousMacroNextPhase() throws Exception {
    return phaseByOffset(
      previousMacroChoice().getIdeaId(),
      previousMacroChoice().nextPhaseOffset());
  }

  @Override
  public Idea idea(ULong id) throws Exception {
    if (!_ideas.containsKey(id))
      _ideas.put(id, ideaDAO.readOne(Access.internal(), id));

    return new Idea().setFromRecord(_ideas.get(id));
  }

  @Override
  public Map<ChainConfigType, ChainConfig> chainConfigs() throws Exception {
    if (Objects.isNull(_chainConfigs) || _chainConfigs.size() == 0) {
      _chainConfigs = Maps.newHashMap();
      chainConfigDAO.readAll(Access.internal(), link().getId())
        .forEach(chainConfigRecord -> _chainConfigs.put(
          ChainConfigType.valueOf(chainConfigRecord.getType()),
          new ChainConfig().setFromRecord(chainConfigRecord)));
    }

    return _chainConfigs;
  }

  @Override
  public List<Arrangement> choiceArrangements(ULong choiceId) throws Exception {
    if (Objects.isNull(_choiceArrangements) || _choiceArrangements.size() == 0) {
      _choiceArrangements = Lists.newArrayList();
      arrangementDAO.readAll(Access.internal(), choiceId)
        .forEach(arrangementRecord -> _choiceArrangements.add(new Arrangement().setFromRecord(arrangementRecord)));
    }

    return _choiceArrangements;
  }

  @Override
  public Chord chordAt(double position) throws Exception {
    // default to returning a chord based on the link key, if nothing else is found
    Chord foundChord = Chord.of(link().getKey());
    Double foundPosition = null;

    // we assume that these chords are in order of position ascending (see: LinkChordDAO.readAll)
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
  public Double secondsAtPosition(double p3) throws Exception {
    if (isInitialLink())
      return p3 * BPM.velocity(link().getTempo());

    double p2 = link().getTotal().doubleValue();
    double v1 = BPM.velocity(previousLink().getTempo());
    double v2 = BPM.velocity(link().getTempo());
    return p3 * (v1 + (p3 / p2) * (v2 - v1));
  }

  @Override
  public Result<IdeaMemeRecord> ideaMemes(ULong ideaId) throws Exception {
    if (!_ideaMemes.containsKey(ideaId))
      _ideaMemes.put(ideaId, ideaMemeDAO.readAll(Access.internal(), ideaId));

    return _ideaMemes.get(ideaId);
  }

  @Override
  public Result<VoiceEventRecord> voiceEvents(ULong voiceId) throws Exception {
    if (!_voiceEvents.containsKey(voiceId))
      _voiceEvents.put(voiceId, voiceEventDAO.readAll(Access.internal(), voiceId));

    return _voiceEvents.get(voiceId);
  }

  @Override
  public List<AudioEvent> instrumentAudioEvents(ULong instrumentId) throws Exception {
    if (!_audioWithFirstEvent.containsKey(instrumentId))
      _audioWithFirstEvent.put(instrumentId, audioEventDAO.readAllFirstEventsForInstrument(Access.internal(), instrumentId));

    return _audioWithFirstEvent.get(instrumentId);
  }

  @Override
  public List<Audio> instrumentAudios(ULong instrumentId) throws Exception {
    if (!_instrumentAudios.containsKey(instrumentId)) {
      _instrumentAudios.put(instrumentId, Lists.newArrayList());
      audioDAO.readAll(Access.internal(), instrumentId)
        .forEach(audioRecord -> _instrumentAudios.get(instrumentId)
          .add(new Audio().setFromRecord(audioRecord)));
    }

    return _instrumentAudios.get(instrumentId);
  }

  @Override
  public Audio linkAudio(ULong audioId) throws Exception {
    if (linkAudios().containsKey(audioId))
      return linkAudios().get(audioId);

    throw new BusinessException(String.format("Audio #%s is not in link picks!", audioId.toString()));
  }

  @Override
  public Map<ULong, Audio> linkAudios() throws Exception {
    if (Objects.isNull(_audiosFromPicks) || _audiosFromPicks.size() == 0) {
      _audiosFromPicks = Maps.newHashMap();
      audioDAO.readAllPickedForLink(Access.internal(), link().getId())
        .forEach(audioRecord -> _audiosFromPicks.put(
          audioRecord.getId(),
          new Audio().setFromRecord(audioRecord)));
    }

    return _audiosFromPicks;
  }

  @Override
  public List<ULong> linkAudioIds() throws Exception {
    return ImmutableList.copyOf(linkAudios().keySet());
  }

  @Override
  public List<LinkChord> linkChords() throws Exception {
    if (Objects.isNull(_linkChords) || _linkChords.size() == 0) {
      _linkChords = Lists.newArrayList();
      linkChordDAO.readAll(Access.internal(), link().getId())
        .forEach(chordRecord -> _linkChords.add(
          new LinkChord().setFromRecord(chordRecord)));
    }

    return _linkChords;
  }

  @Override
  public List<LinkMeme> linkMemes() throws Exception {
    if (Objects.isNull(_linkMemes) || _linkMemes.size() == 0) {
      _linkMemes = Lists.newArrayList();
      linkMemeDAO.readAll(Access.internal(), link().getId())
        .forEach(memeRecord -> _linkMemes.add(linkMeme(memeRecord.getLinkId(), memeRecord.getName())));
    }

    return _linkMemes;
  }

  @Override
  public List<Pick> picks() throws Exception {
    if (Objects.isNull(_picks) || _picks.size() == 0) {
      _picks = Lists.newArrayList();
      pickDAO.readAllInLink(Access.internal(), link().getId())
        .forEach(pickRecord -> _picks.add(new Pick().setFromRecord(pickRecord)));
    }

    return _picks;
  }

  @Override
  public Duration linkTotalLength() throws Exception {
    if (Objects.isNull(link().getEndAt()))
      throw new BusinessException("Cannot compute total length of link with no end!");

    return Duration.ofMillis(link().getEndAt().getTime() - link().getBeginAt().getTime());
  }

  @Override
  public LinkMeme linkMeme(ULong linkId, String memeName) {
    return
      new LinkMeme()
        .setLinkId(linkId.toBigInteger())
        .setName(memeName);
  }

  @Override
  public Result<PhaseMemeRecord> phaseMemes(ULong phaseId) throws Exception {
    if (!_phaseMemes.containsKey(phaseId))
      _phaseMemes.put(phaseId, phaseMemeDAO.readAll(Access.internal(), phaseId));

    return _phaseMemes.get(phaseId);
  }

  @Override
  public PhaseRecord phaseByOffset(ULong ideaId, ULong phaseOffset) throws Exception {
    if (!_ideaPhasesByOffset.containsKey(ideaId))
      _ideaPhasesByOffset.put(ideaId, Maps.newHashMap());

    if (!_ideaPhasesByOffset.get(ideaId).containsKey(phaseOffset))
      _ideaPhasesByOffset.get(ideaId).put(phaseOffset,
        phaseDAO.readOneForIdea(Access.internal(), ideaId, phaseOffset));

    return _ideaPhasesByOffset.get(ideaId).get(phaseOffset);
  }

  @Override
  public Link linkByOffset(ULong chainId, ULong offset) throws Exception {
    if (!_linksByOffset.containsKey(chainId))
      _linksByOffset.put(chainId, Maps.newHashMap());

    if (!_linksByOffset.get(chainId).containsKey(offset))
      _linksByOffset.get(chainId).put(offset,
        linkDAO.readOneAtChainOffset(Access.internal(), chainId, offset));

    return new Link().setFromRecord(_linksByOffset.get(chainId).get(offset));
  }

  @Override
  public Choice linkChoiceByType(ULong linkId, IdeaType ideaType) throws Exception {
    if (!_linkChoicesByType.containsKey(linkId))
      _linkChoicesByType.put(linkId, Maps.newHashMap());

    if (!_linkChoicesByType.get(linkId).containsKey(ideaType))
      _linkChoicesByType.get(linkId).put(ideaType,
        choiceDAO.readOneLinkTypeWithAvailablePhaseOffsets(Access.internal(), linkId, ideaType));

    return _linkChoicesByType.get(linkId).get(ideaType);
  }

  @Override
  public Result<VoiceRecord> voices(ULong phaseId) throws Exception {
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

    if (report.size() == 0)
      return;

    String body = new Yaml().dumpAsMap(report);
    try {
      linkMessageDAO.create(Access.internal(),
        new LinkMessage()
          .setLinkId(linkId().toBigInteger())
          .setType(MessageType.Info)
          .setBody(body));

    } catch (Exception e) {
      log.warn("Failed to send final craft report message for Link {} Message {}", _link, body, e);
    }
  }

  @Override
  public long atMicros(Double seconds) {
    return (long) (seconds * MICROSECONDS_PER_SECOND);
  }

  /**
   * real output channels based on chain configs
   *
   * @return output channels
   */
  private int outputChannels() throws Exception {
    return Integer.parseInt(chainConfig(ChainConfigType.OutputChannels).getValue());
  }

  /**
   * real output sample bits based on chain configs
   *
   * @return output sample bits
   */
  private int outputSampleBits() throws Exception {
    return Integer.parseInt(chainConfig(ChainConfigType.OutputSampleBits).getValue());
  }

  /**
   * real output frame rate based on chain configs
   *
   * @return output frame rate, per second
   */
  private float outputFrameRate() throws Exception {
    return Integer.parseInt(chainConfig(ChainConfigType.OutputFrameRate).getValue());
  }

  /**
   * output encoding based on chain configs
   *
   * @return output encoding
   */
  private AudioFormat.Encoding outputEncoding() throws Exception {
    return new AudioFormat.Encoding(chainConfig(ChainConfigType.OutputEncoding).getValue());
  }
}
