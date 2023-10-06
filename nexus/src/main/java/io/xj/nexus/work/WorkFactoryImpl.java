// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.HubConfiguration;
import io.xj.hub.HubContent;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.mixer.MixerFactory;
import io.xj.lib.notification.NotificationProvider;
import io.xj.lib.telemetry.TelemetryProvider;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.dub.DubAudioCache;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.ship.broadcast.BroadcastFactory;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Service
public class WorkFactoryImpl implements WorkFactory {
  static final Logger LOG = LoggerFactory.getLogger(WorkFactoryImpl.class);
  final BroadcastFactory broadcastFactory;
  final CraftFactory craftFactory;
  final DubAudioCache dubAudioCache;
  final EntityFactory entityFactory;
  final FabricatorFactory fabricatorFactory;
  final FileStoreProvider fileStore;
  final HttpClientProvider httpClientProvider;
  final JsonProvider jsonProvider;
  final JsonapiPayloadFactory jsonapiPayloadFactory;
  final MixerFactory mixerFactory;
  final NexusEntityStore store;
  final NotificationProvider notification;
  final SegmentManager segmentManager;
  final TelemetryProvider telemetryProvider;
  final long dubCycleMillis;
  final int jsonExpiresInSeconds;
  final int mixerSeconds;
  final int outputFileNumberDigits;
  final boolean isJsonOutputEnabled;
  final int pcmChunkSizeBytes;
  final int cycleAudioBytes;
  final long shipCycleMillis;
  final String tempFilePathPrefix;
  final AtomicReference<WorkState> state = new AtomicReference<>(WorkState.Initializing);

  @Nullable
  CraftWork craftWork;

  @Nullable
  DubWork dubWork;

  @Nullable
  ShipWork shipWork;

  public WorkFactoryImpl(
    BroadcastFactory broadcastFactory,
    CraftFactory craftFactory,
    DubAudioCache dubAudioCache,
    EntityFactory entityFactory,
    FabricatorFactory fabricatorFactory,
    FileStoreProvider fileStore,
    HttpClientProvider httpClientProvider,
    JsonProvider jsonProvider,
    JsonapiPayloadFactory jsonapiPayloadFactory,
    MixerFactory mixerFactory,
    NexusEntityStore store,
    NotificationProvider notification,
    SegmentManager segmentManager,
    TelemetryProvider telemetryProvider,
    @Value("${dub.cycle.millis}") long dubCycleMillis,
    @Value("${json.expires.in.seconds}") int jsonExpiresInSeconds,
    @Value("${mixer.timeline.seconds}") int mixerSeconds,
    @Value("${output.file.number.digits}") int outputFileNumberDigits,
    @Value("${output.json.enabled}") boolean isJsonOutputEnabled,
    @Value("${output.pcm.chunk.size.bytes}") int pcmChunkSizeBytes,
    @Value("${ship.cycle.audio.bytes}") int cycleAudioBytes,
    @Value("${ship.cycle.millis}") long shipCycleMillis,
    @Value("${temp.file.path.prefix}") String tempFilePathPrefix
  ) {
    this.broadcastFactory = broadcastFactory;
    this.craftFactory = craftFactory;
    this.dubAudioCache = dubAudioCache;
    this.entityFactory = entityFactory;
    this.fabricatorFactory = fabricatorFactory;
    this.fileStore = fileStore;
    this.httpClientProvider = httpClientProvider;
    this.jsonProvider = jsonProvider;
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.mixerFactory = mixerFactory;
    this.store = store;
    this.notification = notification;
    this.segmentManager = segmentManager;
    this.telemetryProvider = telemetryProvider;
    this.dubCycleMillis = dubCycleMillis;
    this.jsonExpiresInSeconds = jsonExpiresInSeconds;
    this.mixerSeconds = mixerSeconds;
    this.outputFileNumberDigits = outputFileNumberDigits;
    this.isJsonOutputEnabled = isJsonOutputEnabled;
    this.pcmChunkSizeBytes = pcmChunkSizeBytes;
    this.cycleAudioBytes = cycleAudioBytes;
    this.shipCycleMillis = shipCycleMillis;
    this.tempFilePathPrefix = tempFilePathPrefix;
  }

