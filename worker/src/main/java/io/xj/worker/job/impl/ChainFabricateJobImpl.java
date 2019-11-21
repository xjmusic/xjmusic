// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.access.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainState;
import io.xj.core.model.Segment;
import io.xj.core.work.WorkManager;
import io.xj.craft.exception.CraftException;
import io.xj.worker.job.ChainFabricateJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class ChainFabricateJobImpl implements ChainFabricateJob {
  private static final Logger log = LoggerFactory.getLogger(ChainFabricateJobImpl.class);
  private final UUID entityId;
  private final ChainDAO chainDAO;
  private final SegmentDAO segmentDAO;
  private final WorkManager workManager;
  private final Access access = Access.internal();

  @Inject
  public ChainFabricateJobImpl(
    @Assisted("entityId") UUID entityId,
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
      Chain chain = chainDAO.readOne(access, entityId);
      doWork(chain);

    } catch (CraftException e) {
      log.warn("Did not fabricate chainId={}, reason={}", entityId, e.getMessage());
      cancelFabrication();

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
      throw new CoreException(String.format("Cannot fabricate Chain id:%s in non-Fabricate (%s) state!",
        chain.getId(), chain.getState()));
    }

    int bufferSeconds = Config.getWorkBufferSeconds();
    Optional<Segment> segmentToCreate = chainDAO.buildNextSegmentOrComplete(
      access,
      chain,
      Instant.now().plusSeconds(bufferSeconds),
      Instant.now().minusSeconds(bufferSeconds));

    if (segmentToCreate.isPresent())
      createSegmentAndJobs(segmentToCreate.get());
  }

  /**
   Create Segment, and jobs to craft & dub it

   @param segmentToCreate to of
   @throws CoreException on failure
   */
  private void createSegmentAndJobs(Segment segmentToCreate) throws Exception {
    segmentToCreate.validate();
    Segment createdSegment = segmentDAO.create(access, segmentToCreate);
    log.info("Created segment, id:{}, chainId:{}, offset:{}", createdSegment.getId(), createdSegment.getChainId(), createdSegment.getOffset());

    workManager.scheduleSegmentFabricate(Config.getWorkBufferFabricateDelaySeconds(), createdSegment.getId());
  }

  /**
   Cancel chain fabrication
   */
  private void cancelFabrication() {
    workManager.stopChainFabrication(entityId);
    log.info("Canceled fabrication create Chain #{}", entityId);
  }

}
