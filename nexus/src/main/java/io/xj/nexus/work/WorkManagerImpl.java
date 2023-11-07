// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.HubConfiguration;
import io.xj.hub.HubContent;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.util.StringUtils;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityFactoryImpl;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.filestore.FileStoreProviderImpl;
import io.xj.lib.http.HttpClientProvider;
import io.xj.lib.http.HttpClientProviderImpl;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.lib.mixer.EnvelopeProvider;
import io.xj.lib.mixer.EnvelopeProviderImpl;
import io.xj.lib.mixer.MixerFactory;
import io.xj.lib.mixer.MixerFactoryImpl;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.OutputMode;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.dub.DubAudioCache;
import io.xj.nexus.dub.DubAudioCacheImpl;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.hub_client.*;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.SegmentManagerImpl;
import io.xj.nexus.ship.broadcast.BroadcastFactory;
import io.xj.nexus.ship.broadcast.BroadcastFactoryImpl;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static io.xj.nexus.mixer.FixedSampleBits.FIXED_SAMPLE_BITS;

public class WorkManagerImpl implements WorkManager {
  private static final Logger LOG = LoggerFactory.getLogger(WorkManagerImpl.class);
  private final BroadcastFactory broadcastFactory;
  private final CraftFactory craftFactory;
  private final DubAudioCache dubAudioCache;
  private final EntityFactory entityFactory;
  private final FabricatorFactory fabricatorFactory;
  private final FileStoreProvider fileStore;
  private final HubClient hubClient;
  private final MixerFactory mixerFactory;
  private final NexusEntityStore store;
  private final SegmentManager segmentManager;
  private final WorkTelemetry telemetry;
  private long nextCraftCycleMillis;
  private long nextDubCycleMillis;
  private long nextShipCycleMillis;
  private long nextReportCycleMillis;

  private final AtomicReference<HubContent> hubContent = new AtomicReference<>();
  private final AtomicReference<WorkState> state = new AtomicReference<>(WorkState.Standby);
  private final AtomicBoolean isAudioLoaded = new AtomicBoolean(false);
  private final AtomicLong startedAtMillis = new AtomicLong(0);

  private boolean isFileOutputMode;

  @Nullable
  private ScheduledExecutorService scheduler;

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
  private HubClientAccess hubAccess;


  @Nullable
  private Consumer<Float> onProgress;

  @Nullable
  private Consumer<WorkState> onStateChange;

  @Nullable
  private Runnable afterFinished;

  public WorkManagerImpl(
    WorkTelemetry workTelemetry,
    BroadcastFactory broadcastFactory,
    CraftFactory craftFactory,
    DubAudioCache dubAudioCache,
    EntityFactory entityFactory,
    FabricatorFactory fabricatorFactory,
    FileStoreProvider fileStore,
    HubClient hubClient,
    MixerFactory mixerFactory,
    NexusEntityStore store,
    SegmentManager segmentManager
  ) {
    this.broadcastFactory = broadcastFactory;
    this.craftFactory = craftFactory;
    this.dubAudioCache = dubAudioCache;
    this.entityFactory = entityFactory;
    this.fabricatorFactory = fabricatorFactory;
    this.fileStore = fileStore;
    this.hubClient = hubClient;
    this.mixerFactory = mixerFactory;
    this.store = store;
    this.segmentManager = segmentManager;
    this.telemetry = workTelemetry;
  }

  public static WorkManager createInstance() {
    FileStoreProvider fileStore = new FileStoreProviderImpl();
    BroadcastFactory broadcastFactory = new BroadcastFactoryImpl();
    WorkTelemetry workTelemetry = new WorkTelemetryImpl();
    CraftFactory craftFactory = new CraftFactoryImpl();
    HttpClientProvider httpClientProvider = new HttpClientProviderImpl();
    DubAudioCache dubAudioCache = new DubAudioCacheImpl(httpClientProvider);
    JsonProvider jsonProvider = new JsonProviderImpl();
    EntityFactory entityFactory = new EntityFactoryImpl(jsonProvider);
    NexusEntityStore nexusEntityStore = new NexusEntityStoreImpl(entityFactory);
    SegmentManager segmentManager = new SegmentManagerImpl(nexusEntityStore);
    JsonapiPayloadFactory jsonapiPayloadFactory = new JsonapiPayloadFactoryImpl(entityFactory);
    FabricatorFactory fabricatorFactory = new FabricatorFactoryImpl(
      segmentManager,
      jsonapiPayloadFactory,
      jsonProvider
    );
    EnvelopeProvider envelopeProvider = new EnvelopeProviderImpl();
    MixerFactory mixerFactory = new MixerFactoryImpl(envelopeProvider);
    HubClient hubClient = new HubClientImpl(httpClientProvider, jsonProvider, jsonapiPayloadFactory);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);
    return new WorkManagerImpl(
      workTelemetry,
      broadcastFactory,
      craftFactory,
      dubAudioCache,
      entityFactory,
      fabricatorFactory,
      fileStore,
      hubClient,
      mixerFactory,
      nexusEntityStore,
      segmentManager
    );
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