  @Override
  public boolean start(
    WorkConfiguration workConfig,
    HubConfiguration hubConfig,
    Callable<HubContent> hubContentProvider,
    Consumer<Double> progressUpdateCallback,
    Runnable onDone
  ) {
    craftWork = new CraftWorkImpl(
      craftFactory,
      entityFactory,
      fabricatorFactory,
      fileStore,
      jsonapiPayloadFactory,
      jsonProvider,
      store,
      notification,
      segmentManager,
      telemetryProvider,
      hubContentProvider,
      hubConfig.getAudioBaseUrl(),
      hubConfig.getShipBaseUrl(),
      workConfig.getInputMode(),
      workConfig.getOutputMode(),
      workConfig.getInputTemplateKey(),
      isJsonOutputEnabled,
      tempFilePathPrefix,
      jsonExpiresInSeconds,
      workConfig.getOutputFrameRate(),
      workConfig.getOutputChannels(),
      workConfig.getCraftAheadSeconds()
    );
    dubWork = dub(hubConfig, workConfig);
    shipWork = new ShipWorkImpl(
      dubWork,
      notification,
      broadcastFactory,
      workConfig.getOutputMode(),
      workConfig.getOutputFileMode(),
      workConfig.getOutputSeconds(),
      shipCycleMillis,
      cycleAudioBytes,
      workConfig.getOutputPathPrefix(),
      outputFileNumberDigits,
      pcmChunkSizeBytes,
      workConfig.getShipAheadSeconds(),
      progressUpdateCallback
    );

    LOG.info("Will start");
    try {
      // Run work on separate threads.
      Thread craftThread = new Thread(craftWork::start);
      Thread dubThread = new Thread(dubWork::start);
      Thread shipThread = new Thread(shipWork::start);
      craftThread.start();
      dubThread.start();
      shipThread.start();

      // This blocks until a graceful exit on interrupt signal or Dub work complete
      dubThread.join();
      craftThread.join();
      shipThread.join();
      LOG.info("Finished");

    } catch (InterruptedException e) {
      LOG.info("Interrupted");
      craftWork.finish();
      dubWork.finish();
      shipWork.finish();
    }

    onDone.run();
    return true;
  }

  @Override
  public DubWork dub(HubConfiguration hubConfig, WorkConfiguration workConfig) {
    return new DubWorkImpl(
      craftWork,
      dubAudioCache,
      mixerFactory,
      notification,
      workConfig.getContentStoragePathPrefix(),
      hubConfig.getAudioBaseUrl(),
      mixerSeconds,
      dubCycleMillis,
      workConfig.getOutputFrameRate(),
      workConfig.getOutputChannels(),
      workConfig.getDubAheadSeconds());
  }

  @Override
  public void finish() {
    if (Objects.nonNull(shipWork)) {
      shipWork.finish();
    }
  }

  @Override
  public WorkState getWorkState() {
    return state.get();
  }

  @Override
  public boolean isHealthy() {
    return getWorkState() != WorkState.Failed;
  }

  @Override
  public SegmentManager getSegmentManager() {
    return segmentManager;
  }

  @Override
  public void reset() {
    segmentManager.reset();
    craftWork = null;
    dubWork = null;
    shipWork = null;
  }

  @Override
  public HubContent getSourceMaterial() {
    return Objects.requireNonNull(craftWork).getSourceMaterial();
  }

  @Override
  public Optional<Long> getShippedToChainMicros() {
    return Objects.nonNull(shipWork) ? shipWork.getShippedToChainMicros() : Optional.empty();
  }

  @Override
  public Optional<Long> getDubbedToChainMicros() {
    return Objects.nonNull(dubWork) ? dubWork.getDubbedToChainMicros() : Optional.empty();
  }

  @Override
  public Optional<Long> getCraftedToChainMicros() {
    return Objects.nonNull(craftWork) ? craftWork.getCraftedToChainMicros() : Optional.empty();
  }

  @Override
  public Optional<Long> getShipTargetChainMicros() {
    return Objects.nonNull(shipWork) ? shipWork.getShipTargetChainMicros() : Optional.empty();
  }

}
