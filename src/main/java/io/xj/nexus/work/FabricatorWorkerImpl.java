package io.xj.nexus.work;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import datadog.trace.api.Trace;
import io.xj.Chain;
import io.xj.Segment;
import io.xj.SegmentMessage;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.util.Text;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.fabricator.Fabricator;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.hub_client.client.HubClientAccess;
import io.xj.nexus.NexusException;
import io.xj.nexus.dao.ChainDAO;
import io.xj.nexus.dao.SegmentDAO;
import io.xj.nexus.dao.exception.DAOExistenceException;
import io.xj.nexus.dao.exception.DAOFatalException;
import io.xj.nexus.dao.exception.DAOPrivilegeException;
import io.xj.nexus.dao.exception.DAOValidationException;
import io.xj.nexus.dub.DubFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.UUID;

/**
 Fabricator Worker implementation
 */
public class FabricatorWorkerImpl extends WorkerImpl implements FabricatorWorker {
  private static final String NAME = "Fabricator";
  private static final String UNKNOWN = "NaN";
  private static final Logger log = LoggerFactory.getLogger(FabricatorWorker.class);
  private final SegmentDAO segmentDAO;
  private final ChainDAO chainDAO;
  private final String segmentId;
  private final FabricatorFactory fabricatorFactory;
  private final CraftFactory craftFactory;
  private final DubFactory dubFactory;
  private final HubClientAccess access = HubClientAccess.internal();
  private final NotificationProvider notification;
  private Fabricator fabricator;
  private Segment segment;
  private Chain chain;

  @Inject
  public FabricatorWorkerImpl(
    @Assisted String segmentId,
    ChainDAO chainDAO,
    CraftFactory craftFactory,
    DubFactory dubFactory,
    FabricatorFactory fabricatorFactory,
    NotificationProvider notification,
    SegmentDAO segmentDAO
  ) {
    super(notification);
    this.segmentId = segmentId;
    this.craftFactory = craftFactory;
    this.fabricatorFactory = fabricatorFactory;
    this.segmentDAO = segmentDAO;
    this.chainDAO = chainDAO;
    this.dubFactory = dubFactory;
    this.notification = notification;

    log.debug("Instantiated OK");
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
      log.debug("[segId={}] will read Segment for fabrication", segmentId);
      segment = segmentDAO.readOne(access, segmentId);
      chain = chainDAO.readOne(access, segment.getChainId());
    } catch (DAOFatalException | DAOExistenceException | DAOPrivilegeException e) {
      didFailWhile("retrieving", e);
      return;
    }

    try {
      log.debug("[segId={}] will prepare fabricator", segmentId);
      fabricator = fabricatorFactory.fabricate(HubClientAccess.internal(), segment);
    } catch (NexusException e) {
      didFailWhile("creating fabricator", e);
      return;
    }

    try {
      log.debug("[segId={}] will do craft work", segmentId);
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
    } catch (DAOFatalException | DAOPrivilegeException | DAOValidationException | DAOExistenceException | NexusException e) {
      didFailWhile("reverting and re-queueing segment", e);
    }
  }

  /**
   Finish work on Segment
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "finishWork")
  private void finishWork() throws NexusException {
    updateSegmentState(Segment.State.Dubbing, Segment.State.Dubbed);
    log.debug("[segId={}] Worked for {} seconds", segmentId, fabricator.getElapsedSeconds());
  }

  /**
   Craft a Segment, or fail

   @throws NexusException on configuration failure
   @throws NexusException on craft failure
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "doCraftWork")
  private void doCraftWork() throws NexusException {
    updateSegmentState(Segment.State.Planned, Segment.State.Crafting);
    craftFactory.macroMain(fabricator).doWork();
    craftFactory.rhythm(fabricator).doWork();
    craftFactory.detail(fabricator).doWork();
  }

  /**
   Dub a Segment, or fail

   @throws NexusException on craft failure
   @throws NexusException on dub failure
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "doDubWork")
  protected void doDubWork() throws NexusException {
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
    var body = String.format("Failed while %s for Segment[%s] of Chain[%s] (%s) because %s\n\n%s",
      message,
      segmentId,
      chainId(),
      chainType(),
      e.getMessage(),
      Text.formatStackTrace(e));

    createSegmentErrorMessage(body);

    notification.publish(body,
      String.format("%s-Chain[%s] Failure",
        chainType(),
        chainId()));

    log.error("Failed while {} for Segment[{}] of Chain[{}] ({}) because {}",
      message,
      segmentId,
      chainId(),
      chainType(),
      e.getMessage());
  }

  /**
   Create a segment error message
   <p>
   [#177522463] Chain fabrication: segment messages broadcast somewhere the whole music team can see

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
   @throws NexusException if record is invalid
   */
  @Trace(resourceName = "nexus/fabricate", operationName = "updateSegmentState")
  private void updateSegmentState(Segment.State fromState, Segment.State toState) throws NexusException {
    if (fromState != segment.getState())
      throw new NexusException(String.format("Segment[%s] %s requires Segment must be in %s state.", segmentId, toState, fromState));
    fabricator.updateSegment(fabricator.getSegment().toBuilder().setState(toState).build());
    segment = fabricator.getSegment();
    log.debug("[segId={}] Segment transitioned to state {} OK", segmentId, toState);
  }

  /**
   @return the type of the chain, or unknown
   */
  private String chainType() {
    return Objects.nonNull(chain) && Objects.nonNull(chain.getType()) ?
      chain.getType().toString() : UNKNOWN;
  }

  /**
   @return the ID of the chain, or unknown
   */
  private String chainId() {
    return Objects.nonNull(chain) ? chain.getId() : UNKNOWN;
  }
}
