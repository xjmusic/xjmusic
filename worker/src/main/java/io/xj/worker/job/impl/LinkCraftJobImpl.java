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
import io.xj.craft.CraftFactory;
import io.xj.worker.job.LinkCraftJob;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import java.math.BigInteger;

public class LinkCraftJobImpl extends LinkJob implements LinkCraftJob {
  //  private static final Logger log = LoggerFactory.getLogger(LinkCraftJob.class);
  private final CraftFactory craftFactory;

  @Inject
  public LinkCraftJobImpl(
    @Assisted("entityId") BigInteger entityId,
    CraftFactory craftFactory,
    BasisFactory basisFactory,
    LinkDAO linkDAO,
    LinkMessageDAO linkMessageDAO
  ) {
    this.entityId = entityId;
    this.craftFactory = craftFactory;
    this.basisFactory = basisFactory;
    this.linkDAO = linkDAO;
    this.linkMessageDAO = linkMessageDAO;

    name = "Crafting";
    fromState = LinkState.Planned;
    workingState = LinkState.Crafting;
    toState = LinkState.Crafted;
  }

  /**
   Craft a Link, or fail

   @param link to craft
   @throws ConfigException   on configuration failure
   @throws BusinessException on failure
   */
  protected void doWork(Link link) throws Exception {
    Basis basis = basisFactory.createBasis(link);
    craftFactory.foundation(basis).doWork();
    craftFactory.structure(basis).doWork();
    craftFactory.voice(basis).doWork();
    basis.sendReport();
  }

}
