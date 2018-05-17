// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.worker.job.impl;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.SegmentDAO;
import io.xj.core.dao.SegmentMessageDAO;
import io.xj.core.exception.BusinessException;
import io.xj.core.exception.ConfigException;
import io.xj.core.model.segment.Segment;
import io.xj.core.model.segment.SegmentState;
import io.xj.core.model.segment_message.SegmentMessage;
import io.xj.core.model.message.MessageType;
import io.xj.core.util.Text;
import io.xj.craft.basis.Basis;
import io.xj.craft.basis.BasisFactory;
import io.xj.craft.CraftFactory;
import io.xj.dub.DubFactory;
import io.xj.worker.job.SegmentFabricateJob;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Objects;

public class SegmentFabricateJobImpl implements SegmentFabricateJob {
  private static final Logger log = LoggerFactory.getLogger(SegmentFabricateJobImpl.class);

  private final SegmentDAO segmentDAO;
  private final SegmentMessageDAO segmentMessageDAO;
  private final BigInteger entityId;
  private final BasisFactory basisFactory;
  private final CraftFactory craftFactory;
  private final DubFactory dubFactory;
  private Basis basis;
  private Segment segment;

  @Inject
  public SegmentFabricateJobImpl(
    @Assisted("entityId") BigInteger entityId,
    CraftFactory craftFactory,
    BasisFactory basisFactory,
    SegmentDAO segmentDAO,
    SegmentMessageDAO segmentMessageDAO,
    DubFactory dubFactory
  ) {
    this.entityId = entityId;
    this.craftFactory = craftFactory;
    this.basisFactory = basisFactory;
    this.segmentDAO = segmentDAO;
    this.segmentMessageDAO = segmentMessageDAO;
    this.dubFactory = dubFactory;
  }

  /**
   Do the segment Job
   */
  @Override
  public void run() {
    try {
      segment = segmentDAO.readOne(Access.internal(), entityId);
    } catch (Exception e) {
      didFailWhile("retrieving", e);
      return;
    }

    if (Objects.isNull(segment)) {
      didFailWhile("retrieving null");
      return;
    }

    try {
      basis = basisFactory.createBasis(segment);
    } catch (ConfigException e) {
      didFailWhile("creating basis", e);
      return;
    }

    try {
      doCraftWork();
    } catch (Exception e) {
      didFailWhile("doing Craft work", e);
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
   Finish work on Segment
   */
  private void finishWork() throws Exception {
    updateSegmentState(SegmentState.Dubbing, SegmentState.Dubbed);
    basis.sendReport();
  }

  /**
   Craft a Segment, or fail

   @throws ConfigException   on configuration failure
   @throws BusinessException on failure
   */
  private void doCraftWork() throws Exception {
    updateSegmentState(SegmentState.Planned, SegmentState.Crafting);
    craftFactory.macroMain(basis).doWork();
    craftFactory.rhythm(basis).doWork();
    craftFactory.harmonicDetail(basis).doWork();
  }

  /**
   Dub a Segment, or fail

   @throws ConfigException   if mis-configured
   @throws BusinessException on failure
   */
  protected void doDubWork() throws Exception {
    updateSegmentState(SegmentState.Crafting, SegmentState.Dubbing);
    dubFactory.master(basis).doWork();
    dubFactory.ship(basis).doWork();
  }

  /**
   Log and create segment message of error that job failed while (message)

   @param message phrased like "Doing work"
   @param e       exception (optional)
   */
  private void didFailWhile(String message, Exception e) {
    createSegmentMessage(MessageType.Error, String.format("Failed while %s for Segment #%s:\n\n%s\n%s", message, entityId, e.getMessage(), Text.formatStackTrace(e)));
    log.error("Failed while {} for Segment #{}", message, entityId, e);
  }

  /**
   Log and create segment message of error that job failed while (message)

   @param message phrased like "Doing work"
   */
  private void didFailWhile(String message) {
    createSegmentMessage(MessageType.Error, String.format("Failed while %s for Segment #%s", message, entityId));
    log.error("Failed while {} for Segment #{}", message, entityId);
  }

  /**
   Create a segment message

   @param type of message
   @param body of message
   */
  protected void createSegmentMessage(MessageType type, String body) {
    try {
      segmentMessageDAO.create(Access.internal(),
        new SegmentMessage()
          .setSegmentId(entityId)
          .setBody(body)
          .setType(type.toString()));
    } catch (Exception e) {
      log.error("Could not create Segment Message", e);
    }
  }

  /**
   Update Segment to Working state

   @throws Exception on failure
   */
  private void updateSegmentState(SegmentState fromState, SegmentState toState) throws Exception {
    if (fromState != segment.getState()) {
      log.error("{} requires Segment must be in {} state.", toState, fromState);
      return;
    }
    segmentDAO.updateState(Access.internal(), segment.getId(), toState);
    segment.setStateEnum(toState);
    log.info("{} Segment OK (id={})", toState, entityId);
  }

}
