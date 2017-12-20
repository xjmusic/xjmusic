// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import io.xj.core.dao.LinkDAO;
import io.xj.core.dao.LinkMessageDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;
import io.xj.core.work.basis.Basis;
import io.xj.core.work.basis.BasisFactory;
import io.xj.core.work.impl.LinkJob;
import io.xj.dub.DubFactory;
import io.xj.worker.job.LinkDubJob;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.math.BigInteger;

public class LinkDubJobImpl extends LinkJob implements LinkDubJob {
  private final DubFactory dubFactory;

  @Inject
  public LinkDubJobImpl(
    @Assisted("entityId") BigInteger entityId,
    DubFactory dubFactory,
    BasisFactory basisFactory,
    LinkDAO linkDAO,
    LinkMessageDAO linkMessageDAO

  ) {
    this.entityId = entityId;
    this.dubFactory = dubFactory;
    this.basisFactory = basisFactory;
    this.linkDAO = linkDAO;
    this.linkMessageDAO = linkMessageDAO;

    name = "Dubbing";
    fromState = LinkState.Crafted;
    workingState = LinkState.Dubbing;
    toState = LinkState.Dubbed;
  }

  /**
   Do the work

   @param link to work on
   @throws ConfigException   if mis-configured
   @throws BusinessException on failure
   */
  @Override
  protected void doWork(Link link) throws Exception {
    Basis basis = basisFactory.createBasis(link);
    dubFactory.master(basis).doWork();
    dubFactory.ship(basis).doWork();
    basis.sendReport();
  }

}
