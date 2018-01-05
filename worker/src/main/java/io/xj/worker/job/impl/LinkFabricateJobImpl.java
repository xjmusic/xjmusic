// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.LinkDAO;
import io.xj.core.dao.LinkMessageDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.link_message.LinkMessage;
import io.xj.core.model.message.MessageType;
import io.xj.core.util.Text;
import io.xj.core.work.basis.Basis;
import io.xj.core.work.basis.BasisFactory;
import io.xj.craft.CraftFactory;
import io.xj.dub.DubFactory;
import io.xj.worker.job.LinkFabricateJob;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Objects;

public class LinkFabricateJobImpl implements LinkFabricateJob {
  private static final Logger log = LoggerFactory.getLogger(LinkFabricateJobImpl.class);

  private final LinkDAO linkDAO;
  private final LinkMessageDAO linkMessageDAO;
  private final BigInteger entityId;
  private final BasisFactory basisFactory;
  private Basis basis;
  private Link link;


  private final CraftFactory craftFactory;
  private final DubFactory dubFactory;

  @Inject
  public LinkFabricateJobImpl(
    @Assisted("entityId") BigInteger entityId,
    CraftFactory craftFactory,
    BasisFactory basisFactory,
    LinkDAO linkDAO,
    LinkMessageDAO linkMessageDAO,
    DubFactory dubFactory
  ) {
    this.entityId = entityId;
    this.craftFactory = craftFactory;
    this.basisFactory = basisFactory;
    this.linkDAO = linkDAO;
    this.linkMessageDAO = linkMessageDAO;
    this.dubFactory = dubFactory;
  }

  /**
   Do the link Job
   */
  @Override
  public void run() {
    try {
      link = linkDAO.readOne(Access.internal(), entityId);
    } catch (Exception e) {
      didFailWhile("retrieving", e);
      return;
    }

    if (Objects.isNull(link)) {
      didFailWhile("retrieving null");
      return;
    }

    try {
      basis = basisFactory.createBasis(link);
    } catch (ConfigException e) {
      didFailWhile("creating basis", e);
      return;
    }

    try {
      doCraftWork();
    } catch (Exception e) {
      didFailWhile("doing Craft work", e);
      return;
    }

    try {
      doDubWork();
    } catch (Exception e) {
      didFailWhile("doing Dub work", e);
      return;
    }

    try {
      finishWork();
    } catch (Exception e) {
      didFailWhile("finishing work", e);
    }
  }

  /**
   Finish work on Link
   */
  private void finishWork() throws Exception {
    updateLinkState(LinkState.Dubbing, LinkState.Dubbed);
    basis.sendReport();
  }

  /**
   Craft a Link, or fail

   @throws ConfigException   on configuration failure
   @throws BusinessException on failure
   */
  private void doCraftWork() throws Exception {
    updateLinkState(LinkState.Planned, LinkState.Crafting);
    craftFactory.foundation(basis).doWork();
    craftFactory.structure(basis).doWork();
    craftFactory.voice(basis).doWork();
  }

  /**
   Dub a Link, or fail

   @throws ConfigException   if mis-configured
   @throws BusinessException on failure
   */
  protected void doDubWork() throws Exception {
    updateLinkState(LinkState.Crafting, LinkState.Dubbing);
    dubFactory.master(basis).doWork();
    dubFactory.ship(basis).doWork();
  }

  /**
   Log and create link message of error that job failed while (message)

   @param message phrased like "Doing work"
   @param e       exception (optional)
   */
  private void didFailWhile(String message, Exception e) {
    createLinkMessage(MessageType.Error, String.format("Failed while %s for Link #%s:\n\n%s\n%s", message, entityId, e.getMessage(), Text.formatStackTrace(e)));
    log.error("Failed while {} for Link #{}", message, entityId, e);
  }

  /**
   Log and create link message of error that job failed while (message)

   @param message phrased like "Doing work"
   */
  private void didFailWhile(String message) {
    createLinkMessage(MessageType.Error, String.format("Failed while %s for Link #%s", message, entityId));
    log.error("Failed while {} for Link #{}", message, entityId);
  }

  /**
   Create a link message

   @param type of message
   @param body of message
   */
  protected void createLinkMessage(MessageType type, String body) {
    try {
      linkMessageDAO.create(Access.internal(),
        new LinkMessage()
          .setLinkId(entityId)
          .setBody(body)
          .setType(type.toString()));
    } catch (Exception e) {
      log.error("Could not create Link Message", e);
    }
  }

  /**
   Update Link to Working state

   @throws Exception on failure
   */
  private void updateLinkState(LinkState fromState, LinkState toState) throws Exception {
    if (fromState != link.getState()) {
      log.error("{} requires Link must be in {} state.", toState, fromState);
      return;
    }
    linkDAO.updateState(Access.internal(), link.getId(), toState);
    link.setStateEnum(toState);
    log.info("{} Link OK (id={})", toState, entityId);
  }

}
