// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.ship.source;

import com.google.common.collect.Maps;
import io.opencensus.stats.Measure;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.lib.util.Values;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.Segment;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.Segments;
import io.xj.ship.ShipException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Ship broadcast via HTTP Live Streaming https://www.pivotaltracker.com/story/show/179453189
 */
@Service
public class SegmentAudioManagerImpl implements SegmentAudioManager {
  private static final Logger LOG = LoggerFactory.getLogger(SegmentAudioManagerImpl.class);
  private final Map<UUID/* segmentId */, SegmentAudio> segmentAudios = Maps.newConcurrentMap();
  private final NexusEntityStore store;
  private final TelemetryProvider telemetryProvider;
  private final SegmentAudioCache cache;
  private final ChainManager chainManager;
  private final AppEnvironment env;
  private final HttpClientProvider httpClientProvider;
  private final JsonProvider jsonProvider;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final SegmentManager segmentManager;
  private final Measure.MeasureDouble SEGMENT_AUDIO_LOADED_AHEAD_SECONDS;
  private final int segmentLoadRetryLimit;
  private final int segmentLoadRetryDelayMillis;

  @Autowired
  public SegmentAudioManagerImpl(
    AppEnvironment env,
    NexusEntityStore store,
    SegmentAudioCache cache,
    TelemetryProvider telemetryProvider,
    ChainManager chainManager,
    HttpClientProvider httpClientProvider,
    JsonProvider jsonProvider,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    SegmentManager segmentManager
  ) {
    this.cache = cache;
    this.chainManager = chainManager;
    this.env = env;
    this.httpClientProvider = httpClientProvider;
    this.jsonProvider = jsonProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.segmentManager = segmentManager;
    this.store = store;
    this.telemetryProvider = telemetryProvider;
    segmentLoadRetryLimit = env.getShipSegmentLoadRetryLimit();
    segmentLoadRetryDelayMillis = env.getShipSegmentLoadRetryDelayMillis();

    SEGMENT_AUDIO_LOADED_AHEAD_SECONDS = telemetryProvider.gauge("segment_audio_loaded_ahead_seconds", "Segment Audio Loaded Ahead Seconds", "s");
  }

  @Override
  public Optional<SegmentAudio> get(UUID segmentId) {
    if (segmentAudios.containsKey(segmentId)) return Optional.of(segmentAudios.get(segmentId));
    return Optional.empty();
  }

  @Override
  public void load(String shipKey, Segment segment) throws ShipException {
    for (var i = 0; i < segmentLoadRetryLimit; i++)
      try {
        store.put(segment);
        var absolutePath = cache.downloadAndDecompress(segment);
        var segmentAudio = loadSegmentAudio(shipKey, segment, absolutePath);
        put(segmentAudio);
        return;

      } catch (NexusException | FileStoreException | IOException | InterruptedException e) {
        LOG.warn("Failed to preload audio for Segment[{}] of Template[{}], retrying in {}ms...", Segments.getIdentifier(segment), shipKey, segmentLoadRetryDelayMillis, e);
        collectGarbage(segment.getId());
        try {
          Thread.sleep(segmentLoadRetryDelayMillis);
        } catch (InterruptedException ex) {
          throw new ShipException("Failed to sleep");
        }
      }
    LOG.error("Failed to preload audio for Segment[{}] of Template[{}] after {} attempts", segment.getId(), shipKey, segmentLoadRetryLimit);
    throw new ShipException(String.format("Failed to preload audio for Segment[%s] of Template[%s] after %s attempts", segment.getId(), shipKey, segmentLoadRetryLimit));
  }


  @Override
  public ChainLoader loadChain(String shipKey, Runnable onFailure) {
    return new ChainLoaderImpl(shipKey, onFailure, chainManager, env, httpClientProvider, jsonProvider, jsonapiPayloadFactory, this, segmentManager, telemetryProvider);
  }

  @Override
  public SegmentAudio loadSegmentAudio(String shipKey, Segment segment, String absolutePath) {
    return new SegmentAudio(absolutePath, segment, shipKey, telemetryProvider, env);
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
    return segmentAudios.values().stream().filter(sa -> sa.intersects(shipKey, fromInstant, toInstant)).collect(Collectors.toList());
  }

  @Override
  public void sendTelemetry() {
    telemetryProvider.put(SEGMENT_AUDIO_LOADED_AHEAD_SECONDS, segmentAudios.values().stream().map(SegmentAudio::getSegment).mapToDouble(s -> Math.floor(Values.computeRelativeSeconds(Instant.parse(s.getEndAt())))).max().orElse(0));
  }

  @Override
  public boolean isLoadingOrReady(UUID segmentId, Long nowMillis) {
    return get(segmentId).map(sa -> sa.isLoadingOrReady(nowMillis)).orElse(false);
  }
}

