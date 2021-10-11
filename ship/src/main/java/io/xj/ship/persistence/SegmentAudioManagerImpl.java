package io.xj.ship.persistence;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.api.Segment;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.nexus.NexusException;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.Segments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
@Singleton
public class SegmentAudioManagerImpl implements SegmentAudioManager {
  private static final Logger LOG = LoggerFactory.getLogger(SegmentAudioManagerImpl.class);
  private final Map<UUID/* segmentId */, SegmentAudio> segmentAudios = Maps.newConcurrentMap();
  private final FileStoreProvider fileStoreProvider;
  private final String shipBucket;
  private final NexusEntityStore store;
  private final SegmentAudioFactory segmentAudioFactory;

  @Inject
  public SegmentAudioManagerImpl(
    Environment env,
    FileStoreProvider fileStoreProvider,
    NexusEntityStore store,
    SegmentAudioFactory segmentAudioFactory
  ) {
    this.fileStoreProvider = fileStoreProvider;

    shipBucket = env.getShipBucket();
    this.store = store;
    this.segmentAudioFactory = segmentAudioFactory;
  }

  @Override
  public Optional<SegmentAudio> get(UUID segmentId) {
    if (segmentAudios.containsKey(segmentId)) return Optional.of(segmentAudios.get(segmentId));
    return Optional.empty();
  }

  @Override
  public void createAndLoadAudio(String shipKey, Segment segment) {
    // First we create this in Loading state as a placeholder
    try {
      store.put(segment);
    } catch (NexusException e) {
      LOG.error("Failed to put Segment in store", e);
    }
    var segmentAudio = segmentAudioFactory.from(shipKey, segment);
    put(segmentAudio);

    // Then we try to load the actual audio data
    try (var input = fileStoreProvider.streamS3Object(shipBucket, Segments.getStorageFilename(segment))) {
      segmentAudio.loadOggVorbis(new BufferedInputStream(input));
      put(segmentAudio);

    } catch (Exception e) {
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
    segmentAudios.remove(segmentId);
    try {
      store.deleteSegment(segmentId);
    } catch (NexusException e) {
      LOG.error("Failed to destroy Segment[{}]", segmentId);
    }
  }

  @Override
  public void retry(UUID segmentId) {
    SegmentAudio audio = segmentAudios.get(segmentId);
    createAndLoadAudio(audio.getShipKey(), audio.getSegment());
  }

  @Override
  public Collection<SegmentAudio> getAllIntersecting(String shipKey, Instant fromInstant, Instant toInstant) {
    return segmentAudios.values().stream()
      .filter(sa -> sa.intersects(shipKey, fromInstant, toInstant))
      .collect(Collectors.toList());
  }

  @Override
  public boolean isLoadingOrReady(UUID segmentId, Long nowMillis) {
    return
      get(segmentId)
        .map(sa -> sa.isLoadingOrReady(nowMillis))
        .orElse(false);
  }
}

