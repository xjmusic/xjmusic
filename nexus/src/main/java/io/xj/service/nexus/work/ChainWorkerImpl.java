package io.xj.service.nexus.work;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.typesafe.config.Config;
import datadog.trace.api.Trace;
import io.xj.Chain;
import io.xj.Segment;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.service.hub.client.HubClientAccess;
import io.xj.service.nexus.dao.ChainDAO;
import io.xj.service.nexus.dao.SegmentDAO;
import io.xj.service.nexus.dao.exception.DAOExistenceException;
import io.xj.service.nexus.dao.exception.DAOFatalException;
import io.xj.service.nexus.dao.exception.DAOPrivilegeException;
import io.xj.service.nexus.dao.exception.DAOValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Optional;

/**
 Chain Worker implementation
 */
public class ChainWorkerImpl extends WorkerImpl implements ChainWorker {
  private static final float MILLI = 1000;
  private static final float MILLIS_PER_SECOND = MILLI;
  private static final float NANOS_PER_SECOND = MILLI * MILLI * MILLI;
  private static final String NAME = "Chain";
  private static final String METRIC_SEGMENT_CREATED = "segment_created";
  private static final String METRIC_CHAIN_FORMAT = "chain.%s.%s";
  private static final String METRIC_FABRICATED_AHEAD_SECONDS = "fabricated_ahead_seconds";
  private static final String DEFAULT_NAME_PREVIEW = "preview";
  private static final String DEFAULT_NAME_PRODUCTION = "production";
  private final Logger log = LoggerFactory.getLogger(ChainWorker.class);
  private final HubClientAccess access = HubClientAccess.internal();
  private final int bufferProductionSeconds;
  private final int bufferPreviewSeconds;
  private final String chainId;
  private final ChainDAO chainDAO;
  private final SegmentDAO segmentDAO;
  private final WorkerFactory workers;
  private final TelemetryProvider telemetryProvider;

  @Inject
  public ChainWorkerImpl(
    @Assisted String chainId,
    ChainDAO chainDAO,
    Config config,
    NotificationProvider notification,
    SegmentDAO segmentDAO,
    TelemetryProvider telemetryProvider,
    WorkerFactory workerFactory
  ) {
    super(notification);
    this.chainId = chainId;
    this.chainDAO = chainDAO;
    this.segmentDAO = segmentDAO;
    this.workers = workerFactory;
    this.telemetryProvider = telemetryProvider;

    bufferProductionSeconds = config.getInt("work.bufferProductionSeconds");
    bufferPreviewSeconds = config.getInt("work.bufferPreviewSeconds");

    log.debug("Instantiated OK");
  }

  @Override
  protected String getName() {
    return NAME;
  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  @Trace(resourceName = "nexus/chain", operationName = "doWork")
  protected void doWork() {
    long startedAt = System.nanoTime();
    Chain chain;
    try {
      chain = chainDAO.readOne(access, chainId);
      if (Chain.State.Fabricate != chain.getState()) {
        log.error("Cannot fabricate Chain[{}] in non-Fabricate ({}) state!", chain.getId(), chain.getState());
        return;
      }
    } catch (DAOPrivilegeException | DAOFatalException | DAOExistenceException e) {
      log.error("Cannot find Chain[{}]", chainId);
      return;
    }

    try {
      int workBufferSeconds = bufferSecondsFor(chain);

      Optional<Segment> segment = chainDAO.buildNextSegmentOrCompleteTheChain(access, chain,
        Instant.now().plusSeconds(workBufferSeconds),
        Instant.now().minusSeconds(workBufferSeconds));
      if (segment.isEmpty()) return;
      Segment createdSegment = segmentDAO.create(access, segment.get());
      log.debug("Created Segment {}", createdSegment);
      telemetryProvider.getStatsDClient().
        incrementCounter(getChainMetricName(chain, METRIC_SEGMENT_CREATED));

      // FUTURE: fork/join thread possible for this sub-runnable of the fabrication worker
      workers.segment(createdSegment.getId()).run();
      log.info("Fabricated in {}s Segment[{}] of {}-Chain[{}] offset:{}",
        (double) (System.nanoTime() - startedAt) / NANOS_PER_SECOND,
        createdSegment.getId(),
        chain.getType(),
        chain.getId(),
        createdSegment.getOffset());

      // bums
      var fabricatedAheadSeconds = computeFabricatedAheadSeconds(chain);
      telemetryProvider.getStatsDClient()
        .gauge(getChainMetricName(chain, METRIC_FABRICATED_AHEAD_SECONDS), fabricatedAheadSeconds);
      chainDAO.update(access, chain.getId(), chain.toBuilder()
        .setFabricatedAheadSeconds(fabricatedAheadSeconds)
        .build());

    } catch (DAOPrivilegeException | DAOExistenceException | DAOValidationException | DAOFatalException e) {
      var body = String.format("Failed to create Segment of Chain[%s] (%s):\n\n%s",
        chain.getId(),
        chain.getType(),
        e.getMessage());

      notification.publish(body,
        String.format("%s-Chain[%s] Failure",
          chain.getType(),
          chain.getId()));

      log.error("Failed to created Segment in chainId={}, reason={}", chainId, e.getMessage());

      try {
        chainDAO.revive(access, chain.getId(), body);
      } catch (DAOFatalException | DAOPrivilegeException | DAOExistenceException | DAOValidationException e2) {
        log.error("Failed to revive chain after fatal error!", e2);
      }
    }
  }

  /**
   [#177072936] Mk1 UI each chain shows current fabrication latency@param chain
   */
  private float computeFabricatedAheadSeconds(Chain chain) throws DAOPrivilegeException, DAOFatalException, DAOExistenceException {
    var lastDubbedSegment = segmentDAO.readLastDubbedSegment(access, chain.getId());
    var dubbedUntil = lastDubbedSegment.isPresent() ?
      Instant.parse(lastDubbedSegment.get().getEndAt()) :
      Instant.parse(chain.getStartAt());
    var now = Instant.now();
    return (dubbedUntil.toEpochMilli() - now.toEpochMilli()) / MILLIS_PER_SECOND;
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

  /**
   Get the name for a given chain and metric

   @param chain      to get name for
   @param metricName to get get name for
   @return name for the given chain and metric
   */
  private String getChainMetricName(Chain chain, String metricName) {
    return String.format(METRIC_CHAIN_FORMAT, getChainName(chain), metricName);
  }

  /**
   Get the name for a given chain

   @param chain to get name for
   @return name for the given chain
   */
  private String getChainName(Chain chain) {
    return Chain.Type.Production.equals(chain.getType()) ?
      (!Strings.isNullOrEmpty(chain.getEmbedKey()) ? chain.getEmbedKey() : DEFAULT_NAME_PRODUCTION) :
      DEFAULT_NAME_PREVIEW;
  }
}
