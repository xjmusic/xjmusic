// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.typesafe.config.Config;
import io.xj.lib.rest_api.RestApiException;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.dao.SegmentDAO;
import io.xj.service.hub.dao.SegmentMessageDAO;
import io.xj.service.hub.entity.MessageType;
import io.xj.service.hub.model.Segment;
import io.xj.service.hub.model.SegmentMessage;
import io.xj.service.hub.model.SegmentState;
import io.xj.service.hub.work.WorkManager;
import io.xj.service.nexus.craft.CraftFactory;
import io.xj.service.nexus.craft.exception.CraftException;
import io.xj.service.nexus.dub.DubException;
import io.xj.service.nexus.dub.DubFactory;
import io.xj.service.nexus.fabricator.Fabricator;
import io.xj.service.nexus.fabricator.FabricatorFactory;
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
    } catch (HubException e) {
      didFailWhile("retrieving", e);
      return;
    }

    try {
      log.info("[segId={}] will prepare fabricator", entityId);
      fabricator = fabricatorFactory.fabricate(Access.internal(), segment);
    } catch (HubException e) {
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
   */
  private void revertAndRequeue() {
    try {
      updateSegmentState(fabricator.getSegment().getState(), SegmentState.Planned);
      segmentDAO.revert(access, fabricator.getSegment().getId());
      workManager.scheduleSegmentFabricate(segmentRequeueSeconds, fabricator.getSegment().getId());
    } catch (HubException | RestApiException | ValueException e) {
      didFailWhile("reverting and re-queueing segment", e);
    }
  }

  /**
   Finish work on Segment
   */
  private void finishWork() throws HubException, RestApiException, ValueException {
    updateSegmentState(SegmentState.Dubbing, SegmentState.Dubbed);
    log.info("[segId={}] Worked for {} seconds", entityId, fabricator.getElapsedSeconds());
  }

  /**
   Craft a Segment, or fail

   @throws HubException on configuration failure
   @throws HubException on failure
   */
  private void doCraftWork() throws HubException, CraftException, RestApiException, ValueException {
    updateSegmentState(SegmentState.Planned, SegmentState.Crafting);
    craftFactory.macroMain(fabricator).doWork();
    craftFactory.rhythm(fabricator).doWork();
    craftFactory.harmonicDetail(fabricator).doWork();
  }

  /**
   Dub a Segment, or fail

   @throws HubException if mis-configured
   @throws HubException on failure
   */
  protected void doDubWork() throws HubException, CraftException, DubException, RestApiException, ValueException {
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
    createSegmentErrorMessage(String.format("Failed while %s for Segment #%s:\n\n%s", message, entityId, e.getMessage()));
    log.error("[segId={}] Failed while {} due to {}", entityId, message, e.getMessage());
  }

  /**
   Create a segment error message

   @param body of message
   */
  protected void createSegmentErrorMessage(String body) {
    try {
      segmentMessageDAO.create(access, SegmentMessage.create(fabricator.getSegment(), MessageType.Error, body));
    } catch (HubException | RestApiException | ValueException e) {
      log.error("[segId={}] Could not create SegmentMessage, reason={}", entityId, e.getMessage());
    }
  }

  /**
   Update Segment to Working state

   @throws HubException on failure
   */
  private void updateSegmentState(SegmentState fromState, SegmentState toState) throws HubException, RestApiException, ValueException {
    if (fromState != segment.getState()) {
      log.error("[segId={}] {} requires Segment must be in {} state.", entityId, toState, fromState);
      return;
    }
    segmentDAO.updateState(access, segment.getId(), toState);
    segment.setStateEnum(toState);
    log.info("[segId={}] Segment transitioned to state {} OK", entityId, toState);
  }

}
