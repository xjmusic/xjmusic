// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.craft.impl;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.craft.Basis;
import io.outright.xj.core.dao.IdeaDAO;
import io.outright.xj.core.dao.IdeaMemeDAO;
import io.outright.xj.core.dao.LinkDAO;
import io.outright.xj.core.dao.LinkMemeDAO;
import io.outright.xj.core.dao.LinkMessageDAO;
import io.outright.xj.core.dao.PhaseDAO;
import io.outright.xj.core.dao.PhaseMemeDAO;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.link.LinkChoice;
import io.outright.xj.core.model.link_meme.LinkMeme;
import io.outright.xj.core.model.link_message.LinkMessage;
import io.outright.xj.core.model.meme.Meme;
import io.outright.xj.core.model.message.Message;
import io.outright.xj.core.tables.records.IdeaMemeRecord;
import io.outright.xj.core.tables.records.IdeaRecord;
import io.outright.xj.core.tables.records.LinkRecord;
import io.outright.xj.core.tables.records.PhaseMemeRecord;
import io.outright.xj.core.tables.records.PhaseRecord;
import io.outright.xj.core.util.Value;

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
  private final Map<String, Object> report = Maps.newHashMap();
  private final IdeaDAO ideaDAO;
  private final IdeaMemeDAO ideaMemeDAO;
  private Link _link;
  private final LinkDAO linkDAO;
  private final LinkMessageDAO linkMessageDAO;
  private final PhaseDAO phaseDAO;
  private final PhaseMemeDAO phaseMemeDAO;
  private Map<ULong, Result<IdeaMemeRecord>> _ideaMemes = Maps.newHashMap();
  private Map<ULong, Result<PhaseMemeRecord>> _phaseMemes = Maps.newHashMap();
  private Map<ULong, Map<ULong, PhaseRecord>> _ideaPhasesByOffset = Maps.newHashMap();
  private Map<ULong, Map<ULong, LinkRecord>> _linksByOffset = Maps.newHashMap();
  private Map<ULong, Map<String, LinkChoice>> _linkChoicesByType = Maps.newHashMap();
  private Map<ULong, IdeaRecord> _ideas = Maps.newHashMap();
  private Boolean _sentReport = false;
  private Type _type;
  private LinkMemeDAO linkMemeDAO;

  @Inject
  public BasisImpl(
    @Assisted("link") Link link,
    IdeaDAO ideaDAO,
    IdeaMemeDAO ideaMemeDAO,
    LinkDAO linkDAO,
    LinkMemeDAO linkMemeDAO,
    LinkMessageDAO linkMessageDAO,
    PhaseDAO phaseDAO,
    PhaseMemeDAO phaseMemeDAO
  /*-*/) throws BusinessException {
    this._link = link;
    this.ideaDAO = ideaDAO;
    this.ideaMemeDAO = ideaMemeDAO;
    this.linkDAO = linkDAO;
    this.linkMemeDAO = linkMemeDAO;
    this.linkMessageDAO = linkMessageDAO;
    this.phaseDAO = phaseDAO;
    this.phaseMemeDAO = phaseMemeDAO;
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
  public LinkChoice previousMacroChoice() throws Exception {
    return linkChoiceByType(previousLink().getId(), Idea.MACRO);
  }

  @Override
  public LinkChoice previousMainChoice() throws Exception {
    return linkChoiceByType(previousLink().getId(), Idea.MAIN);
  }

  @Override
  public LinkChoice previousRhythmChoice() throws Exception {
    return linkChoiceByType(previousLink().getId(), Idea.RHYTHM);
  }

  @Override
  public LinkChoice currentMacroChoice() throws Exception {
    return linkChoiceByType(link().getId(), Idea.MACRO);
  }

  @Override
  public LinkChoice currentMainChoice() throws Exception {
    return linkChoiceByType(link().getId(), Idea.MAIN);
  }

  @Override
  public LinkChoice currentRhythmChoice() throws Exception {
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
  public Result<IdeaMemeRecord> ideaMemes(ULong ideaId) throws Exception {
    if (!_ideaMemes.containsKey(ideaId))
      _ideaMemes.put(ideaId, ideaMemeDAO.readAll(Access.internal(), ideaId));

    return _ideaMemes.get(ideaId);
  }

  @Override
  public List<Meme> linkMemes() throws Exception {
    List<Meme> memes = Lists.newArrayList();

    linkMemeDAO.readAll(Access.internal(), link().getId())
      .forEach(memeRecord -> memes.add(linkMeme(memeRecord.getLinkId(), memeRecord.getName())));

    return memes;
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
  public LinkChoice linkChoiceByType(ULong linkOffset, String type) throws Exception {
    if (!_linkChoicesByType.containsKey(linkOffset))
      _linkChoicesByType.put(linkOffset, Maps.newHashMap());

    if (!_linkChoicesByType.get(linkOffset).containsKey(type))
      _linkChoicesByType.get(linkOffset).put(type,
        linkDAO.readLinkChoice(Access.internal(), linkOffset, type));

    return _linkChoicesByType.get(linkOffset).get(type);
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
