// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainState;
import io.xj.core.model.segment.Segment;
import io.xj.core.util.TimestampUTC;
import io.xj.core.work.WorkManager;
import io.xj.worker.job.ChainFabricateJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Objects;

public class ChainFabricateJobImpl implements ChainFabricateJob {
  private static final Logger log = LoggerFactory.getLogger(ChainFabricateJobImpl.class);
  private final BigInteger entityId;
  private final ChainDAO chainDAO;
  private final SegmentDAO segmentDAO;
  private final WorkManager workManager;

  @Inject
  public ChainFabricateJobImpl(
    @Assisted("entityId") BigInteger entityId,
    ChainDAO chainDAO,
    SegmentDAO segmentDAO,
    WorkManager workManager
  ) {
    this.entityId = entityId;
    this.chainDAO = chainDAO;
    this.segmentDAO = segmentDAO;
    this.workManager = workManager;
  }

  @Override
  public void run() {

    try {
      Chain chain = chainDAO.readOne(Access.internal(), entityId);
      if (Objects.nonNull(chain)) {
        doWork(chain);
      } else {
        log.warn("Cannot work on non-existent Chain #{}", entityId);
        cancelFabrication();
      }

    } catch (Exception e) {
      log.error("{}:{} failed ({})",
        getClass().getSimpleName(), Thread.currentThread().getName(), e);
    }
  }

  /**
   Build a Segment in the Chain, or Complete the Chain

   @param chain to build on
   @throws Exception on failure
   */
  private void doWork(Chain chain) throws Exception {
    if (ChainState.Fabricate != chain.getState()) {
      workManager.stopChainFabrication(entityId);
      throw new BusinessException(String.format("Cannot fabricate Chain id:%s in non-Fabricate (%s) state!",
        chain.getId(), chain.getState()));
    }

    int bufferSeconds = Config.workBufferSeconds();
    Segment segmentToCreate = chainDAO.buildNextSegmentOrComplete(
      Access.internal(),
      chain,
      TimestampUTC.nowPlusSeconds(bufferSeconds),
      TimestampUTC.nowMinusSeconds(bufferSeconds));

    if (Objects.nonNull(segmentToCreate)) {
      createSegmentAndJobs(segmentToCreate);
    }
  }

  /**
   Create Segment, and jobs to craft & dub it

   @param segmentToCreate to create
   @throws BusinessException on failure
   */
  private void createSegmentAndJobs(Segment segmentToCreate) throws Exception {
    segmentToCreate.validate();
    Segment createdSegment = segmentDAO.create(Access.internal(), segmentToCreate);
    log.info("Created segment, id:{}, chainId:{}, offset:{}", createdSegment.getId(), createdSegment.getChainId(), createdSegment.getOffset());

    workManager.scheduleSegmentFabricate(Config.workBufferFabricateDelaySeconds(), createdSegment.getId());
  }

  /**
   Cancel chain fabrication
   */
  private void cancelFabrication() {
    workManager.stopChainFabrication(entityId);
    log.info("Canceled fabrication of Chain #{}", entityId);
  }

}
