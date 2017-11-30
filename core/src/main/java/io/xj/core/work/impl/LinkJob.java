// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.work.impl;

import org.jooq.types.ULong;

import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.exception.WorkException;
import io.xj.core.dao.LinkDAO;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;
import io.xj.core.tables.records.LinkRecord;
import io.xj.core.work.basis.BasisFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public abstract class LinkJob implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(LinkJob.class);
  private static final long MILLIS_PER_SECOND = 1000;

  protected String name;
  protected LinkDAO linkDAO;
  protected ULong entityId;
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
        LinkRecord linkRecord = linkDAO.readOne(Access.internal(), entityId);
        if (Objects.isNull(linkRecord)) {
          throw new WorkException(String.format("Cannot begin %s nonexistent Link", name));
        }
        log.info("Begin {} Link (id={})", name, entityId);
        Link link = updateToWorkingState(linkRecord);
        doWork(link);
        updateToFinishedState();
        return;

      } catch (Exception e) {
        ++count;
        if (count == maxTries) {
          log.error("{} Link (id={}) failed ({})",
            name, entityId, e);
          return;

        } else try {
          log.warn("{} Link (id={}) failed ({}) Will retry...",
            name, entityId, e.getMessage(), e);
          Thread.sleep(Config.workLinkDubRetrySleepSeconds() * MILLIS_PER_SECOND);

        } catch (InterruptedException sleepException) {
          log.warn("{} Link (id={}) was going to retry, but sleep was interrupted ({})",
            name, entityId, sleepException);
          return;
        }
      }
    }
  }

  /**
   * Update Link to Working state
   *
   * @param linkRecord to update
   * @return Link model, updated to working state
   * @throws Exception on failure
   */
  private Link updateToWorkingState(LinkRecord linkRecord) throws Exception {
    if (!fromState.equals(linkRecord.getState())) {
      throw new WorkException(String.format("%s requires Link must be in %s state.", name, fromState.toString()));
    }
    linkDAO.updateState(Access.internal(), linkRecord.getId(), workingState);
    Link link = new Link().setFromRecord(linkRecord);
    if (Objects.isNull(link)) {
      throw new WorkException(String.format("%s requires Link must be in %s state.", name, workingState.toString()));
    }
    return link.setStateEnum(workingState);
  }

  /**
   * Update Link to finished state
   */
  private void updateToFinishedState() throws Exception {
    linkDAO.updateState(Access.internal(), entityId, toState);
    log.info("Done {} Link OK (id={})", name, entityId);
  }

  /**
   * Do Work
   *
   * @param link to do work on
   * @throws Exception on failure
   */
  protected abstract void doWork(Link link) throws Exception;
}
