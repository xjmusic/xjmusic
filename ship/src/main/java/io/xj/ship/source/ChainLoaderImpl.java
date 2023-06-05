// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.source;


import io.opencensus.stats.Measure;
import io.xj.lib.app.AppEnvironment;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiException;
import io.xj.lib.jsonapi.JsonapiPayload;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.persistence.ChainManager;
import io.xj.nexus.persistence.Chains;
import io.xj.nexus.persistence.FilePathProvider;
import io.xj.nexus.persistence.ManagerFatalException;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.Segments;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static io.xj.lib.filestore.FileStoreProvider.EXTENSION_JSON;

/**
 * Ship broadcast via HTTP Live Streaming https://www.pivotaltracker.com/story/show/179453189
 */
@SuppressWarnings("DuplicatedCode")
public class ChainLoaderImpl extends ChainLoader {
  private static final Logger LOG = LoggerFactory.getLogger(ChainLoaderImpl.class);
  private static final String THREAD_NAME = "chain";
  private final ChainManager chainManager;
  private final HttpClientProvider httpClientProvider;
  private final FilePathProvider filePathProvider;
  private final JsonProvider jsonProvider;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final Measure.MeasureLong CHAIN_LOADED;
  private final Measure.MeasureLong SEGMENT_LOADED;
  private final Measure.MeasureLong SEGMENT_IGNORED;
  private final boolean isLocalModeEnabled;
  private final Runnable onFailure;
  private final SegmentAudioManager segmentAudioManager;
  private final SegmentManager segmentManager;
  private final String shipBaseUrl;
  private final String shipKey;
  private final TelemetryProvider telemetryProvider;
  private final int loadBackSeconds;
  private final int loadAheadSeconds;

  public ChainLoaderImpl(
    String shipKey,
    Runnable onFailure,
    AppEnvironment env, ChainManager chainManager,
    HttpClientProvider httpClientProvider,
    FilePathProvider filePathProvider,
    JsonProvider jsonProvider,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    SegmentAudioManager segmentAudioManager,
    SegmentManager segmentManager,
    TelemetryProvider telemetryProvider
  ) {
    this.chainManager = chainManager;
    this.httpClientProvider = httpClientProvider;
    this.filePathProvider = filePathProvider;
    this.jsonProvider = jsonProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.onFailure = onFailure;
    this.segmentAudioManager = segmentAudioManager;
    this.segmentManager = segmentManager;
    this.shipKey = shipKey;
    this.telemetryProvider = telemetryProvider;
    this.isLocalModeEnabled = env.isYardLocalModeEnabled();

    loadAheadSeconds = env.getShipPlaylistAheadSeconds() + env.getShipSegmentLoadAheadSeconds();
    loadBackSeconds = env.getShipPlaylistBackSeconds();
    shipBaseUrl = env.getShipBaseUrl();

    CHAIN_LOADED = telemetryProvider.count("chain_loaded", "Chain Loaded", "");
    SEGMENT_LOADED = telemetryProvider.count("segment_loaded", "Segment Loaded", "");
    SEGMENT_IGNORED = telemetryProvider.count("segment_ignored", "Segment Ignored", "");
  }

