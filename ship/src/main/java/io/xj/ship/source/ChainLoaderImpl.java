// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.source;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import io.xj.api.Chain;
import io.xj.api.Segment;
import io.xj.api.SegmentState;
import io.xj.lib.app.Environment;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.util.Values;
import io.xj.nexus.persistence.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
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
@SuppressWarnings("DuplicatedCode")
public class ChainLoaderImpl extends ChainLoader {
  private static final Logger LOG = LoggerFactory.getLogger(ChainLoaderImpl.class);
  private static final String THREAD_NAME = "chain";
  private final ChainManager chainManager;
  private final HttpClientProvider httpClientProvider;
  private final JsonProvider jsonProvider;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final Runnable onFailure;
  private final SegmentAudioManager segmentAudioManager;
  private final SegmentManager segmentManager;
  private final SourceFactory source;
  private final String shipBaseUrl;
  private final String shipKey;
  private final int eraseSegmentsOlderThanSeconds;
  private final int shipFabricatedAheadThreshold;

  @Inject
  public ChainLoaderImpl(
    @Assisted("shipKey") String shipKey,
    @Assisted("onFailure") Runnable onFailure,
    ChainManager chainManager,
    Environment env,
    HttpClientProvider httpClientProvider,
    JsonProvider jsonProvider,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    SegmentAudioManager segmentAudioManager,
    SegmentManager segmentManager,
    SourceFactory source
  ) {
    this.chainManager = chainManager;
    this.httpClientProvider = httpClientProvider;
    this.jsonProvider = jsonProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.onFailure = onFailure;
    this.segmentAudioManager = segmentAudioManager;
    this.segmentManager = segmentManager;
    this.shipKey = shipKey;
    this.source = source;

    eraseSegmentsOlderThanSeconds = env.getWorkEraseSegmentsOlderThanSeconds();
    shipBaseUrl = env.getShipBaseUrl();
    shipFabricatedAheadThreshold = env.getWorkShipFabricatedAheadThresholdSeconds();
  }

  @Override
  public void compute() {
    final Thread currentThread = Thread.currentThread();
    final String oldName = currentThread.getName();
    currentThread.setName(THREAD_NAME);
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
    JsonapiPayload chainPayload;
    Chain chain;
    LOG.debug("will check for shipped data");
    var key = Chains.getShipKey(Chains.getFullKey(shipKey), EXTENSION_JSON);
    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", shipBaseUrl, key)))
    ) {
      chainPayload = jsonProvider.getMapper().readValue(response.getEntity().getContent(), JsonapiPayload.class);
      chain = jsonapiPayloadFactory.toOne(chainPayload);
      chainManager.put(chain);
    } catch (JsonapiException | ClassCastException | IOException | ManagerFatalException e) {
      LOG.error("Failed to retrieve previously fabricated chain for Template[{}] because {}", shipKey, e.getMessage());
      onFailure.run();
      return;
    }

    LOG.debug("will load Chain");

    Instant ignoreSegmentsBefore = Instant.now().minusSeconds(eraseSegmentsOlderThanSeconds);
    //noinspection DuplicatedCode
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
            ForkJoinPool.commonPool().execute(source.spawnSegmentLoader(shipKey, segment));
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
      LOG.debug("Fetched data for {} Segments (skipped {})", segmentLoaded.get(), segmentSkipped.get());
    else
      LOG.debug("skipped all {} Segments", segmentSkipped.get());

    // Nexus with bootstrap won't rehydrate stale Chain
    // https://www.pivotaltracker.com/story/show/178727631
    try {
      var aheadSeconds = Values.computeRelativeSeconds(Chains.computeFabricatedAheadAt(chain,
        segmentManager.readMany(ImmutableList.of(chain.getId())).stream()
          .filter(seg -> SegmentState.DUBBED.equals(seg.getState()))
          .collect(Collectors.toList())));

      if (aheadSeconds >= shipFabricatedAheadThreshold) {
        LOG.info("fabricated ahead {}s OK (> {}s)", aheadSeconds, shipFabricatedAheadThreshold);
      } else {
        LOG.warn("stale, fabricated ahead {}s (not > {}s)", aheadSeconds, shipFabricatedAheadThreshold);
      }

    } catch (ManagerFatalException | ManagerPrivilegeException | ManagerExistenceException e) {
      e.printStackTrace();
    }
  }
}
