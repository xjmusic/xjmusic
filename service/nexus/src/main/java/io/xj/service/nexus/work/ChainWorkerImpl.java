package io.xj.service.nexus.work;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.typesafe.config.Config;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.ChainDAO;
import io.xj.service.nexus.dao.SegmentDAO;
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
public class ChainWorkerImpl implements ChainWorker {
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
    ChainDAO chainDAO,
    SegmentDAO segmentDAO,
    WorkerFactory workerFactory,
    Config config
  ) {
    this.chainId = chainId;
    this.chainDAO = chainDAO;
    this.segmentDAO = segmentDAO;
    this.workers = workerFactory;

    bufferProductionSeconds = config.getInt("work.bufferProductionSeconds");
    bufferPreviewSeconds = config.getInt("work.bufferPreviewSeconds");
  }

  public void run() {
    final Thread currentThread = Thread.currentThread();
    final String _ogThreadName = currentThread.getName();
    currentThread.setName(_ogThreadName + "-ChainWorker-" + chainId);

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
      long start = Instant.now().toEpochMilli();

      // FUTURE: fork/join thread possible for this runnable
      workers.segment(createdSegment.getId()).run();

      log.info("Fabricated Segment in {}ms, id:{}, chainId:{}, offset:{}", Instant.now().toEpochMilli() - start,
        createdSegment.getId(), createdSegment.getChainId(), createdSegment.getOffset());

    } catch (Throwable e) {
      log.error("Failed to created Segment in chainId={}, reason={}", chainId, e.getMessage());

    } finally {
      currentThread.setName(_ogThreadName);
    }
  }

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
