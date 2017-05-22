// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.craft.impl;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.config.Config;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.craft.Basis;
import io.outright.xj.core.dao.ArrangementDAO;
import io.outright.xj.core.dao.AudioDAO;
import io.outright.xj.core.dao.AudioEventDAO;
import io.outright.xj.core.dao.ChoiceDAO;
import io.outright.xj.core.dao.IdeaDAO;
import io.outright.xj.core.dao.IdeaMemeDAO;
import io.outright.xj.core.dao.LinkChordDAO;
import io.outright.xj.core.dao.LinkDAO;
import io.outright.xj.core.dao.LinkMemeDAO;
import io.outright.xj.core.dao.LinkMessageDAO;
import io.outright.xj.core.dao.PhaseDAO;
import io.outright.xj.core.dao.PhaseMemeDAO;
import io.outright.xj.core.dao.VoiceDAO;
import io.outright.xj.core.dao.VoiceEventDAO;
import io.outright.xj.core.model.arrangement.Arrangement;
import io.outright.xj.core.model.audio.Audio;
import io.outright.xj.core.model.audio_event.AudioEvent;
import io.outright.xj.core.model.choice.Choice;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.link_chord.LinkChord;
import io.outright.xj.core.model.link_meme.LinkMeme;
import io.outright.xj.core.model.link_message.LinkMessage;
import io.outright.xj.core.model.message.Message;
import io.outright.xj.core.tables.records.IdeaMemeRecord;
import io.outright.xj.core.tables.records.IdeaRecord;
import io.outright.xj.core.tables.records.LinkRecord;
import io.outright.xj.core.tables.records.PhaseMemeRecord;
import io.outright.xj.core.tables.records.PhaseRecord;
import io.outright.xj.core.tables.records.VoiceEventRecord;
import io.outright.xj.core.tables.records.VoiceRecord;
import io.outright.xj.core.util.Value;
import io.outright.xj.music.BPM;
import io.outright.xj.music.Chord;
import io.outright.xj.music.MusicalException;
import io.outright.xj.music.Note;
import io.outright.xj.music.Tuning;

