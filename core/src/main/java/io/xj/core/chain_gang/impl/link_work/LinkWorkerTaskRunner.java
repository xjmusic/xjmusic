// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.chain_gang.impl.link_work;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.CancelException;
import io.xj.core.chain_gang.ChainGangOperation;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.LinkDAO;
import io.xj.core.dao.LinkMessageDAO;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;
import io.xj.core.model.link_message.LinkMessage;
import io.xj.core.model.message.MessageType;
import io.xj.core.util.Text;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 This runnable is executed in a thread pool
 */
public class LinkWorkerTaskRunner implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(LinkWorkerTaskRunner.class);
  private final ChainDAO chainDAO;
  private final LinkDAO linkDAO;
  private final LinkMessageDAO linkMessageDAO;
  private final Link link;
  private final LinkState workingState;
  private final LinkState finishedState;
  private final ChainGangOperation chainGangOperation;

  LinkWorkerTaskRunner(ChainDAO chainDAO, LinkDAO linkDAO, LinkMessageDAO linkMessageDAO, ChainGangOperation chainGangOperation, Link link, LinkState workingState, LinkState finishedState) {
    this.chainDAO = chainDAO;
    this.linkMessageDAO = linkMessageDAO;
    this.link = link;
    this.workingState = workingState;
    this.finishedState = finishedState;
    this.chainGangOperation = chainGangOperation;
    this.linkDAO = linkDAO;
  }

  @Override
  public void run() {
    try {
      before();
      chainGangOperation.workOn(link);
      success();

    } catch (CancelException e) {
      cancel(e);

    } catch (Exception e) {
      failure(e);
    }
  }

  /**
   After Link Work
   */
  private void success() throws Exception {
    link.setState(finishedState.toString());
    linkDAO.update(Access.internal(), link.getId(), link);
    log.info("LinkWorker[{}] successful; updated link {} to finished state {}", Thread.currentThread().getName(), link, finishedState);
  }

  /**
   Before Link Work
   */
  private void before() throws Exception {
    link.setState(workingState.toString());
    linkDAO.update(Access.internal(), link.getId(), link);
    log.debug("LinkWorker[{}] updated link {} to working state {}", Thread.currentThread().getName(), link, workingState);
  }

  /**
   [#227] Chain fabrication: Link and Chain enters "failed" state if there is a blocking error

   @param e exception that caused failure
   */
  private void failure(Exception e) {
    try {
      linkMessageDAO.create(Access.internal(), newLinkMessage(MessageType.Error, e));

      linkDAO.updateState(Access.internal(), link.getId(), LinkState.Failed);
      log.error("LinkWorker[{}] updated link {} to FAILED state, on exception: {}", Thread.currentThread().getName(), link, e);

      chainDAO.updateState(Access.internal(), link.getChainId(), ChainState.Failed);
      log.error("LinkWorker[{}] updated chain #{} to FAILED state", Thread.currentThread().getName(), link.getChainId());

    } catch (Exception e2) {
      log.error("LinkWorker[{}] failure failed! Link:{} OriginalException:{} SecondException:{}", Thread.currentThread().getName(), link, e, e2);
    }
  }

  /**
   [#276] Link state transition errors should not cause chain failure

   @param e exception that caused failure
   */
  private void cancel(Exception e) {
    log.warn("LinkWorker[{}] canceled work on link {} on exception: {}", Thread.currentThread().getName(), link, e);
  }

  /**
   [#226] Messages pertaining to a Link

   @param type of message
   @param e    exception
   @return new link message
   */
  private LinkMessage newLinkMessage(MessageType type, Exception e) {
    return new LinkMessage()
      .setLinkId(link.getId().toBigInteger())
      .setType(type)
      .setBody(e.getMessage() + " " + Text.formatStackTrace(e));
  }

}
