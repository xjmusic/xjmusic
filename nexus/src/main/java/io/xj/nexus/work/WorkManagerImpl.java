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
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.hub_client.access.HubAccess;
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
import java.util.concurrent.atomic.AtomicReference;

@Service
public class WorkManagerImpl implements WorkManager {
  private static final Logger LOG = LoggerFactory.getLogger(WorkManagerImpl.class);
  private final BroadcastFactory broadcastFactory;
  private final CraftFactory craftFactory;
  private final DubAudioCache dubAudioCache;
  private final EntityFactory entityFactory;
  private final FabricatorFactory fabricatorFactory;
  private final FileStoreProvider fileStore;
  private final HttpClientProvider httpClientProvider;
  private final JsonProvider jsonProvider;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final MixerFactory mixerFactory;
  private final NexusEntityStore store;
  private final NotificationProvider notification;
  private final SegmentManager segmentManager;
  private final TelemetryProvider telemetryProvider;
  private final long dubCycleMillis;
  private final int jsonExpiresInSeconds;
  private final int mixerSeconds;
  private final int outputFileNumberDigits;
  private final boolean isJsonOutputEnabled;
  private final int pcmChunkSizeBytes;
  private final int cycleAudioBytes;
  private final long shipCycleMillis;
  private final String tempFilePathPrefix;
  private final AtomicReference<WorkState> state = new AtomicReference<>(WorkState.Initializing);

  @Nullable
  private CraftWork craftWork;

  @Nullable
  private DubWork dubWork;

  @Nullable
  private ShipWork shipWork;

  @Nullable
  private WorkConfiguration workConfig;

  @Nullable
  private HubConfiguration hubConfig;

  @Nullable
  private HubContent hubContent;

  @Nullable
  private HubClientAccess hubAccess;

  public WorkManagerImpl(
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
  public void start(
    WorkConfiguration workConfig,
    HubConfiguration hubConfig,
    HubClientAccess hubAccess
  ) {
    this.workConfig = workConfig;
    this.hubConfig = hubConfig;
    this.hubAccess = hubAccess;
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
  public boolean isFinished() {
    return getWorkState() == WorkState.Done || getWorkState() == WorkState.Failed;
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

  @Override
  public void runCycle() {
    switch (state.get()) {

      case Starting -> {
        startLoadingContent();
        state.set(WorkState.LoadingContent);
        LOG.info("Fabrication work starting");
      }

      case LoadingContent -> {
        if (isContentLoaded()) {
          state.set(WorkState.LoadedContent);
          LOG.info("Fabrication work loaded content");
        }
      }

      case LoadedContent -> {
        startLoadingAudio();
        state.set(WorkState.LoadingAudio);
        LOG.info("Fabrication work loading audio");
      }

      case LoadingAudio -> {
        if (isAudioLoaded()) {
          state.set(WorkState.LoadedAudio);
          LOG.info("Fabrication work loaded audio");
        }
      }

      case LoadedAudio -> {
        initialize();
        state.set(WorkState.Initializing);
        LOG.info("Fabrication work initialized");
      }

      case Initializing -> {
        if (isInitialized()) {
          state.set(WorkState.Active);
          LOG.info("Fabrication work active");
        }
      }

      case Active -> {
        if (Objects.nonNull(shipWork)) {
          shipWork.runCycle();
        }
        if (Objects.nonNull(dubWork)) {
          dubWork.runCycle();
        }
        if (Objects.nonNull(craftWork)) {
          craftWork.runCycle();
        }
        if (isDone()) {
          state.set(WorkState.Done);
          LOG.info("Fabrication work done");
        } else if (isFailed()) {
          state.set(WorkState.Failed);
          LOG.error("Fabrication work failed");
        }
      }

      case Standby, Done, Failed -> {
        // no op
      }
    }
  }

  private void startLoadingContent() {
    // TODO start loading content
    // TODO utilize hubAccess to load content
  }

  private boolean isContentLoaded() {
    // TODO return true if content is loaded
    return false;
  }

  private void startLoadingAudio() {
    // TODO start loading audio
  }

  private boolean isAudioLoaded() {
    // TODO return true if audio is loaded
    return false;
  }

  private void initialize() {
    if (Objects.isNull(hubContent)) {
      didFail("Work configuration is null");
      return;
    } else if (Objects.isNull(workConfig)) {
      didFail("Work configuration is null");
      return;
    } else if (Objects.isNull(hubConfig)) {
      didFail("Hub configuration is null");
      return;
    }

    craftWork = new CraftWorkImpl(
      craftFactory,
      entityFactory,
      fabricatorFactory,
      fileStore,
      store,
      segmentManager,
      hubConfig.getAudioBaseUrl(),
      hubConfig.getShipBaseUrl(),
      workConfig.getInputMode(),
      workConfig.getOutputMode(),
      tempFilePathPrefix,
      workConfig.getOutputFrameRate(),
      workConfig.getOutputChannels(),
      workConfig.getCraftAheadSeconds(),
      hubContent
    );
    dubWork = new DubWorkImpl(
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
      workConfig.getDubAheadSeconds()
    );
    shipWork = new ShipWorkImpl(
      dubWork,
      notification,
      broadcastFactory,
      workConfig.getOutputMode(),
      workConfig.getOutputFileMode(),
      workConfig.getOutputSeconds(),
      shipCycleMillis,
      cycleAudioBytes,
      workConfig.getInputTemplateKey(),
      workConfig.getOutputPathPrefix(),
      outputFileNumberDigits,
      pcmChunkSizeBytes,
      workConfig.getShipAheadSeconds()
    );
  }

  private void didFail(String message) {
    LOG.error("Did fail: {}", message);
    state.set(WorkState.Failed);
  }

  private boolean isInitialized() {
    // TODO return true if initialized
    return false;
  }

  private boolean isDone() {
    // TODO return true if done
    return false;
  }

  private boolean isFailed() {
    // TODO return true if failed
    return false;
  }


}