import org.jooq.Result;
import org.jooq.types.ULong;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 [#214] If a Chain has Ideas associated with it directly, prefer those choices to any in the Library
 */
public class BasisImpl implements Basis {
  private final Logger log = LoggerFactory.getLogger(BasisImpl.class);
  private Boolean _sentReport = false;
  private final ArrangementDAO arrangementDAO;
  private final AudioDAO audioDAO;
  private final AudioEventDAO audioEventDAO;
  private final ChoiceDAO choiceDAO;
  private final IdeaDAO ideaDAO;
  private final IdeaMemeDAO ideaMemeDAO;
  private final LinkChordDAO linkChordDAO;
  private final LinkDAO linkDAO;
  private final LinkMemeDAO linkMemeDAO;
  private final LinkMessageDAO linkMessageDAO;
  private final Map<String, Object> report = Maps.newHashMap();
  private final PhaseDAO phaseDAO;
  private final PhaseMemeDAO phaseMemeDAO;
  private final Tuning tuning;
  private final VoiceDAO voiceDAO;
  private final VoiceEventDAO voiceEventDAO;
  private Link _link;
  private List<Arrangement> _choiceArrangements;
  private List<LinkChord> _linkChords;
  private List<LinkMeme> _linkMemes;
  private Map<ULong, IdeaRecord> _ideas = Maps.newHashMap();
  private Map<ULong, List<AudioEvent>> _audioWithFirstEvent = Maps.newHashMap();
  private Map<ULong, List<Audio>> _instrumentAudios = Maps.newHashMap();
  private Map<ULong, Map<String, Choice>> _linkChoicesByType = Maps.newHashMap();
  private Map<ULong, Map<ULong, LinkRecord>> _linksByOffset = Maps.newHashMap();
  private Map<ULong, Map<ULong, PhaseRecord>> _ideaPhasesByOffset = Maps.newHashMap();
  private Map<ULong, Result<IdeaMemeRecord>> _ideaMemes = Maps.newHashMap();
  private Map<ULong, Result<PhaseMemeRecord>> _phaseMemes = Maps.newHashMap();
  private Map<ULong, Result<VoiceEventRecord>> _voiceEvents = Maps.newHashMap();
  private Map<ULong, Result<VoiceRecord>> _voicesByPhase = Maps.newHashMap();
  private Type _type;

  @Inject
  public BasisImpl(
    @Assisted("link") Link link,
    ArrangementDAO arrangementDAO,
    AudioDAO audioDAO,
    AudioEventDAO audioEventDAO,
    ChoiceDAO choiceDAO,
    IdeaDAO ideaDAO,
    IdeaMemeDAO ideaMemeDAO,
    LinkDAO linkDAO,
    LinkChordDAO linkChordDAO,
    LinkMemeDAO linkMemeDAO,
    LinkMessageDAO linkMessageDAO,
    PhaseDAO phaseDAO,
    PhaseMemeDAO phaseMemeDAO,
    VoiceDAO voiceDAO,
    VoiceEventDAO voiceEventDAO
  /*-*/) throws BusinessException {
    this._link = link;
    this.arrangementDAO = arrangementDAO;
    this.audioDAO = audioDAO;
    this.audioEventDAO = audioEventDAO;
    this.choiceDAO = choiceDAO;
    this.ideaDAO = ideaDAO;
    this.ideaMemeDAO = ideaMemeDAO;
    this.linkDAO = linkDAO;
    this.linkChordDAO = linkChordDAO;
    this.linkMemeDAO = linkMemeDAO;
    this.linkMessageDAO = linkMessageDAO;
    this.phaseDAO = phaseDAO;
    this.phaseMemeDAO = phaseMemeDAO;
    this.voiceDAO = voiceDAO;
    this.voiceEventDAO = voiceEventDAO;

    // [#255] Tuning based on root note configured in environment parameters.
    try {
      this.tuning = Tuning.at(
        Note.of(Config.tuningRootNote()),
        Config.tuningRootPitch());
    } catch (MusicalException e) {
      throw new BusinessException("Could not tune XJ!", e);
    }
  }

  @Override
  public Type type() {
    if (Objects.isNull(_type))
      try {
        if (isInitialLink())
          _type = Type.Initial;
        else if (previousMainChoice().hasOneMorePhase())
          _type = Type.Continue;
        else if (previousMacroChoice().hasTwoMorePhases())
          _type = Type.NextMain;
        else
          _type = Type.NextMacro;

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
  public Timestamp linkBeginAt() {
    return _link.getBeginAt();
  }

  @Override
  public Link previousLink() throws Exception {
    return linkByOffset(chainId(), Value.inc(_link.getOffset(), -1));
  }

  @Override
  public Choice previousMacroChoice() throws Exception {
    return linkChoiceByType(previousLink().getId(), Idea.MACRO);
  }

  @Override
  public Choice previousMainChoice() throws Exception {
    return linkChoiceByType(previousLink().getId(), Idea.MAIN);
  }

  @Override
  public Choice previousRhythmChoice() throws Exception {
    return linkChoiceByType(previousLink().getId(), Idea.RHYTHM);
  }

  @Override
  public List<Arrangement> previousPercussiveArrangements() throws Exception {
    return choiceArrangements(previousRhythmChoice().getId());
  }

  @Override
  public Choice currentMacroChoice() throws Exception {
    return linkChoiceByType(link().getId(), Idea.MACRO);
  }

  @Override
  public Choice currentMainChoice() throws Exception {
    return linkChoiceByType(link().getId(), Idea.MAIN);
  }

  @Override
  public Choice currentRhythmChoice() throws Exception {
    return linkChoiceByType(link().getId(), Idea.RHYTHM);
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
  public Choice linkChoiceByType(ULong linkOffset, String type) throws Exception {
    if (!_linkChoicesByType.containsKey(linkOffset))
      _linkChoicesByType.put(linkOffset, Maps.newHashMap());

    if (!_linkChoicesByType.get(linkOffset).containsKey(type))
      _linkChoicesByType.get(linkOffset).put(type,
        choiceDAO.readOneLinkTypeWithAvailablePhaseOffsets(Access.internal(), linkOffset, type));

    return _linkChoicesByType.get(linkOffset).get(type);
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
    this._link = link;
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
          .setType(Message.INFO)
          .setBody(body));

    } catch (Exception e) {
      log.warn("Failed to send final craft report message for Link {} Message {}", _link, body, e);
    }
  }

}
