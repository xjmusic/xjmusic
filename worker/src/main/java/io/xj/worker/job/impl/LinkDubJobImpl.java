// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.worker.job.impl;

import org.jooq.types.ULong;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.dao.LinkDAO;
import io.xj.core.model.link.Link;
import io.xj.core.model.link.LinkState;
import io.xj.core.work.impl.LinkJob;
import io.xj.core.work.basis.Basis;
import io.xj.core.work.basis.BasisFactory;
import io.xj.worker.job.LinkDubJob;
import io.xj.worker.work.dub.DubFactory;

public class LinkDubJobImpl extends LinkJob implements LinkDubJob {
  private final DubFactory dubFactory;

  @Inject
  public LinkDubJobImpl(
    @Assisted("entityId") ULong entityId,
    DubFactory dubFactory,
    BasisFactory basisFactory,
    LinkDAO linkDAO
  ) {
    this.entityId = entityId;
    this.dubFactory = dubFactory;
    this.basisFactory = basisFactory;
    this.linkDAO = linkDAO;

    this.name = "Dubbing";
    this.fromState = LinkState.Crafted;
    this.workingState = LinkState.Dubbing;
    this.toState = LinkState.Dubbed;
  }

  /**
   * Do the work
   *
   * @param link to work on
   * @throws ConfigException   if mis-configured
   * @throws BusinessException on failure
   */
  @Override
  protected void doWork(Link link) throws Exception {
    Basis basis = basisFactory.createBasis(link);
    dubFactory.master(basis).doWork();
    dubFactory.ship(basis).doWork();
    basis.sendReport();
  }

}