  @Override
  public void run() {
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
   * Do the work inside a named thread
   */
  private void doWork() {
    var nowMillis = Instant.now().toEpochMilli();
    var success = new AtomicBoolean(true);
    var segmentIgnoredPast = new AtomicInteger(0);
    var segmentIgnoredFuture = new AtomicInteger(0);
    var segmentIgnoredLoading = new AtomicInteger(0);
    var segmentLoaded = new AtomicInteger(0);
    JsonapiPayload chainPayload;
    Chain chain;
    LOG.debug("will load chain");
    var key = Chains.getShipKey(Chains.getFullKey(shipKey), EXTENSION_JSON);
    try {
      var json = isLocalModeEnabled ? readLocalFile(filePathProvider.computeTempFilePath(key)) : fetchRemoteFile(key);
      chainPayload = jsonProvider.getMapper().readValue(json, JsonapiPayload.class);
      chain = jsonapiPayloadFactory.toOne(chainPayload);
      chainManager.put(chain);
      LOG.info("Loaded Chain[{}]", Chains.getIdentifier(chain));
      telemetryProvider.put(CHAIN_LOADED, 1L);

    } catch (SourceNotReadyException e) {
      LOG.warn("Source not ready for Template[{}] because {}", shipKey, e.getMessage());
      return;
    } catch (JsonapiException | ClassCastException | IOException | ManagerFatalException e) {
      LOG.error("Failed to retrieve previously fabricated chain for Template[{}] because {}", shipKey, e.getMessage());
      onFailure.run();
      return;
    }

    Instant ignoreSegmentsBefore = Instant.now().minusSeconds(loadBackSeconds);
    Instant ignoreSegmentsAfter = Instant.now().plusSeconds(loadAheadSeconds);
    //noinspection DuplicatedCode
    chainPayload.getIncluded().parallelStream()
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
            segmentIgnoredPast.incrementAndGet();
            LOG.debug("Ignored past Segment[{}]", Segments.getIdentifier(segment));

          } else if (Segments.isAfter(segment, ignoreSegmentsAfter)) {
            segmentIgnoredFuture.incrementAndGet();
            LOG.debug("Ignored future Segment[{}]", Segments.getIdentifier(segment));

          } else if (segmentAudioManager.isLoadingOrReady(segment.getId(), nowMillis)) {
            segmentIgnoredLoading.incrementAndGet();
            LOG.debug("Ignored existing Segment[{}] ({})", Segments.getIdentifier(segment),
              segmentManager.readOne(segment.getId()).getState());

          } else {
            segmentLoaded.incrementAndGet();
            var audio =
              segmentAudioManager.get(segment.getId());

            if (audio.isEmpty()) {
              segmentAudioManager.load(shipKey, segment);

            } else switch (audio.get().getState()) {
              case Pending, Decoding, Ready -> {
                // no op; if the segment spends too much time in this state, it'll time out
              }
              case Failed -> segmentAudioManager.retry(segment.getId());
            }
          }

        } catch (Exception e) {
          LOG.error("Could not load Segment[{}]", Segments.getIdentifier(segment), e);
          segmentAudioManager.collectGarbage(segment.getId());
          success.set(false);
        }
      });

    // Quit if anything failed up to here
    if (!success.get()) {
      LOG.error("Failed!");
      onFailure.run();
      return;
    }

    // Telemetry
    telemetryProvider.put(SEGMENT_LOADED, segmentLoaded.longValue());
    telemetryProvider.put(SEGMENT_IGNORED, segmentIgnoredPast.longValue());
    telemetryProvider.put(SEGMENT_IGNORED, segmentIgnoredFuture.longValue());
    if (0 < segmentLoaded.get())
      LOG.info("Fetched data for {} Segments. Ignored: {} Loading, {} Past, {} Future",
        segmentLoaded.get(),
        segmentIgnoredLoading.get(),
        segmentIgnoredPast.get(),
        segmentIgnoredFuture.get());
    else
      LOG.info("Ignored all segments: {} Loading, {} Past, {} Future",
        segmentIgnoredLoading.get(),
        segmentIgnoredPast.get(),
        segmentIgnoredFuture.get());
  }

  /**
   * Fetch a remote file via HTTP
   *
   * @param key the key of the file to fetch
   * @return the file contents
   * @throws IOException if the file could not be fetched
   */
  private InputStream fetchRemoteFile(String key) throws IOException {
    CloseableHttpClient client = httpClientProvider.getClient();
    try (
      CloseableHttpResponse response = client.execute(new HttpGet(String.format("%s%s", shipBaseUrl, key)))
    ) {
      if (!Objects.equals(HttpStatus.OK.value(), response.getStatusLine().getStatusCode())) {
        throw new IOException("Failed to get previously fabricated chain for Template[" + key + "] because " + response.getStatusLine().getStatusCode() + " " + response.getStatusLine().getReasonPhrase());
      }
      return response.getEntity().getContent();

    } catch (ClassCastException | IOException e) {
      throw new IOException("Failed to get previously fabricated chain for Template[" + key + "] because " + e.getMessage(), e);
    }
  }

  /**
   * Read a local file from disk
   *
   * @param path the key of the file to fetch
   * @return the file contents
   * @throws SourceNotReadyException if the file could not be fetched
   */
  private InputStream readLocalFile(String path) throws SourceNotReadyException {
    try {
      return Files.newInputStream(Path.of(path));
    } catch (IOException e) {
      throw new SourceNotReadyException("Failed to read " + path + " because " + e.getMessage(), e);
    }
  }
}
