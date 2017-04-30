// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.dubworker.work;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.dao.LinkDAO;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.work.WorkerOperation;

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DubLinkWorkerOperation implements WorkerOperation {
  private final static Logger log = LoggerFactory.getLogger(DubLinkWorkerOperation.class);
  private LinkDAO linkDAO;

  @Inject
  public DubLinkWorkerOperation(
    LinkDAO linkDAO
  ) {
    this.linkDAO = linkDAO;
  }

  @Override
  public void workOn(Link link) throws BusinessException {
    try {
      // TODO actually dub link! use real endAt!
      log.info("Fake dub link {}", link);
    } catch (Exception e) {
      throw new BusinessException("DubLinkWorkerOperation failed (" + e.getClass().getName() + ") " + e.getMessage());
    }
  }

}
