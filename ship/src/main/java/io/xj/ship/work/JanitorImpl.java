// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.opencensus.stats.Measure;
import io.xj.lib.entity.Entities;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.lib.util.Text;
import io.xj.nexus.NexusException;
import io.xj.nexus.persistence.*;
import io.xj.ship.ShipException;
import io.xj.ship.broadcast.MediaSeqNumProvider;
import io.xj.ship.broadcast.PlaylistPublisher;
import io.xj.ship.source.SegmentAudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

/**
 * Ship broadcast via HTTP Live Streaming https://www.pivotaltracker.com/story/show/179453189
 */
@Service
public class JanitorImpl implements Janitor {
  private static final Logger LOG = LoggerFactory.getLogger(JanitorImpl.class);
  private final Measure.MeasureLong METRIC_SEGMENT_ERASED;
  private final ChainManager chainManager;
  private final int eraseSegmentsOlderThanSeconds;
  private final NexusEntityStore store;
  private final NotificationProvider notification;
  private final PlaylistPublisher playlist;
  private final SegmentAudioManager segmentAudioManager;
  private final TelemetryProvider telemetryProvider;
  private final String threadName;
  private final MediaSeqNumProvider mediaSeqNumProvider;
  private final String envName;
  private final String shipKey;

  @Autowired
  public JanitorImpl(
    ChainManager chainManager,
    MediaSeqNumProvider mediaSeqNumProvider,
    NexusEntityStore store,
    NotificationProvider notification,
    PlaylistPublisher playlist,
    SegmentAudioManager segmentAudioManager,
    TelemetryProvider telemetryProvider,
    @Value("${ship.key}") String shipKey,
    @Value("${environment}") String environment,
    @Value("${work.erase.seconds.older.than.seconds}") int workEraseSegmentsOlderThanSeconds
  ) {
    this.chainManager = chainManager;
    this.notification = notification;
    this.playlist = playlist;
    this.segmentAudioManager = segmentAudioManager;
    this.store = store;
    this.telemetryProvider = telemetryProvider;
    this.envName = Text.toProper(environment);
    this.shipKey = shipKey;

    this.eraseSegmentsOlderThanSeconds = workEraseSegmentsOlderThanSeconds;

    METRIC_SEGMENT_ERASED = telemetryProvider.count("ship_segments_erased", "Ship Segments Erased", "");
    this.mediaSeqNumProvider = mediaSeqNumProvider;

    threadName = "Janitor";
  }

  @Override
  public void cleanup() {
    final Thread currentThread = Thread.currentThread();
    final String oldName = currentThread.getName();
    currentThread.setName(threadName);
    try {
      doWork();
    } finally {
      currentThread.setName(oldName);
    }
  }

  /**
   * Do the work inside a named thread
   */
  private void doWork() {
    // Seek segments to erase
    Collection<UUID> segmentIdsToErase;
    try {
      segmentIdsToErase = getSegmentIdsToErase();
    } catch (ShipException | ManagerFatalException | ManagerExistenceException | ManagerPrivilegeException |
             NexusException e) {
      var detail = Strings.isNullOrEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();
      LOG.error("Failed while checking for segments to erase because {}", detail, e);
      notification.publish(
        Strings.isNullOrEmpty(shipKey) ? String.format("%s-Chain[%s] Janitor Failure", envName, shipKey) : String.format("%s-Chains Janitor Failure", envName),
        String.format("Failed while checking for segments to erase because %s\n\n%s", detail, Text.formatStackTrace(e)));
      return;
    }

    // Erase segments if necessary
    if (segmentIdsToErase.isEmpty())
      LOG.debug("found no segments to erase");
    else
      LOG.debug("will erase {} segments", segmentIdsToErase.size());

    for (UUID segmentId : segmentIdsToErase) {
      segmentAudioManager.collectGarbage(segmentId);
      LOG.debug("collected garbage Segment[{}]", segmentId);
    }

    // Collect garbage from playlist-- when everything else stalls, this will ensure the health check fails the way we want it to
    playlist.collectGarbage(mediaSeqNumProvider.computeMediaSeqNum(System.currentTimeMillis()));

    telemetryProvider.put(METRIC_SEGMENT_ERASED, Long.valueOf(segmentIdsToErase.size()));
  }

  /**
   * Get the IDs of all Segments that we ought to erase
   *
   * @return list of IDs of Segments we ought to erase
   */
  private Collection<UUID> getSegmentIdsToErase() throws ShipException, ManagerFatalException, ManagerExistenceException, ManagerPrivilegeException, NexusException {
    Instant eraseBefore = Instant.now().minusSeconds(eraseSegmentsOlderThanSeconds);
    Collection<UUID> segmentIds = Lists.newArrayList();

    for (UUID chainId : chainManager.readAll().stream()
      .flatMap(Entities::flatMapIds).toList())
      store.getAllSegments(chainId)
        .stream()
        .filter(segment -> Segments.isBefore(segment, eraseBefore))
        .flatMap(Entities::flatMapIds)
        .forEach(segmentIds::add);
    return segmentIds;
  }

}
