// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.source;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.opencensus.stats.Measure;
import io.xj.nexus.model.Segment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.lib.util.Values;
import io.xj.nexus.NexusException;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.Segments;
import io.xj.ship.ShipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Ship broadcast via HTTP Live Streaming https://www.pivotaltracker.com/story/show/179453189
 */
@Singleton
public class SegmentAudioManagerImpl implements SegmentAudioManager {
  private static final Logger LOG = LoggerFactory.getLogger(SegmentAudioManagerImpl.class);
  private final Map<UUID/* segmentId */, SegmentAudio> segmentAudios = Maps.newConcurrentMap();
  private final NexusEntityStore store;
  private final TelemetryProvider telemetryProvider;
  private final SegmentAudioCache cache;
  private final SourceFactory sourceFactory;
  private final Measure.MeasureDouble SEGMENT_AUDIO_LOADED_AHEAD_SECONDS;

  @Inject
  public SegmentAudioManagerImpl(
    NexusEntityStore store,
    SegmentAudioCache cache,
    SourceFactory sourceFactory,
    TelemetryProvider telemetryProvider
  ) {
    this.cache = cache;
    this.sourceFactory = sourceFactory;
    this.store = store;
    this.telemetryProvider = telemetryProvider;

    SEGMENT_AUDIO_LOADED_AHEAD_SECONDS = telemetryProvider.gauge("segment_audio_loaded_ahead_seconds", "Segment Audio Loaded Ahead Seconds", "s");
  }

  @Override
  public Optional<SegmentAudio> get(UUID segmentId) {
    if (segmentAudios.containsKey(segmentId)) return Optional.of(segmentAudios.get(segmentId));
    return Optional.empty();
  }

  @Override
  public void load(String shipKey, Segment segment) throws ShipException {
    try {
      store.put(segment);
      var absolutePath = cache.downloadAndDecompress(segment);
      var segmentAudio = sourceFactory.loadSegmentAudio(shipKey, segment, absolutePath);
      put(segmentAudio);

    } catch (NexusException | FileStoreException | IOException | InterruptedException e) {
      LOG.error("Failed to preload audio for Segment[{}]", Segments.getIdentifier(segment), e);
      collectGarbage(segment.getId());
    }
  }

  @Override
  public void put(SegmentAudio segmentAudio) {
    try {
      segmentAudio.setUpdated(Instant.now());
      store.put(segmentAudio.getSegment());
    } catch (NexusException e) {
      LOG.error("Failed to put Segment[{}]", segmentAudio.getSegment().getId());
    }
    segmentAudios.put(segmentAudio.getId(), segmentAudio);
  }

  @Override
  public void collectGarbage(UUID segmentId) {
    try {
      segmentAudios.remove(segmentId);
      store.getSegment(segmentId).ifPresent(cache::collectGarbage);
      store.deleteSegment(segmentId);
    } catch (NexusException e) {
      LOG.error("Failed to destroy Segment[{}]", segmentId);
    }
  }

  @Override
  public void retry(UUID segmentId) throws ShipException {
    SegmentAudio audio = segmentAudios.get(segmentId);
    load(audio.getShipKey(), audio.getSegment());
  }

  @Override
  public Collection<SegmentAudio> getAllIntersecting(String shipKey, Instant fromInstant, Instant toInstant) {
    return segmentAudios.values().stream()
      .filter(sa -> sa.intersects(shipKey, fromInstant, toInstant))
      .collect(Collectors.toList());
  }

  @Override
  public void sendTelemetry() {
    telemetryProvider.put(SEGMENT_AUDIO_LOADED_AHEAD_SECONDS,
      segmentAudios.values().stream()
        .map(SegmentAudio::getSegment)
        .mapToDouble(s -> Math.floor(Values.computeRelativeSeconds(Instant.parse(s.getEndAt()))))
        .max()
        .orElse(0));
  }

  @Override
  public boolean isLoadingOrReady(UUID segmentId, Long nowMillis) {
    return
      get(segmentId)
        .map(sa -> sa.isLoadingOrReady(nowMillis))
        .orElse(false);
  }
}

