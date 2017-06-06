// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.dubworker;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.basis.Basis;
import io.xj.core.basis.BasisFactory;
import io.xj.core.model.link.Link;
import io.xj.core.chain_gang.ChainGangOperation;
import io.xj.dubworker.dub.DubFactory;

import com.google.inject.Inject;

/**
 Consumes a task that is a JSONObject of a Link
 */
public class DubChainGangOperation implements ChainGangOperation {
  //  private final static Logger log = LoggerFactory.getLogger(DubLinkWorkerOperation.class);
  private final DubFactory dubFactory;
  private final BasisFactory basisFactory;

  @Inject
  public DubChainGangOperation(
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
