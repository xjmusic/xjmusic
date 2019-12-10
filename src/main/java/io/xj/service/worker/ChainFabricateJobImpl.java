// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.worker;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.typesafe.config.Config;
import io.xj.core.access.Access;
import io.xj.core.dao.ChainDAO;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.Chain;
import io.xj.core.model.ChainState;
import io.xj.core.model.Segment;
import io.xj.core.work.WorkManager;
import io.xj.craft.exception.CraftException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

class ChainFabricateJobImpl implements ChainFabricateJob {
  private static final Logger log = LoggerFactory.getLogger(ChainFabricateJobImpl.class);
  private final UUID entityId;
  private final ChainDAO chainDAO;
  private final SegmentDAO segmentDAO;
  private final WorkManager workManager;
  private final Access access = Access.internal();
  private final int workBufferSeconds;
  private final int workBufferFabricateDelaySeconds;

  @Inject
  public ChainFabricateJobImpl(
    @Assisted("entityId") UUID entityId,
    ChainDAO chainDAO,
    SegmentDAO segmentDAO,
    WorkManager workManager,
    Config config
  ) {
    this.entityId = entityId;
    this.chainDAO = chainDAO;
    this.segmentDAO = segmentDAO;
    this.workManager = workManager;

    workBufferSeconds = config.getInt("work.bufferSeconds");
    workBufferFabricateDelaySeconds = config.getInt("work.bufferFabricateDelaySeconds");
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

    Optional<Segment> segmentToCreate = chainDAO.buildNextSegmentOrComplete(
      access,
      chain,
      Instant.now().plusSeconds(workBufferSeconds),
      Instant.now().minusSeconds(workBufferSeconds));

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

    workManager.scheduleSegmentFabricate(workBufferFabricateDelaySeconds, createdSegment.getId());
  }

  /**
   Cancel chain fabrication
   */
  private void cancelFabrication() {
    workManager.stopChainFabrication(entityId);
    log.info("Canceled fabrication create Chain #{}", entityId);
  }

}
