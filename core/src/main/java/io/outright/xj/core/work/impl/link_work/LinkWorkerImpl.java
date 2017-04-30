// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.work.impl.link_work;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.dao.LinkDAO;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.link.LinkWrapper;
import io.outright.xj.core.work.Worker;
import io.outright.xj.core.work.WorkerOperation;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkWorkerImpl implements Worker {
  private final static Logger log = LoggerFactory.getLogger(LinkWorkerImpl.class);
  private String workingState;
  private String finishedState;
  private final LinkDAO linkDAO;
  private WorkerOperation operation;

  @AssistedInject
  public LinkWorkerImpl(
    LinkDAO linkDAO,
    @Assisted("workingState") String workingState,
    @Assisted("finishedState") String finishedState,
    @Assisted("operation") WorkerOperation operation
  ) {
    this.workingState = workingState;
    this.finishedState = finishedState;
    this.linkDAO = linkDAO;
    this.operation = operation;
  }

  @AssistedInject
  public LinkWorkerImpl(
    LinkDAO linkDAO,
    @Assisted("finishedState") String finishedState
  ) {
    this.finishedState = finishedState;
    this.linkDAO = linkDAO;
  }

  @Override
  public Runnable getTaskRunnable(JSONObject task) throws Exception {
    return new LinkWorkerTaskRunner(
      Link.fromJSON(task),
      workingState,
      finishedState,
      operation,
      linkDAO
    );
  }

  /**
   This stateless runnable is then executed in a thread pool
   */
  public class LinkWorkerTaskRunner implements Runnable {
    private Link link;
    private String workingState;
    private String finishedState;
    private WorkerOperation operation;
    private final LinkDAO linkDAO;

    LinkWorkerTaskRunner(Link link, String workingState, String finishedState, WorkerOperation operation, LinkDAO linkDAO) throws BusinessException {
      this.link = link;
      this.workingState = workingState;
      this.finishedState = finishedState;
      this.operation = operation;
      this.linkDAO = linkDAO;
    }

    @Override
    public void run() {
      try {
        link.setState(workingState);
        linkDAO.update(AccessControl.forInternalWorker(), link.getId(), new LinkWrapper().setLink(link));
        log.debug("LinkWorker[" + Thread.currentThread().getName() + "] updated link {} to working state {}", link, workingState);

        // Here's the main callback to whatever the particular client-implementor app's business is.
        operation.workOn(link);
        //        ^ here

        link.setState(finishedState);
        linkDAO.update(AccessControl.forInternalWorker(), link.getId(), new LinkWrapper().setLink(link));
        log.debug("LinkWorker[" + Thread.currentThread().getName() + "] updated link {} to finished state {}", link, finishedState);
      } catch (Exception e) {
        log.error("LinkWorker[" + Thread.currentThread().getName() + "] processing work", e);
      }
    }

  }

}

