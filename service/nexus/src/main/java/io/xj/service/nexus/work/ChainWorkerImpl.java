package io.xj.service.nexus.work;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.typesafe.config.Config;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.lib.util.ValueException;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.ChainDAO;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import io.xj.service.nexus.entity.Chain;
import io.xj.service.nexus.entity.ChainState;
import io.xj.service.nexus.entity.Segment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 Chain Worker implementation
 */
public class ChainWorkerImpl extends WorkerImpl implements ChainWorker {
  private static final String NAME = "Chain";
  private static final String SEGMENT_CREATED = "SEGMENT_CREATED";
  private final Logger log = LoggerFactory.getLogger(ChainWorker.class);
  private final HubClientAccess access = HubClientAccess.internal();
  private final int bufferProductionSeconds;
  private final int bufferPreviewSeconds;
  private final UUID chainId;
  private final ChainDAO chainDAO;
  private final SegmentDAO segmentDAO;
  private final WorkerFactory workers;

  @Inject
  public ChainWorkerImpl(
    @Assisted UUID chainId,
    Config config,
    SegmentDAO segmentDAO,
    ChainDAO chainDAO,
    WorkerFactory workerFactory,
    TelemetryProvider telemetryProvider
  ) {
    super(telemetryProvider);
    this.chainId = chainId;
    this.chainDAO = chainDAO;
    this.segmentDAO = segmentDAO;
    this.workers = workerFactory;

    bufferProductionSeconds = config.getInt("work.bufferProductionSeconds");
    bufferPreviewSeconds = config.getInt("work.bufferPreviewSeconds");

    log.info("Instantiated OK");
  }

  @Override
  protected String getName() {
    return NAME;
  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook

   @throws ValueException         on failure
   @throws DAOFatalException      on failure
   @throws DAOPrivilegeException  on failure
   @throws DAOValidationException on failure
   @throws DAOExistenceException  on failure
   */
  protected void doWork() throws ValueException, DAOFatalException, DAOPrivilegeException, DAOValidationException, DAOExistenceException {
    try {
      Chain chain = chainDAO.readOne(access, chainId);

      if (ChainState.Fabricate != chain.getState()) {
        log.error("Cannot fabricate Chain id:{} in non-Fabricate ({}) state!", chain.getId(), chain.getState());
        return;
      }

      int workBufferSeconds = bufferSecondsFor(chain);

      Optional<Segment> segment = chainDAO.buildNextSegmentOrCompleteTheChain(access, chain,
        Instant.now().plusSeconds(workBufferSeconds),
        Instant.now().minusSeconds(workBufferSeconds));
      if (segment.isEmpty()) return;
      segment.get().validate();
      Segment createdSegment = segmentDAO.create(access, segment.get());
      log.info("Created Segment {}", createdSegment);
      observeCount(SEGMENT_CREATED, 1.0);

      // FUTURE: fork/join thread possible for this sub-runnable of the fabrication worker
      workers.segment(createdSegment.getId()).run();
      log.info("Fabricated Segment, id:{}, chainId:{}, offset:{}",
        createdSegment.getId(), createdSegment.getChainId(), createdSegment.getOffset());

    } catch (Throwable e) {
      log.error("Failed to created Segment in chainId={}, reason={}", chainId, e.getMessage());
      throw e;
    }
  }

  /**
   Determine the buffer for a specified type of chain

   @param chain to get buffer for, based on its type
   @return buffer # of seconds this type of chain
   */
  private int bufferSecondsFor(Chain chain) {
    switch (chain.getType()) {
      default:
      case Production:
        return bufferProductionSeconds;
      case Preview:
        return bufferPreviewSeconds;
    }
  }
}
