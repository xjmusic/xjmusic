// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.chain_gang.impl.link_pilot_work;

import io.xj.core.app.access.impl.Access;
import io.xj.core.app.exception.BusinessException;
import io.xj.core.dao.LinkDAO;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;
import io.xj.core.transport.JSON;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.sql.Timestamp;

/**
 This runnable is executed in a thread pool
 */
public class LinkPilotWorkerTaskRunner implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(LinkPilotWorkerTaskRunner.class);
  private final LinkDAO linkDAO;
  private final Link newLink;

  LinkPilotWorkerTaskRunner(BigInteger chainId, BigInteger linkOffset, Timestamp linkBeginAt, LinkState linkInitState, LinkDAO linkDAO) throws BusinessException {
    this.linkDAO = linkDAO;

    newLink = new Link()
      .setChainId(chainId)
      .setOffset(linkOffset)
      .setBeginAtTimestamp(linkBeginAt)
      .setState(linkInitState.toString());

    newLink.validate();
  }

  @Override
  public void run() {
    try {
      JSONObject newLinkToRun = JSON.objectFromRecord(linkDAO.create(Access.internal(), newLink));
      log.info("PilotWorker[{}] readMany link: {}", Thread.currentThread().getName(), newLinkToRun);
    } catch (BusinessException e) {
      log.debug("PilotWorker[{}] BusinessException: {}", Thread.currentThread().getName(), e.getMessage());
    } catch (Exception e) {
      log.error("PilotWorker[{}] processing work", Thread.currentThread().getName(), e);
    }
  }

}
