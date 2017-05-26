// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.craftworker;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.basis.Basis;
import io.outright.xj.core.basis.BasisFactory;
import io.outright.xj.craftworker.craft.CraftFactory;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.work.WorkerOperation;

import com.google.inject.Inject;

public class CraftOperation implements WorkerOperation {
  //  private final static Logger log = LoggerFactory.getLogger(CraftLinkWorkerOperation.class);
  private final CraftFactory craftFactory;
  private final BasisFactory basisFactory;

  @Inject
  public CraftOperation(
    CraftFactory craftFactory,
    BasisFactory basisFactory
  ) {
    this.craftFactory = craftFactory;
    this.basisFactory = basisFactory;
  }

  @Override
  public void workOn(Link link) throws BusinessException, ConfigException {
    Basis basis = basisFactory.createBasis(link);
    craftFactory.foundation(basis).doWork();
    craftFactory.structure(basis).doWork();
    craftFactory.voice(basis).doWork();
    basis.sendReport();
  }
}
