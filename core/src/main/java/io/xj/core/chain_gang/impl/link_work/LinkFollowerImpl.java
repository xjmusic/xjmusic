// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.chain_gang.impl.link_work;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.app.exception.CancelException;
import io.xj.core.chain_gang.ChainGangOperation;
import io.xj.core.chain_gang.Follower;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.LinkDAO;
import io.xj.core.dao.LinkMessageDAO;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.link.Link;
import io.xj.core.model.link_message.LinkMessage;
import io.xj.core.model.message.MessageType;
import io.xj.core.util.Text;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkFollowerImpl implements Follower {
  private final static Logger log = LoggerFactory.getLogger(LinkFollowerImpl.class);
  private final LinkDAO linkDAO;
  private final ChainDAO chainDAO;
  private final LinkMessageDAO linkMessageDAO;
  private String workingState;
  private String finishedState;
  private ChainGangOperation chainGangOperation;

  @AssistedInject
  public LinkFollowerImpl(
    LinkDAO linkDAO,
    ChainDAO chainDAO, LinkMessageDAO linkMessageDAO, @Assisted("workingState") String workingState,
    @Assisted("finishedState") String finishedState,
    @Assisted("operation") ChainGangOperation chainGangOperation
  ) {
    this.chainDAO = chainDAO;
    this.linkMessageDAO = linkMessageDAO;
    this.workingState = workingState;
    this.finishedState = finishedState;
    this.linkDAO = linkDAO;
    this.chainGangOperation = chainGangOperation;
  }

  @AssistedInject
  public LinkFollowerImpl(
    LinkDAO linkDAO,
    ChainDAO chainDAO, LinkMessageDAO linkMessageDAO, @Assisted("finishedState") String finishedState
  ) {
    this.chainDAO = chainDAO;
    this.linkMessageDAO = linkMessageDAO;
    this.finishedState = finishedState;
    this.linkDAO = linkDAO;
  }

  @Override
  public Runnable getTaskRunnable(JSONObject task) throws Exception {
    Link link = new Link().setFromJSON(task);
    return new LinkWorkerTaskRunner(chainDAO, linkDAO, linkMessageDAO, chainGangOperation, link, workingState, finishedState);
  }

  /**
   This runnable is executed in a thread pool
   */
  public class LinkWorkerTaskRunner implements Runnable {
    private final ChainDAO chainDAO;
    private final LinkDAO linkDAO;
    private final LinkMessageDAO linkMessageDAO;
    private Link link;
    private String workingState;
    private String finishedState;
    private ChainGangOperation chainGangOperation;

    LinkWorkerTaskRunner(ChainDAO chainDAO, LinkDAO linkDAO, LinkMessageDAO linkMessageDAO, ChainGangOperation chainGangOperation, Link link, String workingState, String finishedState) throws BusinessException {
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
      link.setState(finishedState);
      linkDAO.update(Access.internal(), link.getId(), link);
      log.info("LinkWorker[" + Thread.currentThread().getName() + "] successful; updated link {} to finished state {}", link, finishedState);
    }

    /**
     Before Link Work
     */
    private void before() throws Exception {
      link.setState(workingState);
      linkDAO.update(Access.internal(), link.getId(), link);
      log.debug("LinkWorker[" + Thread.currentThread().getName() + "] updated link {} to working state {}", link, workingState);
    }

    /**
     [#227] Chain fabrication: Link and Chain enters "failed" state if there is a blocking error

     @param e exception that caused failure
     */
    private void failure(Exception e) {
      try {
        linkMessageDAO.create(Access.internal(), newLinkMessage(MessageType.Error, e));

        linkDAO.updateState(Access.internal(), link.getId(), Link.FAILED);
        log.error("LinkWorker[" + Thread.currentThread().getName() + "] updated link {} to FAILED state, on exception: {}", link, e);

        chainDAO.updateState(Access.internal(), link.getChainId(), Chain.FAILED);
        log.error("LinkWorker[" + Thread.currentThread().getName() + "] updated chain #{} to FAILED state", link.getChainId());

      } catch (Exception e2) {
        log.error("LinkWorker[" + Thread.currentThread().getName() + "] failure failed! Link:{} OriginalException:{} SecondException:{}", link, e, e2);
      }
    }

    /**
     [#276] Link state transition errors should not cause chain failure

     @param e exception that caused failure
     */
    private void cancel(Exception e) {
      log.warn("LinkWorker[" + Thread.currentThread().getName() + "] canceled work on link {} on exception: {}", link, e);
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

}

