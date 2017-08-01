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
import io.xj.core.work.LinkJob;
import io.xj.core.work.basis.Basis;
import io.xj.core.work.basis.BasisFactory;
import io.xj.worker.job.LinkCraftJob;
import io.xj.worker.work.craft.CraftFactory;

public class LinkCraftJobImpl extends LinkJob implements LinkCraftJob {
//  private static final Logger log = LoggerFactory.getLogger(LinkCraftJob.class);
  private final CraftFactory craftFactory;

  @Inject
  public LinkCraftJobImpl(
    @Assisted("entityId") ULong entityId,
    CraftFactory craftFactory,
    BasisFactory basisFactory,
    LinkDAO linkDAO
  ) {
    this.entityId = entityId;
    this.craftFactory = craftFactory;
    this.basisFactory = basisFactory;
    this.linkDAO = linkDAO;

    this.name = "Crafting";
    this.fromState = LinkState.Planned;
    this.workingState = LinkState.Crafting;
    this.toState = LinkState.Crafted;
  }

  /**
   * Craft a Link, or fail
   *
   * @param link to craft
   * @throws ConfigException   on configuration failure
   * @throws BusinessException on failure
   */
  protected void doWork(Link link) throws Exception {
    Basis basis = basisFactory.createBasis(link);
    craftFactory.foundation(basis).doWork();
    craftFactory.structure(basis).doWork();
    craftFactory.voice(basis).doWork();
    basis.sendReport();
  }

}