    startedAtMillis.set(System.currentTimeMillis());
    isFileOutputMode = workConfig.getOutputMode() == OutputMode.FILE;
    isAudioLoaded.set(false);
    updateState(WorkState.Starting);

    scheduler = Executors.newScheduledThreadPool(1);
    scheduler.scheduleAtFixedRate(this::runCycle, 0, workConfig.getCycleMillis(), TimeUnit.MILLISECONDS);

    telemetry.startTimer();
  }

  private void updateState(WorkState workState) {
    state.set(workState);
    if (Objects.nonNull(onStateChange)) {
      onStateChange.accept(workState);
    }
  }

  @Override
  public void finish(boolean cancelled) {
    if (Objects.nonNull(scheduler)) {
      scheduler.shutdown();
    }

    // Shutting down ship work will cascade-send the finish() instruction to dub and ship
    if (Objects.nonNull(shipWork)) {
      shipWork.finish();
    }

    updateState(cancelled ? WorkState.Cancelled : WorkState.Done);
    if (Objects.nonNull(afterFinished)) {
      afterFinished.run();
    }

    telemetry.stopTimer();
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
  public void setOnProgress(@Nullable Consumer<Float> onProgress) {
    this.onProgress = onProgress;
  }

  @Override
  public void setOnStateChange(@Nullable Consumer<WorkState> onStateChange) {
    this.onStateChange = onStateChange;
  }

  @Override
  public void setAfterFinished(@Nullable Runnable afterFinished) {
    this.afterFinished = afterFinished;
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
    try {
      switch (state.get()) {

        case Starting -> {
          updateState(WorkState.LoadingContent);
          startLoadingContent();
        }

        case LoadingContent -> {
          if (isContentLoaded()) {
            updateState(WorkState.LoadedContent);
          }
        }

        case LoadedContent -> {
          updateState(WorkState.PreparingAudio);
          startLoadingAudio();
        }

        case PreparingAudio -> {
          if (isAudioLoaded()) {
            updateState(WorkState.PreparedAudio);
          }
        }

        case PreparedAudio -> {
          updateState(WorkState.Initializing);
          initialize();
        }

        case Initializing -> {
          if (isInitialized()) {
            updateState(WorkState.Active);
          }
        }

        case Active -> runFabricationCycle();

        case Standby, Done, Failed -> {
          // no op
        }
      }

    } catch (Exception e) {
      didFailWhile("running work cycle", e);
    }
  }

  private void updateProgress(float progress) {
    if (Objects.nonNull(onProgress)) {
      onProgress.accept(progress);
    }
  }

  private void startLoadingContent() {
    assert Objects.nonNull(workConfig);
    hubContent.set(null);

    Callable<HubContent> hubContentProvider = new HubContentProvider(
      hubClient,
      hubConfig,
      hubAccess,
      workConfig.getInputMode(),
      workConfig.getInputTemplateKey()
    );

    try {
      hubContent.set(hubContentProvider.call());
    } catch (Exception e) {
      didFailWhile("loading content", e);
    }
  }

  private boolean isContentLoaded() {
    return Objects.nonNull(hubContent.get());
  }

  private void startLoadingAudio() {
    assert Objects.nonNull(workConfig);
    assert Objects.nonNull(hubConfig);

    var contentStoragePathPrefix = workConfig.getContentStoragePathPrefix();
    var audioBaseUrl = hubConfig.getAudioBaseUrl();
    var outputFrameRate = (int) workConfig.getOutputFrameRate();
    var outputChannels = workConfig.getOutputChannels();

    int loaded = 0;

    try {
      var instruments = new ArrayList<>(hubContent.get().getInstruments());
      var audios = new ArrayList<>(hubContent.get().getInstrumentAudios());
      for (Instrument instrument : instruments) {
        for (InstrumentAudio audio : audios.stream()
          .filter(a -> Objects.equals(a.getInstrumentId(), instrument.getId()))
          .sorted(Comparator.comparing(InstrumentAudio::getName))
          .toList()) {
          if (isFinished()) {
            return;
          }
          if (!StringUtils.isNullOrEmpty(audio.getWaveformKey())) {
            dubAudioCache.load(
              contentStoragePathPrefix,
              audioBaseUrl,
              audio.getInstrumentId(),
              audio.getWaveformKey(),
              outputFrameRate,
              FIXED_SAMPLE_BITS,
              outputChannels);
            updateProgress((float) loaded / audios.size());
            loaded++;
          }
        }
      }
      if (Objects.nonNull(onProgress)) onProgress.accept(1.0f);
      isAudioLoaded.set(true);
      LOG.info("Preloaded {}  audios from {} instruments", loaded, instruments.size());

    } catch (Exception e) {
      didFailWhile("preloading audio", e);
    }
  }

  private boolean isAudioLoaded() {
    return isAudioLoaded.get();
  }

  private void initialize() {
    assert Objects.nonNull(hubConfig);
    assert Objects.nonNull(workConfig);
    craftWork = new CraftWorkImpl(
      telemetry, craftFactory,
      entityFactory,
      fabricatorFactory,
      segmentManager,
      fileStore,
      store, hubContent.get(),
      workConfig.getInputMode(),
      workConfig.getOutputMode(),
      hubConfig.getAudioBaseUrl(),
      hubConfig.getShipBaseUrl(),
      workConfig.getTempFilePathPrefix(),
      workConfig.getOutputFrameRate(),
      workConfig.getOutputChannels()
    );
    dubWork = new DubWorkImpl(
      telemetry, craftWork,
      dubAudioCache,
      mixerFactory,
      workConfig.getContentStoragePathPrefix(),
      hubConfig.getAudioBaseUrl(),
      workConfig.getMixBufferLengthSeconds(),
      workConfig.getOutputFrameRate(),
      workConfig.getOutputChannels()
    );
    shipWork = new ShipWorkImpl(
      telemetry, dubWork,
      broadcastFactory,
      workConfig.getOutputMode(),
      workConfig.getOutputFileMode(),
      workConfig.getOutputSeconds(),
      workConfig.getShipCycleAudioBytes(),
      workConfig.getInputTemplateKey(),
      workConfig.getOutputPathPrefix(),
      workConfig.getShipOutputFileNumberDigits(),
      workConfig.getShipOutputPcmChunkSizeBytes()
    );
  }

  private void runFabricationCycle() {
    assert Objects.nonNull(workConfig);
    assert Objects.nonNull(shipWork);
    assert Objects.nonNull(dubWork);
    assert Objects.nonNull(craftWork);

    long now = System.currentTimeMillis();

    // Ship
    if (now >= nextShipCycleMillis) {
      nextShipCycleMillis = now + workConfig.getShipCycleMillis();
      shipWork.runCycle(0);
      if (isFileOutputMode) {
        updateProgress(shipWork.getProgress());
      }
      if (shipWork.isFinished()) {
        updateState(WorkState.Done);
        LOG.info("Fabrication work done");
      }
    }

    // Dub
    if (now >= nextDubCycleMillis) {
      nextDubCycleMillis = now + workConfig.getDubCycleMillis();
      dubWork.runCycle(shipWork.getShippedToChainMicros().map(m -> m + workConfig.getDubAheadMicros()).orElse(0L));
    }

    // Craft
    if (Objects.nonNull(craftWork) && now >= nextCraftCycleMillis) {
      nextCraftCycleMillis = now + workConfig.getCraftCycleMillis();
      craftWork.runCycle(shipWork.getShippedToChainMicros().map(m -> m + workConfig.getCraftAheadMicros()).orElse(0L));
    }

    // End lap & report if needed
    var lapText = telemetry.markLap();
    LOG.debug("Lap time: {}", lapText);
    if (now >= nextReportCycleMillis) {
      nextReportCycleMillis = now + workConfig.getReportCycleMillis();
      telemetry.report();
    }
  }

  private boolean isInitialized() {
    return Objects.nonNull(craftWork) && Objects.nonNull(dubWork) && Objects.nonNull(shipWork);
  }

  /**
   Log and of segment message of error that job failed while (message)

   @param msgWhile phrased like "Doing work"
   @param e        exception (optional)
   */
  void didFailWhile(String msgWhile, Exception e) {
    LOG.error("Failed while {}", msgWhile, e);
    // This will cascade-send the finish() instruction to dub and ship
    if (Objects.nonNull(shipWork)) {
      shipWork.finish();
    }
    updateState(WorkState.Failed);
  }
}
