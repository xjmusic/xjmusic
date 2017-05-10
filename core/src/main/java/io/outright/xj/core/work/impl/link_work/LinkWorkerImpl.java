// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.work.impl.link_work;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.dao.ChainDAO;
import io.outright.xj.core.dao.LinkDAO;
import io.outright.xj.core.dao.LinkMessageDAO;
import io.outright.xj.core.model.chain.Chain;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.link_message.LinkMessage;
import io.outright.xj.core.model.message.Message;
import io.outright.xj.core.work.Worker;
import io.outright.xj.core.work.WorkerOperation;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkWorkerImpl implements Worker {
  private final static Logger log = LoggerFactory.getLogger(LinkWorkerImpl.class);
  private final LinkDAO linkDAO;
  private final ChainDAO chainDAO;
  private final LinkMessageDAO linkMessageDAO;
  private String workingState;
  private String finishedState;
  private WorkerOperation operation;

  @AssistedInject
  public LinkWorkerImpl(
    LinkDAO linkDAO,
    ChainDAO chainDAO, LinkMessageDAO linkMessageDAO, @Assisted("workingState") String workingState,
    @Assisted("finishedState") String finishedState,
    @Assisted("operation") WorkerOperation operation
  ) {
    this.chainDAO = chainDAO;
    this.linkMessageDAO = linkMessageDAO;
    this.workingState = workingState;
    this.finishedState = finishedState;
    this.linkDAO = linkDAO;
    this.operation = operation;
  }

  @AssistedInject
  public LinkWorkerImpl(
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
    return new LinkWorkerTaskRunner(
      chainDAO, linkDAO, linkMessageDAO, operation, Link.fromJSON(task),
      workingState,
      finishedState
    );
  }

  /**
   This stateless runnable is then executed in a thread pool
   */
  public class LinkWorkerTaskRunner implements Runnable {
    private final ChainDAO chainDAO;
    private final LinkDAO linkDAO;
    private final LinkMessageDAO linkMessageDAO;
    private Link link;
    private String workingState;
    private String finishedState;
    private WorkerOperation operation;

    LinkWorkerTaskRunner(ChainDAO chainDAO, LinkDAO linkDAO, LinkMessageDAO linkMessageDAO, WorkerOperation operation, Link link, String workingState, String finishedState) throws BusinessException {
      this.chainDAO = chainDAO;
      this.linkMessageDAO = linkMessageDAO;
      this.link = link;
      this.workingState = workingState;
      this.finishedState = finishedState;
      this.operation = operation;
      this.linkDAO = linkDAO;
    }

    @Override
    public void run() {
      try {
        before();
        operation.workOn(link);
        success();

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
        // [#226] Messages pertaining to a Link
        linkMessageDAO.create(Access.internal(),
          new LinkMessage()
            .setLinkId(link.getId().toBigInteger())
            .setType(Message.ERROR)
            .setBody(e.getMessage()));

        linkDAO.updateState(Access.internal(), link.getId(), Link.FAILED);
        log.error("LinkWorker[" + Thread.currentThread().getName() + "] updated link {} to FAILED state, on exception: {}", link, e);

        chainDAO.updateState(Access.internal(), link.getChainId(), Chain.FAILED);
        log.error("LinkWorker[" + Thread.currentThread().getName() + "] updated chain #{} to FAILED state", link.getChainId());

      } catch (Exception e2) {
        log.error("LinkWorker[" + Thread.currentThread().getName() + "] failure failed! Link:{} OriginalException:{} SecondException:{}", link, e, e2);
      }
    }

  }

}

