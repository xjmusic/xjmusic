// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.worker;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.dao.ChainDAO;
import io.xj.lib.core.dao.SegmentDAO;
import io.xj.lib.core.model.Segment;
import io.xj.lib.core.util.CSV;
import io.xj.lib.core.work.WorkManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

class ChainEraseJobImpl implements ChainEraseJob {
  private static final Logger log = LoggerFactory.getLogger(ChainEraseJobImpl.class);
  private final UUID entityId;
  private final ChainDAO chainDAO;
  private final SegmentDAO segmentDAO;
  private final WorkManager workManager;
  private final Access access = Access.internal();

  @Inject
  public ChainEraseJobImpl(
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
      doWork();

    } catch (Exception e) {
      log.error("{}:{} failed ({})",
        getClass().getSimpleName(), Thread.currentThread().getName(), e);
    }
  }

  /**
   Do Chain Erase Work
   If the Chain is empty, Eraseworker deletes the chain

   @throws Exception on failure
   */
  private void doWork() throws Exception {
    Collection<Segment> segments = segmentDAO.readMany(access, ImmutableList.of(entityId));
    if (segments.isEmpty())
      try {
        log.info("Found ZERO segments in chainId={}; attempting to delete...", entityId);
        chainDAO.destroy(access, entityId);
        log.info("Did destroy chainId={} OK", entityId);

      } catch (Exception e) {
        log.warn("Did not delete chainId={}, reason={}", entityId, e.getMessage());
        workManager.stopChainErase(entityId);
      }
    else {
      List<String> segmentIds = Lists.newArrayList();
      for (Segment segment : segments) {
        segmentIds.add(segment.getId().toString());
      }
      log.info("Found {} segments in chainId={}; segmentsIds={}; attempting to erase...", segments.size(), entityId, CSV.join(segmentIds));
      eraseSegments(segments);
    }
  }

  /**
   Erase many segments
   Eraseworker iterates on each segment in the chain, reading in batches of a limited size

   @param segments to erase
   @throws Exception on failure
   */
  private void eraseSegments(Iterable<Segment> segments) throws Exception {
    for (Segment segment : segments)
      eraseSegment(segment);
  }

  /**
   Erase a segment
   Eraseworker removes all child entities for the Segment
   Eraseworker deletes all S3 objects for the Segment
   If the Segment is empty and the S3 object is confirmed deleted, Eraseworker deletes the Segment

   @param segment to erase
   @throws Exception on failure
   */
  private void eraseSegment(Segment segment) throws Exception {
    segmentDAO.destroy(access, segment.getId());
    log.info("Erased Segment #{}, destroyed child entities, and deleted s3 object {}", segment.getId(), segment.getWaveformKey());
  }

}
