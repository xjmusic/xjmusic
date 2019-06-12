// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.core.access.impl.Access;
import io.xj.core.config.Config;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.fabricator.Fabricator;
import io.xj.core.fabricator.FabricatorFactory;
import io.xj.core.model.message.MessageType;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment.sub.SegmentMessage;
import io.xj.core.work.WorkManager;
import io.xj.craft.CraftFactory;
import io.xj.craft.exception.CraftException;
import io.xj.dub.DubFactory;
import io.xj.dub.exception.DubException;
import io.xj.worker.job.SegmentFabricateJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

public class SegmentFabricateJobImpl implements SegmentFabricateJob {
  private static final Logger log = LoggerFactory.getLogger(SegmentFabricateJobImpl.class);

  private final SegmentDAO segmentDAO;
  private final BigInteger entityId;
  private final FabricatorFactory fabricatorFactory;
  private final CraftFactory craftFactory;
  private final DubFactory dubFactory;
  private final WorkManager workManager;
  private final Access access = Access.internal();
  private Fabricator fabricator;
  private Segment segment;

  @Inject
  public SegmentFabricateJobImpl(
    @Assisted("entityId") BigInteger entityId,
    CraftFactory craftFactory,
    FabricatorFactory fabricatorFactory,
    SegmentDAO segmentDAO,
    DubFactory dubFactory,
    WorkManager workManager
  ) {
    this.entityId = entityId;
    this.craftFactory = craftFactory;
    this.fabricatorFactory = fabricatorFactory;
    this.segmentDAO = segmentDAO;
    this.dubFactory = dubFactory;
    this.workManager = workManager;
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
      fabricator = fabricatorFactory.fabricate(segment);
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
      workManager.scheduleSegmentFabricate(Config.getSegmentRequeueSeconds(), fabricator.getSegment().getId());
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
   Log and create segment message of error that job failed while (message)

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
      fabricator.add(new SegmentMessage()
        .setBody(body)
        .setType(type.toString()));
      segmentDAO.update(access, fabricator.getSegment().getId(), fabricator.getSegment());
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
