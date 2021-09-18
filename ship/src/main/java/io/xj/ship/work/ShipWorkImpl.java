// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.api.Chain;
import io.xj.api.ChainState;
import io.xj.api.Segment;
import io.xj.api.SegmentMessage;
import io.xj.api.SegmentMessageType;
import io.xj.api.SegmentState;
import io.xj.hub.enums.TemplateType;
import io.xj.lib.entity.Entities;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.telemetry.MultiStopwatch;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.lib.util.Text;
import io.xj.lib.util.Value;
import io.xj.ship.ShipException;
import io.xj.ship.persistence.ShipEntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static io.xj.lib.telemetry.MultiStopwatch.MILLIS_PER_SECOND;

/**
 The Lab Ship Distributed Work Manager (Implementation)
 <p>
 https://www.nurkiewicz.com/2014/11/executorservice-10-tips-and-tricks.html
 */
@Singleton
public class ShipWorkImpl implements ShipWork {
  private static final Logger LOG = LoggerFactory.getLogger(ShipWorkImpl.class);
  private static final String DEFAULT_NAME_PREVIEW = "preview";
  private static final String DEFAULT_NAME_PRODUCTION = "production";
  private static final String METRIC_CHAIN_FORMAT = "chain.%s.%s";
  private static final String METRIC_FABRICATED_AHEAD_SECONDS = "fabricated_ahead_seconds";
  private static final String METRIC_SEGMENT_CREATED = "segment_created";
  private static final String METRIC_CHAIN_REVIVED = "chain_revived";
  private static final String METRIC_SEGMENT_ERASED = "segment_erased";
  private final Map<UUID, Long> chainNextIngestMillis = Maps.newHashMap();
  private final ShipEntityStore store;
  private final NotificationProvider notification;
  private final TelemetryProvider telemetryProvider;
  private final boolean janitorEnabled;
  private final boolean medicEnabled;
  private final int bufferPreviewSeconds;
  private final int bufferProductionSeconds;
  private final int cycleMillis;
  private final int eraseSegmentsOlderThanSeconds;
  private final int ingestCycleSeconds;
  private final int janitorCycleSeconds;
  private final int medicCycleSeconds;
  private final int reviveChainFabricatedBehindSeconds;
  private final int reviveChainProductionGraceSeconds;
  private final long healthCycleStalenessThresholdMillis;
  private MultiStopwatch timer;
  private long nextCycleMillis = 0;
  private long nextJanitorMillis = 0;
  private long nextMedicMillis = 0;
  private boolean alive = true;
  private final ShipWorkChainManager chainManager;

  @Inject
  public ShipWorkImpl(
    Config config,
    ShipEntityStore store,
    NotificationProvider notification,
    TelemetryProvider telemetryProvider,
    ShipWorkChainManager shipWorkChainManager
  ) {
    this.notification = notification;
    this.store = store;
    this.telemetryProvider = telemetryProvider;
    this.chainManager = shipWorkChainManager;

    bufferPreviewSeconds = config.getInt("work.bufferPreviewSeconds");
    bufferProductionSeconds = config.getInt("work.bufferProductionSeconds");
    cycleMillis = config.getInt("work.cycleMillis");
    eraseSegmentsOlderThanSeconds = config.getInt("work.eraseSegmentsOlderThanSeconds");
    healthCycleStalenessThresholdMillis = config.getInt("work.healthCycleStalenessThresholdSeconds") * MILLIS_PER_SECOND;
    ingestCycleSeconds = config.getInt("work.ingestCycleSeconds");
    janitorCycleSeconds = config.getInt("work.janitorCycleSeconds");
    janitorEnabled = config.getBoolean("work.janitorEnabled");
    medicCycleSeconds = config.getInt("work.medicCycleSeconds");
    medicEnabled = config.getBoolean("work.medicEnabled");
    reviveChainFabricatedBehindSeconds = config.getInt("fabrication.reviveChainFabricatedBehindSeconds");
    reviveChainProductionGraceSeconds = config.getInt("fabrication.reviveChainProductionGraceSeconds");
    Executors.newSingleThreadScheduledExecutor();

    LOG.debug("Instantiated OK");
  }

  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  public void run() {
    if (System.currentTimeMillis() < nextCycleMillis) return;
    nextCycleMillis = System.currentTimeMillis() + cycleMillis;

    // Poll the chain manager
    chainManager.poll();

    // Do the ship work for the current ship key
    try {
      // FUTURE ship work for the current ship key

      if (medicEnabled) doMedic();
      if (janitorEnabled) doJanitor();
    } catch (Exception e) {
      didFailWhile("Running Ship Work", e);
    }

    // End lap & do telemetry on all fabricated chains
    timer.lap();
    LOG.info("Lap time: {}", timer.lapToString());
    timer.clearLapSections();
  }

