// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.LinkDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.link.Link;
import io.xj.core.timestamp.TimestampUTC;
import io.xj.core.work.WorkManager;
import io.xj.worker.job.ChainFabricateJob;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Objects;

public class ChainFabricateJobImpl implements ChainFabricateJob {
  private static final Logger log = LoggerFactory.getLogger(ChainFabricateJobImpl.class);
  private final BigInteger entityId;
  private final ChainDAO chainDAO;
  private final LinkDAO linkDAO;
  private final WorkManager workManager;

  @Inject
  public ChainFabricateJobImpl(
    @Assisted("entityId") BigInteger entityId,
    ChainDAO chainDAO,
    LinkDAO linkDAO,
    WorkManager workManager
  ) {
    this.entityId = entityId;
    this.chainDAO = chainDAO;
    this.linkDAO = linkDAO;
    this.workManager = workManager;
  }

  @Override
  public void run() {

    try {
      Chain chain = chainDAO.readOne(Access.internal(), entityId);
      if (Objects.nonNull(chain)) {
        doWork(chain);
      } else {
        cancelFabrication();
      }

    } catch (Exception e) {
      log.error("{}:{} failed ({})",
        getClass().getSimpleName(), Thread.currentThread().getName(), e);
    }
  }

  /**
   Build a Link in the Chain, or Complete the Chain

   @param chain to build on
   @throws Exception on failure
   */
  private void doWork(Chain chain) throws Exception {
    if (!Objects.equals(ChainState.Fabricate, chain.getState())) {
      workManager.stopChainFabrication(entityId);
      throw new BusinessException(String.format("Cannot fabricate Chain id:%s in non-Fabricate (%s) state!",
        chain.getId(), chain.getState()));
    }

    int bufferSeconds = Config.workBufferSeconds();
    Link linkToCreate = chainDAO.buildNextLinkOrComplete(
      Access.internal(),
      chain,
      TimestampUTC.nowPlusSeconds(bufferSeconds),
      TimestampUTC.nowMinusSeconds(bufferSeconds));

    if (Objects.nonNull(linkToCreate)) {
      createLinkAndJobs(linkToCreate);
    }
  }

  /**
   Create Link, and jobs to craft & dub it

   @param linkToCreate to create
   @throws BusinessException on failure
   */
  private void createLinkAndJobs(Link linkToCreate) throws Exception {
    linkToCreate.validate();
    Link createdLink = linkDAO.create(Access.internal(), linkToCreate);
    log.info("Created link, id:{}, chainId:{}, offset:{}", createdLink.getId(), createdLink.getChainId(), createdLink.getOffset());

    workManager.scheduleLinkFabricate(Config.workBufferFabricateDelaySeconds(), createdLink.getId());
  }

  /**
   Cancel chain fabrication
   */
  private void cancelFabrication() {
    workManager.stopChainFabrication(entityId);
  }

}
