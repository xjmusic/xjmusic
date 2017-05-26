// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.dubworker;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.basis.Basis;
import io.outright.xj.core.basis.BasisFactory;
import io.outright.xj.core.model.link.Link;
import io.outright.xj.core.work.WorkerOperation;
import io.outright.xj.dubworker.dub.DubFactory;

import com.google.inject.Inject;

public class DubOperation implements WorkerOperation {
  //  private final static Logger log = LoggerFactory.getLogger(DubLinkWorkerOperation.class);
  private final DubFactory dubFactory;
  private final BasisFactory basisFactory;

  @Inject
  public DubOperation(
    DubFactory dubFactory,
    BasisFactory basisFactory
  ) {
    this.dubFactory = dubFactory;
    this.basisFactory = basisFactory;
  }

  @Override
  public void workOn(Link link) throws BusinessException, ConfigException {
    Basis basis = basisFactory.createBasis(link);
    dubFactory.master(basis).doWork();
    dubFactory.ship(basis).doWork();
    basis.sendReport();
  }
}
