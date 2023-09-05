// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

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
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.ship.broadcast.BroadcastFactory;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

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
  final HubClient hubClient;
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
  final int planAheadSeconds;
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
    HubClient hubClient,
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
    @Value("${ship.output.synchronous.plan.ahead.seconds}") int planAheadSeconds,
    @Value("${temp.file.path.prefix}") String tempFilePathPrefix
  ) {
    this.broadcastFactory = broadcastFactory;
    this.craftFactory = craftFactory;
    this.dubAudioCache = dubAudioCache;
    this.entityFactory = entityFactory;
    this.fabricatorFactory = fabricatorFactory;
    this.fileStore = fileStore;
    this.httpClientProvider = httpClientProvider;
    this.hubClient = hubClient;
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
    this.planAheadSeconds = planAheadSeconds;
    this.tempFilePathPrefix = tempFilePathPrefix;
  }

  @Override
  public boolean start(
    WorkConfiguration configuration,
    Runnable onDone
  ) {
    craftWork = new CraftWorkImpl(
      craftFactory,
      entityFactory,
      fabricatorFactory,
      fileStore,
      httpClientProvider,
      hubClient,
      jsonapiPayloadFactory,
      jsonProvider,
      store,
      notification,
      segmentManager,
      telemetryProvider,
      configuration.getInputMode(),
      configuration.getOutputMode(),
      configuration.getInputTemplateKey(),
      isJsonOutputEnabled,
      tempFilePathPrefix,
      jsonExpiresInSeconds
    );
    dubWork = new DubWorkImpl(
      craftWork,
      dubAudioCache,
      mixerFactory,
      notification,
      mixerSeconds,
      dubCycleMillis
    );
    shipWork = new ShipWorkImpl(
      dubWork,
      notification,
      broadcastFactory,
      configuration.getOutputMode(),
      configuration.getOutputFileMode(),
      configuration.getOutputSeconds(),
      shipCycleMillis,
      cycleAudioBytes,
      planAheadSeconds,
      configuration.getOutputPathPrefix(),
      outputFileNumberDigits,
      pcmChunkSizeBytes
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
  @Nullable
  public CraftWork getCraftWork() {
    return craftWork;
  }

  @Override
  @Nullable
  public DubWork getDubWork() {
    return dubWork;
  }

  @Override
  @Nullable
  public ShipWork getShipWork() {
    return shipWork;
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
    return craftWork.getSourceMaterial();
  }

}
