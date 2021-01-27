package io.xj.service.nexus.work;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import datadog.trace.api.Trace;
import io.xj.Segment;
import io.xj.SegmentMessage;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.NexusException;
import io.xj.service.nexus.craft.CraftException;
import io.xj.service.nexus.craft.CraftFactory;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import io.xj.service.nexus.dub.DubException;
import io.xj.service.nexus.dub.DubFactory;
import io.xj.service.nexus.fabricator.FabricationException;
import io.xj.service.nexus.fabricator.Fabricator;
import io.xj.service.nexus.fabricator.FabricatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 Fabricator Worker implementation
 */
public class FabricatorWorkerImpl extends WorkerImpl implements FabricatorWorker {
  private static final String NAME = "Fabricator";
  private static final Logger log = LoggerFactory.getLogger(FabricatorWorker.class);
  private final SegmentDAO segmentDAO;
  private final String segmentId;
  private final FabricatorFactory fabricatorFactory;
  private final CraftFactory craftFactory;
  private final DubFactory dubFactory;
  private final HubClientAccess access = HubClientAccess.internal();
  private Fabricator fabricator;
  private Segment segment;

  @Inject
  public FabricatorWorkerImpl(
    @Assisted String segmentId,
    CraftFactory craftFactory,
    FabricatorFactory fabricatorFactory,
    SegmentDAO segmentDAO,
    DubFactory dubFactory
  ) {
    this.segmentId = segmentId;
    this.craftFactory = craftFactory;
    this.fabricatorFactory = fabricatorFactory;
    this.segmentDAO = segmentDAO;
    this.dubFactory = dubFactory;

    log.info("Instantiated OK");
  }

  @Override
  protected String getName() {
    return NAME;
  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "doWork")
  protected void doWork() {
    try {
      log.info("[segId={}] will read Segment for fabrication", segmentId);
      segment = segmentDAO.readOne(access, segmentId);
    } catch (DAOFatalException | DAOExistenceException | DAOPrivilegeException e) {
      didFailWhile("retrieving", e);
      return;
    }

    try {
      log.info("[segId={}] will prepare fabricator", segmentId);
      fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment);
    } catch (FabricationException e) {
      didFailWhile("creating fabricator", e);
      return;
    }

    try {
      log.info("[segId={}] will do craft work", segmentId);
      doCraftWork();
    } catch (Exception e) {
      didFailWhile("doing Craft work", e);
      revert();
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
   [#171553408] Remove all Queue mechanics in favor of a cycle happening in Main class for as long as the application is alive, that does nothing but search for active chains, search for segments that need work, and work on them. Zero need for a work queue-- that's what the Chain-Segment state machine is!
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "revert")
  private void revert() {
    try {
      updateSegmentState(fabricator.getSegment().getState(), Segment.State.Planned);
      segmentDAO.revert(access, segment.getId());
    } catch (DAOFatalException | DAOPrivilegeException | DAOValidationException | DAOExistenceException | FabricationException e) {
      didFailWhile("reverting and re-queueing segment", e);
    }
  }

  /**
   Finish work on Segment
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "finishWork")
  private void finishWork() throws FabricationException {
    updateSegmentState(Segment.State.Dubbing, Segment.State.Dubbed);
    log.info("[segId={}] Worked for {} seconds", segmentId, fabricator.getElapsedSeconds());
  }

  /**
   Craft a Segment, or fail

   @throws NexusException on configuration failure
   @throws CraftException on craft failure
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "doCraftWork")
  private void doCraftWork() throws NexusException, CraftException, FabricationException {
    updateSegmentState(Segment.State.Planned, Segment.State.Crafting);
    craftFactory.macroMain(fabricator).doWork();
    craftFactory.rhythm(fabricator).doWork();
    craftFactory.detail(fabricator).doWork();
  }

  /**
   Dub a Segment, or fail

   @throws CraftException on craft failure
   @throws DubException   on dub failure
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "doDubWork")
  protected void doDubWork() throws CraftException, DubException, FabricationException {
    updateSegmentState(Segment.State.Crafting, Segment.State.Dubbing);
    dubFactory.master(fabricator).doWork();
    dubFactory.ship(fabricator).doWork();
  }

  /**
   Log and of segment message of error that job failed while (message)

   @param message phrased like "Doing work"
   @param e       exception (optional)
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "didFailWhile")
  private void didFailWhile(String message, Exception e) {
    createSegmentErrorMessage(String.format("Failed while %s for Segment #%s:\n\n%s", message, segmentId, e.getMessage()));
    log.error("[segId={}] Failed while {}", segmentId, message, e);
  }

  /**
   Create a segment error message

   @param body of message
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "createSegmentErrorMessage")
  protected void createSegmentErrorMessage(String body) {
    try {
      segmentDAO.create(access, SegmentMessage.newBuilder()
        .setId(UUID.randomUUID().toString())
        .setSegmentId(segment.getId())
        .setType(SegmentMessage.Type.Error)
        .setBody(body)
        .build());

    } catch (DAOValidationException | DAOPrivilegeException | DAOExistenceException | DAOFatalException e) {
      log.error("[segId={}] Could not create SegmentMessage, reason={}", segmentId, e.getMessage());
    }
  }

  /**
   Update Segment to Working state

   @param fromState of existing segment
   @param toState   of new segment
   @throws FabricationException if record is invalid
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "updateSegmentState")
  private void updateSegmentState(Segment.State fromState, Segment.State toState) throws FabricationException {
    if (fromState != segment.getState())
      throw new FabricationException(String.format("Segment[%s] %s requires Segment must be in %s state.", segmentId, toState, fromState));
    fabricator.updateSegment(fabricator.getSegment().toBuilder().setState(toState).build());
    segment = fabricator.getSegment();
    log.info("[segId={}] Segment transitioned to state {} OK", segmentId, toState);
  }

}