  /**
   [#158897383] Engineer wants platform heartbeat to check for any stale production chains in fabricate state,
   and if found, *revive* it in order to ensure the Chain remains in an operable state.
   <p>
   [#177021797] Medic relies on precomputed  telemetry of fabrication latency
   */
  private void doMedic() {
    if (System.currentTimeMillis() < nextMedicMillis) return;
    nextMedicMillis = System.currentTimeMillis() + (medicCycleSeconds * MILLIS_PER_SECOND);
    timer.section("Medic");

    try {

      // FUTURE perform any medic tasks

    } catch (Exception e) {
      didFailWhile("Medic", e);
    }
  }


  /**
   Do the work-- this is called by the underlying WorkerImpl run() hook
   */
  protected void doJanitor() {
    if (System.currentTimeMillis() < nextJanitorMillis) return;
    nextJanitorMillis = System.currentTimeMillis() + (janitorCycleSeconds * MILLIS_PER_SECOND);
    timer.section("Janitor");

    // Seek segments to erase
    Collection<UUID> segmentIdsToErase;
    try {
      segmentIdsToErase = getSegmentIdsToErase();
    } catch (ShipException e) {
      didFailWhile("Checking for segments to erase", e);
      return;
    }

    // Erase segments if necessary
    if (segmentIdsToErase.isEmpty())
      LOG.info("Found no segments to erase");
    else
      LOG.info("Found {} segments to erase", segmentIdsToErase.size());

    for (UUID segmentId : segmentIdsToErase) {
      try {
        store.deleteSegment(segmentId);
        LOG.info("Did erase Segment[{}]", segmentId);
      } catch (ShipException e) {
        LOG.warn("Error while destroying Segment[{}]", segmentId);
      }
    }

    telemetryProvider.put(METRIC_SEGMENT_ERASED, StandardUnit.Count, segmentIdsToErase.size());
  }

  @Override
  public void work() {
    timer = MultiStopwatch.start();
    while (alive) this.run();
  }

  @Override
  public void stop() {
    alive = false;
  }

  /**
   Log and of segment message of error that job failed while (message)

   @param message phrased like "Doing work"
   @param e       exception (optional)
   */
  private void didFailWhile(String message, Exception e) {
    var detail = Strings.isNullOrEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();

    LOG.error("Failed while {} because {}", message, detail, e);

    notification.publish(String.format("Failed while %s because %s\n\n%s", message, detail, Text.formatStackTrace(e)), "Failure");
  }

  /**
   Whether this Segment is before a given threshold, first by end-at if available, else begin-at

   @param eraseBefore threshold to filter before
   @return true if segment is before threshold
   */
  protected boolean isBefore(Segment segment, Instant eraseBefore) {
    return Value.isSet(segment.getEndAt()) ?
      Instant.parse(segment.getEndAt()).isBefore(eraseBefore) :
      Instant.parse(segment.getBeginAt()).isBefore(eraseBefore);
  }

  /**
   Get the IDs of all Segments that we ought to erase

   @return list of IDs of Segments we ought to erase
   */
  private Collection<UUID> getSegmentIdsToErase() throws ShipException {
    Instant eraseBefore = Instant.now().minusSeconds(eraseSegmentsOlderThanSeconds);
    Collection<UUID> segmentIds = Lists.newArrayList();
    for (UUID chainId : store.getAllChains().stream()
      .flatMap(Entities::flatMapIds)
      .collect(Collectors.toList()))
      store.getAllSegments(chainId)
        .stream()
        .filter(segment -> isBefore(segment, eraseBefore))
        .flatMap(Entities::flatMapIds)
        .forEach(segmentIds::add);
    return segmentIds;
  }

  @Override
  public boolean isHealthy() {
    return chainManager.isHealthy()
      && nextCycleMillis > System.currentTimeMillis() - healthCycleStalenessThresholdMillis;
  }

}
