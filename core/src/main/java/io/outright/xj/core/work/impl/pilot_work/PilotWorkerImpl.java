// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.work.impl.pilot_work;

import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.dao.LinkDAO;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.transport.JSON;
import io.outright.xj.core.work.Worker;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Timestamp;

public class PilotWorkerImpl implements Worker {
  private final static Logger log = LoggerFactory.getLogger(PilotWorkerImpl.class);
  private final LinkDAO linkDAO;
  private String finishedState;

  @Inject
  public PilotWorkerImpl(
    LinkDAO linkDAO,
    @Assisted("finishedState") String finishedState
  ) {
    this.finishedState = finishedState;
    this.linkDAO = linkDAO;
  }

  @Override
  public Runnable getTaskRunnable(JSONObject task) throws Exception {
    return new PilotWorkerTaskRunner(
      task.getBigInteger(Link.KEY_CHAIN_ID),
      task.getBigInteger(Link.KEY_OFFSET),
      Timestamp.valueOf(task.get(Link.KEY_BEGIN_AT).toString()),
      finishedState,
      linkDAO
    );
  }

  /**
   This stateless runnable is then executed in a thread pool
   */
  public class PilotWorkerTaskRunner implements Runnable {

    private final LinkDAO linkDAO;
    private Link newLink;

    PilotWorkerTaskRunner(BigInteger chainId, BigInteger linkOffset, Timestamp linkBeginAt, String linkInitState, LinkDAO linkDAO) throws BusinessException {
      this.linkDAO = linkDAO;

      this.newLink = new Link()
        .setChainId(chainId)
        .setOffset(linkOffset)
        .setBeginAtTimestamp(linkBeginAt)
        .setState(linkInitState);

      this.newLink.validate();
    }

    @Override
    public void run() {
      try {
        JSONObject newLink = JSON.objectFromRecord(linkDAO.create(Access.internal(), this.newLink));
        log.info("PilotWorker[" + Thread.currentThread().getName() + "] readMany link: {}", newLink);
      } catch (BusinessException e) {
        log.debug("PilotWorker[" + Thread.currentThread().getName() + "] BusinessException: " + e.getMessage());
      } catch (Exception e) {
        log.error("PilotWorker[" + Thread.currentThread().getName() + "] processing work", e);
      }
    }

  }

}

