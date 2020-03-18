// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.worker;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.typesafe.config.Config;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.dao.SegmentDAO;
import io.xj.lib.core.dao.SegmentMessageDAO;
import io.xj.lib.core.entity.MessageType;
import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.fabricator.Fabricator;
import io.xj.lib.core.fabricator.FabricatorFactory;
import io.xj.lib.core.model.Segment;
import io.xj.lib.core.model.SegmentMessage;
import io.xj.lib.core.model.SegmentState;
import io.xj.lib.core.work.WorkManager;
import io.xj.lib.craft.CraftFactory;
import io.xj.lib.craft.exception.CraftException;
import io.xj.lib.dub.DubFactory;
import io.xj.lib.dub.DubException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

class SegmentFabricateJobImpl implements SegmentFabricateJob {
  private static final Logger log = LoggerFactory.getLogger(SegmentFabricateJobImpl.class);

  private final SegmentDAO segmentDAO;
  private final UUID entityId;
  private final FabricatorFactory fabricatorFactory;
  private final CraftFactory craftFactory;
  private final DubFactory dubFactory;
  private final WorkManager workManager;
  private final Access access = Access.internal();
  private Fabricator fabricator;
  private Segment segment;
  private SegmentMessageDAO segmentMessageDAO;
  private int segmentRequeueSeconds;

  @Inject
  public SegmentFabricateJobImpl(
    @Assisted("entityId") UUID entityId,
    CraftFactory craftFactory,
    FabricatorFactory fabricatorFactory,
    SegmentDAO segmentDAO,
    DubFactory dubFactory,
    WorkManager workManager,
    SegmentMessageDAO segmentMessageDAO,
    Config config
  ) {
    this.entityId = entityId;
    this.craftFactory = craftFactory;
    this.fabricatorFactory = fabricatorFactory;
    this.segmentDAO = segmentDAO;
    this.dubFactory = dubFactory;
    this.workManager = workManager;
    this.segmentMessageDAO = segmentMessageDAO;

    segmentRequeueSeconds = config.getInt("segment.requeueSeconds");
  }

  /**
   Do the segment Job
   */
  @Override
  public void run() {
    try {
      log.info("[segId={}] will read Segment for fabrication", entityId);
      segment = segmentDAO.readOne(access, entityId);
    } catch (CoreException e) {
      didFailWhile("retrieving", e);
      return;
    }

    try {
      log.info("[segId={}] will prepare fabricator", entityId);
      fabricator = fabricatorFactory.fabricate(Access.internal(), segment);
    } catch (CoreException e) {
      didFailWhile("creating fabricator", e);
      return;
    }

    try {
      log.info("[segId={}] will do craft work", entityId);
      doCraftWork();
    } catch (Exception e) {
      didFailWhile("doing Craft work", e);
      revertAndRequeue();
      return;
    }

    try {
      doDubWork();
    } catch (Exception e) {
      didFailWhile("doing Dub work", e);
      return;
    }

    try {
      finishWork();
    } catch (Exception e) {
      didFailWhile("finishing work", e);
    }
  }

  /**
   [#158610991] Engineer wants a Segment to be reverted, and re-queued for Craft, in the event that such a Segment has just failed its Craft process, in order to ensure Chain fabrication fault tolerance
   [#166132897] SegmentBasis POJO via gson only (no JSONObject), so reverting simply means resetting the basis
   */
  private void revertAndRequeue() {
    try {
      updateSegmentState(fabricator.getSegment().getState(), SegmentState.Planned);
      segmentDAO.revert(access, fabricator.getSegment().getId());
      workManager.scheduleSegmentFabricate(segmentRequeueSeconds, fabricator.getSegment().getId());
    } catch (CoreException e) {
      didFailWhile("reverting and re-queueing segment", e);
    }
  }

  /**
   Finish work on Segment
   */
  private void finishWork() throws CoreException {
    updateSegmentState(SegmentState.Dubbing, SegmentState.Dubbed);
    log.info("[segId={}] Worked for {} seconds", entityId, fabricator.getElapsedSeconds());
  }

  /**
   Craft a Segment, or fail

   @throws CoreException on configuration failure
   @throws CoreException on failure
   */
  private void doCraftWork() throws CoreException, CraftException {
    updateSegmentState(SegmentState.Planned, SegmentState.Crafting);
    craftFactory.macroMain(fabricator).doWork();
    craftFactory.rhythm(fabricator).doWork();
    craftFactory.harmonicDetail(fabricator).doWork();
  }

  /**
   Dub a Segment, or fail

   @throws CoreException if mis-configured
   @throws CoreException on failure
   */
  protected void doDubWork() throws CoreException, CraftException, DubException {
    updateSegmentState(SegmentState.Crafting, SegmentState.Dubbing);
    dubFactory.master(fabricator).doWork();
    dubFactory.ship(fabricator).doWork();
  }

  /**
   Log and of segment message of error that job failed while (message)

   @param message phrased like "Doing work"
   @param e       exception (optional)
   */
  private void didFailWhile(String message, Exception e) {
    createSegmentMessage(MessageType.Error, String.format("Failed while %s for Segment #%s:\n\n%s", message, entityId, e.getMessage()));
    log.error("[segId={}] Failed while {} due to {}", entityId, message, e.getMessage());
  }

  /**
   Create a segment message

   @param type of message
   @param body of message
   */
  protected void createSegmentMessage(MessageType type, String body) {
    try {
      segmentMessageDAO.create(access, SegmentMessage.create(fabricator.getSegment(), type, body));
    } catch (CoreException e) {
      log.error("[segId={}] Could not create SegmentMessage, reason={}", entityId, e.getMessage());
    }
  }

  /**
   Update Segment to Working state

   @throws CoreException on failure
   */
  private void updateSegmentState(SegmentState fromState, SegmentState toState) throws CoreException {
    if (fromState != segment.getState()) {
      log.error("[segId={}] {} requires Segment must be in {} state.", entityId, toState, fromState);
      return;
    }
    segmentDAO.updateState(access, segment.getId(), toState);
    segment.setStateEnum(toState);
    log.info("[segId={}] Segment transitioned to state {} OK", entityId, toState);
  }

}
