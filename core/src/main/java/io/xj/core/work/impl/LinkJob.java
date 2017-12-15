// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.work.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.LinkDAO;
import io.xj.core.dao.LinkMessageDAO;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.link_message.LinkMessage;
import io.xj.core.model.message.MessageType;
import io.xj.core.util.Text;
import io.xj.core.work.basis.BasisFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.math.BigInteger;
import java.util.Objects;

public abstract class LinkJob implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(LinkJob.class);
  private static final long MILLIS_PER_SECOND = 1000;

  protected String name;
  protected LinkDAO linkDAO;
  protected LinkMessageDAO linkMessageDAO;
  protected BigInteger entityId;
  protected BasisFactory basisFactory;

  protected LinkState fromState;
  protected LinkState workingState;
  protected LinkState toState;

  @Override
  public void run() {
    int count = 0;
    int maxTries = Config.workLinkDubRetryLimit();
    while (true) {
      try {
        Link link = linkDAO.readOne(Access.internal(), entityId);
        if (Objects.isNull(link)) {
          log.error("Cannot begin {} nonexistent Link", name);
          return;
        }
        log.info("Begin {} Link (id={})", name, entityId);
        link = updateToWorkingState(link);
        if (Objects.isNull(link)) {
          // future .. care more that this job was unable to be completed because of a link state problem?
          return;
        }
        doWork(link);
        updateToFinishedState();
        return;

      } catch (Exception e) {
        ++count;
        if (count == maxTries) {
          log.error("{} Link (id={}) failed ({})",
            name, entityId, e);
          createLinkMessage(MessageType.Error, String.format("%s Link (id=%s) failed (%s)! %s", name, entityId, e.getMessage(), Text.formatStackTrace(e)));
          return;

        } else try {
          log.warn("{} Link (id={}) failed ({}) Will retry...",
            name, entityId, e.getMessage(), e);
          createLinkMessage(MessageType.Warning, String.format("%s Link (id=%s) failed (%s); Will Retry... %s", name, entityId, e.getMessage(), Text.formatStackTrace(e)));
          // future: don't busy-wait (Thread.sleep), re-queue!
          Thread.sleep(Config.workLinkDubRetrySleepSeconds() * MILLIS_PER_SECOND);
          // this is the only branch that goes back to to the top of the while(true) loop...

        } catch (InterruptedException sleepException) {
          log.warn("{} Link (id={}) was going to retry, but sleep was interrupted ({})",
            name, entityId, sleepException);
          return;
        }
      }
    }
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

   @param link to update
   @return Link model, updated to working state
   @throws Exception on failure
   */
  @Nullable
  private Link updateToWorkingState(Link link) throws Exception {
    if (fromState != link.getState()) {
      log.error("{} requires Link must be in {} state.", name, fromState);
      return null;
    }
    linkDAO.updateState(Access.internal(), link.getId(), workingState);
    return link.setStateEnum(workingState);
  }

  /**
   Update Link to finished state
   */
  private void updateToFinishedState() throws Exception {
    linkDAO.updateState(Access.internal(), entityId, toState);
    log.info("Done {} Link OK (id={})", name, entityId);
  }

  /**
   Do Work

   @param link to do work on
   @throws Exception on failure
   */
  protected abstract void doWork(Link link) throws Exception;
}
