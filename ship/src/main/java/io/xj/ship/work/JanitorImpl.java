// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import io.xj.lib.app.Environment;
import io.xj.lib.entity.Entities;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.lib.util.Text;
import io.xj.nexus.NexusException;
import io.xj.nexus.persistence.*;
import io.xj.ship.ShipException;
import io.xj.ship.source.SegmentAudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collection;
import java.util.UUID;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
public class JanitorImpl implements Janitor {
  private static final Logger LOG = LoggerFactory.getLogger(JanitorImpl.class);
  private static final String METRIC_SEGMENT_ERASED = "segment_erased";
  private final ChainManager chainManager;
  private final int eraseSegmentsOlderThanSeconds;
  private final NexusEntityStore store;
  private final NotificationProvider notification;
  private final SegmentAudioManager segmentAudioManager;
  private final TelemetryProvider telemetryProvider;
  private final String threadName;

  @Inject
  public JanitorImpl(
    ChainManager chainManager,
    Environment env,
    NexusEntityStore store,
    NotificationProvider notification,
    SegmentAudioManager segmentAudioManager,
    TelemetryProvider telemetryProvider
  ) {
    this.chainManager = chainManager;
    this.notification = notification;
    this.segmentAudioManager = segmentAudioManager;
    this.store = store;
    this.telemetryProvider = telemetryProvider;

    eraseSegmentsOlderThanSeconds = env.getWorkEraseSegmentsOlderThanSeconds();

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
   Do the work inside a named thread
   */
  private void doWork() {
    // Seek segments to erase
    Collection<UUID> segmentIdsToErase;
    try {
      segmentIdsToErase = getSegmentIdsToErase();
    } catch (ShipException | ManagerFatalException | ManagerExistenceException | ManagerPrivilegeException | NexusException e) {
      var detail = Strings.isNullOrEmpty(e.getMessage()) ? e.getClass().getSimpleName() : e.getMessage();
      LOG.error("Failed while checking for segments to erase because {}", detail, e);
      notification.publish("Failure", String.format("Failed while checking for segments to erase because %s\n\n%s", detail, Text.formatStackTrace(e)));
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

    // FUTURE garbage collect chunks

    telemetryProvider.put(METRIC_SEGMENT_ERASED, StandardUnit.Count, segmentIdsToErase.size());
  }

  /**
   Get the IDs of all Segments that we ought to erase

   @return list of IDs of Segments we ought to erase
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
