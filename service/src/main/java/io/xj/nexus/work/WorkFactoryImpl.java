// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.lock.LockProvider;
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
import org.jetbrains.annotations.Nullable;
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
  final LockProvider lockProvider;
  final MixerFactory mixerFactory;
  final NexusEntityStore store;
  final NotificationProvider notification;
  final SegmentManager segmentManager;
  final TelemetryProvider telemetryProvider;
  final long dubCycleMillis;
  final String environment;
  final String inputMode;
  final String inputTemplateKey;
  final int jsonExpiresInSeconds;
  final int mixerSeconds;
  final String outputFileMode;
  final int outputFileNumberDigits;
  final boolean isJsonOutputEnabled;
  final String outputMode;
  final String outputPathPrefix;
  final int pcmChunkSizeBytes;
  final int outputSeconds;
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
    LockProvider lockProvider,
    MixerFactory mixerFactory,
    NexusEntityStore store,
    NotificationProvider notification,
    SegmentManager segmentManager,
    TelemetryProvider telemetryProvider,
    @Value("${dub.cycle.millis}") long dubCycleMillis,
    @Value("${environment}") String environment,
    @Value("${input.mode}") String inputMode,
    @Value("${input.template.key}") String inputTemplateKey,
    @Value("${json.expires.in.seconds}") int jsonExpiresInSeconds,
    @Value("${mixer.timeline.seconds}") int mixerSeconds,
    @Value("${output.file.mode}") String outputFileMode,
    @Value("${output.file.number.digits}") int outputFileNumberDigits,
    @Value("${output.json.enabled}") boolean isJsonOutputEnabled,
    @Value("${output.mode}") String outputMode,
    @Value("${output.path.prefix}") String outputPathPrefix,
    @Value("${output.pcm.chunk.size.bytes}") int pcmChunkSizeBytes,
    @Value("${output.seconds}") int outputSeconds,
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
    this.lockProvider = lockProvider;
    this.mixerFactory = mixerFactory;
    this.store = store;
    this.notification = notification;
    this.segmentManager = segmentManager;
    this.telemetryProvider = telemetryProvider;
    this.dubCycleMillis = dubCycleMillis;
    this.environment = environment;
    this.inputMode = inputMode;
    this.inputTemplateKey = inputTemplateKey;
    this.jsonExpiresInSeconds = jsonExpiresInSeconds;
    this.mixerSeconds = mixerSeconds;
    this.outputFileMode = outputFileMode;
    this.outputFileNumberDigits = outputFileNumberDigits;
    this.isJsonOutputEnabled = isJsonOutputEnabled;
    this.outputMode = outputMode;
    this.outputPathPrefix = outputPathPrefix;
    this.pcmChunkSizeBytes = pcmChunkSizeBytes;
    this.outputSeconds = outputSeconds;
    this.cycleAudioBytes = cycleAudioBytes;
    this.shipCycleMillis = shipCycleMillis;
    this.planAheadSeconds = planAheadSeconds;
    this.tempFilePathPrefix = tempFilePathPrefix;
  }

  @Override
  public void start(Runnable onDone) {
    craftWork = new CraftWorkImpl(
      craftFactory,
      entityFactory,
      fabricatorFactory,
      fileStore,
      httpClientProvider,
      hubClient,
      jsonapiPayloadFactory,
      jsonProvider,
      lockProvider,
      store,
      notification,
      segmentManager,
      telemetryProvider,
      inputMode,
      outputMode,
      inputTemplateKey,
      environment,
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
      outputMode,
      outputFileMode,
      outputSeconds,
      shipCycleMillis,
      cycleAudioBytes,
      planAheadSeconds,
      outputPathPrefix,
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
    }

    onDone.run();
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
}
