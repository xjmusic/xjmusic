// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.craftworker.work;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.craft.CraftFactory;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.work.WorkerOperation;

import com.google.inject.Inject;

public class CraftLinkWorkerOperation implements WorkerOperation {
  //  private final static Logger log = LoggerFactory.getLogger(CraftLinkWorkerOperation.class);
  private final CraftFactory craftFactory;

  @Inject
  public CraftLinkWorkerOperation(
    CraftFactory craftFactory
  ) {
    this.craftFactory = craftFactory;
  }

  @Override
  public void workOn(Link link) throws BusinessException, ConfigException {
    craftFactory.createMacroCraft(link).craft();
  }
}
