// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.craftworker.work;

import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.dao.LinkDAO;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.model.link.LinkWrapper;
import io.outright.xj.core.work.WorkerOperation;

import com.google.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;

public class CraftLinkWorkerOperation implements WorkerOperation {
  private final static Logger log = LoggerFactory.getLogger(CraftLinkWorkerOperation.class);
  private LinkDAO linkDAO;

  @Inject
  public CraftLinkWorkerOperation(
    LinkDAO linkDAO
  ) {
    this.linkDAO = linkDAO;
  }

  @Override
  public void workOn(Link link) throws BusinessException {
    try {
      link.setEndAt(Timestamp.from(link.getBeginAt().toInstant().plusSeconds(30)));
      // TODO actually craft link! use real endAt!
      linkDAO.update(AccessControl.forInternalWorker(), link.getId(), new LinkWrapper().setLink(link));
    } catch (Exception e) {
      throw new BusinessException("CraftLinkWorkerOperation failed (" + e.getClass().getName() + ") " + e.getMessage());
    }
  }

}
