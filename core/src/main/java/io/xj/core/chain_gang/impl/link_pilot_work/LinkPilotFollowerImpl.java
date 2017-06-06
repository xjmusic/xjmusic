// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.chain_gang.impl.link_pilot_work;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.dao.LinkDAO;
import io.xj.core.model.link.Link;
import io.xj.core.transport.JSON;
import io.xj.core.chain_gang.Follower;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Timestamp;

public class LinkPilotFollowerImpl implements Follower {
  private final static Logger log = LoggerFactory.getLogger(LinkPilotFollowerImpl.class);
  private final LinkDAO linkDAO;
  private String finishedState;

  @Inject
  public LinkPilotFollowerImpl(
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
   This runnable is executed in a thread pool
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

