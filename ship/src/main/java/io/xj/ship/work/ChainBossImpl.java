// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.Chain;
import io.xj.api.Segment;
import io.xj.api.SegmentState;
import io.xj.lib.app.Environment;
import io.xj.lib.filestore.FileStoreException;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.nexus.persistence.*;
import io.xj.ship.persistence.SegmentAudioManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.xj.lib.filestore.FileStoreProvider.EXTENSION_JSON;

/**
 Ship broadcast via HTTP Live Streaming #179453189
 */
public class ChainBossImpl extends ChainBoss {
  private static final Logger LOG = LoggerFactory.getLogger(ChainBossImpl.class);
  private final WorkFactory work;
  private final ChainManager chainManager;
  private final FileStoreProvider fileStoreProvider;
  private final int eraseSegmentsOlderThanSeconds;
  private final int shipFabricatedAheadThreshold;
  private final JsonProvider jsonProvider;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final Runnable onFailure;
  private final SegmentAudioManager segmentAudioManager;
  private final SegmentManager segmentManager;
  private final String shipBucket;
  private final String shipKey;
  private final String threadName;

  @Inject
  public ChainBossImpl(
    @Assisted("shipKey") String shipKey,
    @Assisted("onFailure") Runnable onFailure,
    ChainManager chainManager,
    Environment env,
    FileStoreProvider fileStoreProvider,
    JsonProvider jsonProvider,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    SegmentAudioManager segmentAudioManager,
    SegmentManager segmentManager,
    WorkFactory work
  ) {
    this.work = work;
    this.chainManager = chainManager;
    this.fileStoreProvider = fileStoreProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.jsonProvider = jsonProvider;
    this.onFailure = onFailure;
    this.segmentManager = segmentManager;
    this.shipKey = shipKey;

    eraseSegmentsOlderThanSeconds = env.getWorkEraseSegmentsOlderThanSeconds();
    shipFabricatedAheadThreshold = env.getWorkShipFabricatedAheadThresholdSeconds();

    shipBucket = env.getShipBucket();
    this.segmentAudioManager = segmentAudioManager;

    threadName = String.format("CHAIN:%s", this.shipKey);
  }

  @Override
  public void compute() {
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
    var nowMillis = Instant.now().toEpochMilli();
    var success = new AtomicBoolean(true);
    var segmentSkipped = new AtomicInteger(0);
    var segmentLoaded = new AtomicInteger(0);
    String chainFullKey;
    InputStream chainStream;
    JsonapiPayload chainPayload;
    Chain chain;
    try {
      LOG.info("will check for last shipped data");
      chainFullKey = Chains.getShipKey(Chains.getFullKey(shipKey), EXTENSION_JSON);
      chainStream = fileStoreProvider.streamS3Object(shipBucket, chainFullKey);
      chainPayload = jsonProvider.getObjectMapper().readValue(chainStream, JsonapiPayload.class);
      chain = jsonapiPayloadFactory.toOne(chainPayload);
      chainManager.put(chain);
    } catch (FileStoreException | JsonapiException | ClassCastException | IOException | ManagerFatalException e) {
      LOG.error("Failed to retrieve previously fabricated chain for Template[{}] because {}", shipKey, e.getMessage());
      onFailure.run();
      return;
    }

    LOG.debug("will load Chain");

    Instant ignoreSegmentsBefore = Instant.now().minusSeconds(eraseSegmentsOlderThanSeconds);
    chainPayload.getIncluded().stream()
      .filter(po -> po.isType(Segment.class))
      .flatMap(po -> {
        try {
          return Stream.of((Segment) jsonapiPayloadFactory.toOne(po));
        } catch (JsonapiException | ClassCastException e) {
          LOG.error("Could not deserialize Segment from shipped Chain JSON because {}", e.getMessage());
          success.set(false);
          return Stream.empty();
        }
      })
      .filter(seg -> SegmentState.DUBBED.equals(seg.getState()))
      .forEach(segment -> {
        try {
          if (Segments.isBefore(segment, ignoreSegmentsBefore)) {
            segmentSkipped.incrementAndGet();
            LOG.debug("Skipped past Segment[{}]", Segments.getIdentifier(segment));

          } else if (segmentAudioManager.isLoadingOrReady(segment.getId(), nowMillis)) {
            segmentSkipped.incrementAndGet();
            LOG.debug("Skipped existing Segment[{}] ({})", Segments.getIdentifier(segment),
              segmentManager.readOne(segment.getId()).getState());

          } else {
            segmentLoaded.incrementAndGet();
            ForkJoinPool.commonPool().execute(work.spawnSegmentLoader(shipKey, segment));
          }

        } catch (Exception e) {
          LOG.error("Could not load Segment[{}]", Segments.getIdentifier(segment), e);
          success.set(false);
        }
      });

    // Quit if anything failed up to here
    if (!success.get()) {
      LOG.error("Failed!");
      onFailure.run();
      return;
    }

    // OK
    if (0 < segmentLoaded.get())
      LOG.info("will load {} Segments (skipped {})", segmentLoaded.get(), segmentSkipped.get());
    else
      LOG.info("skipped  all {} Segments", segmentSkipped.get());

    // Nexus with bootstrap won't rehydrate stale Chain
    // https://www.pivotaltracker.com/story/show/178727631
    var fabricatedAheadSeconds = computeFabricatedAheadSeconds(chain);

    if (fabricatedAheadSeconds >= shipFabricatedAheadThreshold) {
      LOG.info("fabricated ahead {}s OK (> {}s)", fabricatedAheadSeconds, shipFabricatedAheadThreshold);
    } else {
      LOG.warn("stale, fabricated ahead {}s (not > {}s)", fabricatedAheadSeconds, shipFabricatedAheadThreshold);
    }
  }

  private float computeFabricatedAheadSeconds(Chain chain) {
    try {
      return Chains.computeFabricatedAheadSeconds(chain,
        segmentManager.readMany(ImmutableList.of(chain.getId())).stream()
          .filter(seg -> SegmentState.DUBBED.equals(seg.getState()))
          .collect(Collectors.toList()));

    } catch (ManagerFatalException | ManagerPrivilegeException | ManagerExistenceException e) {
      LOG.error("Failed to get fabricated segments", e);
      return 0;
    }
  }

}
