// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.HubConfiguration;
import io.xj.hub.HubContent;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.util.StringUtils;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.audio_cache.AudioCache;
import io.xj.nexus.audio_cache.AudioCacheImpl;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.craft.CraftFactoryImpl;
import io.xj.nexus.entity.EntityFactory;
import io.xj.nexus.entity.EntityFactoryImpl;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.fabricator.FabricatorFactoryImpl;
import io.xj.nexus.http.HttpClientProvider;
import io.xj.nexus.http.HttpClientProviderImpl;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.hub_client.HubClientImpl;
import io.xj.nexus.hub_client.HubContentProvider;
import io.xj.nexus.hub_client.HubTopology;
import io.xj.nexus.json.JsonProvider;
import io.xj.nexus.json.JsonProviderImpl;
import io.xj.nexus.jsonapi.JsonapiPayloadFactory;
import io.xj.nexus.jsonapi.JsonapiPayloadFactoryImpl;
import io.xj.nexus.mixer.EnvelopeProvider;
import io.xj.nexus.mixer.EnvelopeProviderImpl;
import io.xj.nexus.mixer.MixerFactory;
import io.xj.nexus.mixer.MixerFactoryImpl;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.NexusEntityStoreImpl;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.persistence.SegmentManagerImpl;
import io.xj.nexus.ship.broadcast.BroadcastFactory;
import io.xj.nexus.ship.broadcast.BroadcastFactoryImpl;
import io.xj.nexus.telemetry.Telemetry;
import io.xj.nexus.telemetry.TelemetryImpl;
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

import static io.xj.hub.util.StringUtils.formatStackTrace;
import static io.xj.nexus.mixer.FixedSampleBits.FIXED_SAMPLE_BITS;

public class WorkManagerImpl implements WorkManager {
  private static final Logger LOG = LoggerFactory.getLogger(WorkManagerImpl.class);
  private final BroadcastFactory broadcastFactory;
  private final CraftFactory craftFactory;
  private final AudioCache audioCache;
  private final FabricatorFactory fabricatorFactory;
  private final HubClient hubClient;
  private final MixerFactory mixerFactory;
  private final NexusEntityStore store;
  private final SegmentManager segmentManager;
  private final Telemetry telemetry;
  private final AtomicReference<HubContent> hubContent = new AtomicReference<>();
  private final AtomicReference<WorkState> state = new AtomicReference<>(WorkState.Standby);
  private final AtomicBoolean isAudioLoaded = new AtomicBoolean(false);
  private final AtomicLong startedAtMillis = new AtomicLong(0);

  @Nullable
  private ScheduledExecutorService controlScheduler;

  @Nullable
  private ScheduledExecutorService craftScheduler;

  @Nullable
  private ScheduledExecutorService dubScheduler;

  @Nullable
  private ScheduledExecutorService shipScheduler;

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
    Telemetry telemetry,
    BroadcastFactory broadcastFactory,
    CraftFactory craftFactory,
    AudioCache audioCache,
    FabricatorFactory fabricatorFactory,
    HubClient hubClient,
    MixerFactory mixerFactory,
    NexusEntityStore store,
    SegmentManager segmentManager
  ) {
    this.broadcastFactory = broadcastFactory;
    this.craftFactory = craftFactory;
    this.audioCache = audioCache;
    this.fabricatorFactory = fabricatorFactory;
    this.hubClient = hubClient;
    this.mixerFactory = mixerFactory;
    this.store = store;
    this.segmentManager = segmentManager;
    this.telemetry = telemetry;
  }

  public static WorkManager createInstance() {
    BroadcastFactory broadcastFactory = new BroadcastFactoryImpl();
    Telemetry telemetry = new TelemetryImpl();
    CraftFactory craftFactory = new CraftFactoryImpl();
    HttpClientProvider httpClientProvider = new HttpClientProviderImpl();
    AudioCache audioCache = new AudioCacheImpl(httpClientProvider);
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
    MixerFactory mixerFactory = new MixerFactoryImpl(envelopeProvider, audioCache);
    HubClient hubClient = new HubClientImpl(httpClientProvider, jsonProvider, jsonapiPayloadFactory);
    HubTopology.buildHubApiTopology(entityFactory);
    NexusTopology.buildNexusApiTopology(entityFactory);
    return new WorkManagerImpl(
      telemetry,
      broadcastFactory,
      craftFactory,
      audioCache,
      fabricatorFactory,
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

    audioCache.initialize(
      workConfig.getContentStoragePathPrefix(),
      hubConfig.getAudioBaseUrl(),
      workConfig.getOutputFrameRate(),
      FIXED_SAMPLE_BITS,
      workConfig.getOutputChannels()
    );

    startedAtMillis.set(System.currentTimeMillis());
    isAudioLoaded.set(false);
    updateState(WorkState.Starting);

    controlScheduler = Executors.newScheduledThreadPool(1);
    controlScheduler.scheduleWithFixedDelay(this::runControlCycle, 0, workConfig.getControlCycleDelayMillis(), TimeUnit.MILLISECONDS);

    craftScheduler = Executors.newScheduledThreadPool(1);
    craftScheduler.scheduleWithFixedDelay(this::runCraftCycle, 0, workConfig.getCraftCycleDelayMillis(), TimeUnit.MILLISECONDS);

    dubScheduler = Executors.newScheduledThreadPool(1);
    dubScheduler.scheduleAtFixedRate(this::runDubCycle, 0, workConfig.getDubCycleRateMillis(), TimeUnit.MILLISECONDS);

    shipScheduler = Executors.newScheduledThreadPool(1);
    shipScheduler.scheduleAtFixedRate(this::runShipCycle, 0, workConfig.getShipCycleRateMillis(), TimeUnit.MILLISECONDS);

    telemetry.startTimer();
  }

  @Override
  public void finish(boolean cancelled) {
    if (Objects.nonNull(controlScheduler)) {
      controlScheduler.shutdown();
    }

    if (Objects.nonNull(craftScheduler)) {
      craftScheduler.shutdown();
    }

    if (Objects.nonNull(dubScheduler)) {
      dubScheduler.shutdown();
    }

    if (Objects.nonNull(shipScheduler)) {
      shipScheduler.shutdown();
    }

    // Shutting down ship work will cascade-send the finish() instruction to dub and ship
    if (Objects.nonNull(shipWork)) {
      shipWork.finish();
    }

    updateState(cancelled ? WorkState.Cancelled : WorkState.Done);
    if (Objects.nonNull(afterFinished)) {
      afterFinished.run();
    }

    audioCache.invalidateAll();
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
  public void gotoMacroProgram(Program macroProgram) {
    assert Objects.nonNull(craftWork);
    assert Objects.nonNull(dubWork);
    craftWork.gotoMacroProgram(macroProgram, dubWork.getDubbedToChainMicros().orElse(0L));
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

  /**
   Update the current work state

   @param workState work state
   */
  private void updateState(WorkState workState) {
    state.set(workState);
    if (Objects.nonNull(onStateChange)) {
      onStateChange.accept(workState);
    }
  }

  /**
   Run the control cycle, which prepares fabrication and moves the machine into the active state
   */
  private void runControlCycle() {
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

        case Active, Standby, Done, Failed -> {
          // no op
        }
      }

    } catch (Exception e) {
      didFailWhile("running work cycle", e);
    }
  }

  /**
   Run the craft cycle
   */
  private void runCraftCycle() {
    if (!Objects.equals(state.get(), WorkState.Active)) return;
    assert Objects.nonNull(workConfig);
    assert Objects.nonNull(craftWork);
    assert Objects.nonNull(shipWork);
    assert Objects.nonNull(dubWork);
    craftWork.runCycle(
      shipWork.getShippedToChainMicros().orElse(0L),
      dubWork.getDubbedToChainMicros().orElse(0L)
    );
  }

  /**
   Run the dub cycle
   */
  private void runDubCycle() {
    if (!Objects.equals(state.get(), WorkState.Active)) return;
    assert Objects.nonNull(workConfig);
    assert Objects.nonNull(dubWork);
    assert Objects.nonNull(shipWork);
    dubWork.runCycle(shipWork.getShippedToChainMicros().orElse(0L));
  }

  /**
   Run the ship cycle
   */
  private void runShipCycle() {
    if (!Objects.equals(state.get(), WorkState.Active)) return;
    assert Objects.nonNull(workConfig);
    assert Objects.nonNull(shipWork);
    shipWork.runCycle();
    if (shipWork.isFinished()) {
      updateState(WorkState.Done);
      LOG.info("Fabrication work done");
    }
  }

  /**
   Update the progress

   @param progress progress
   */
  private void updateProgress(float progress) {
    if (Objects.nonNull(onProgress)) {
      onProgress.accept(progress);
    }
  }

  /**
   Start loading content
   */
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

  /**
   @return true if content is loaded
   */
  private boolean isContentLoaded() {
    return Objects.nonNull(hubContent.get());
  }

  /**
   Start loading audio
   */
  private void startLoadingAudio() {
    assert Objects.nonNull(workConfig);
    assert Objects.nonNull(hubConfig);

    int loaded = 0;

    try {
      var instruments = new ArrayList<>(hubContent.get().getInstruments());
      var audios = new ArrayList<>(hubContent.get().getInstrumentAudios());
      for (Instrument instrument : instruments) {
        for (InstrumentAudio audio : audios.stream()
          .filter(a -> Objects.equals(a.getInstrumentId(), instrument.getId()))
          .sorted(Comparator.comparing(InstrumentAudio::getName))
          .toList()) {
          if (!Objects.equals(state.get(), WorkState.PreparingAudio)) {
            // Workstation canceling preloading should cease resampling audio files https://www.pivotaltracker.com/story/show/186209135
            return;
          }
          if (!StringUtils.isNullOrEmpty(audio.getWaveformKey())) {
            audioCache.prepare(audio);
            updateProgress((float) loaded / audios.size());
            loaded++;
          }
        }
      }
      if (Objects.nonNull(onProgress)) onProgress.accept(1.0f);
      isAudioLoaded.set(true);
      LOG.info("Preloaded {} audios from {} instruments", loaded, instruments.size());

    } catch (Exception e) {
      didFailWhile("preloading audio", e);
    }
  }

  /**
   @return true if audio is loaded
   */
  private boolean isAudioLoaded() {
    return isAudioLoaded.get();
  }

  /**
   Initialize the work
   */
  private void initialize() {
    assert Objects.nonNull(hubConfig);
    assert Objects.nonNull(workConfig);
    craftWork = new CraftWorkImpl(
      telemetry,
      craftFactory,
      fabricatorFactory,
      segmentManager,
      store,
      audioCache,
      hubContent.get(),
      workConfig.getPersistenceWindowSeconds(),
      workConfig.getCraftAheadSeconds(),
      workConfig.getMixerLengthSeconds(),
      workConfig.getOutputFrameRate(),
      workConfig.getOutputChannels());
    dubWork = new DubWorkImpl(
      telemetry,
      craftWork,
      mixerFactory,
      hubConfig.getAudioBaseUrl(),
      workConfig.getContentStoragePathPrefix(),
      workConfig.getMixerLengthSeconds(),
      workConfig.getDubAheadSeconds(),
      workConfig.getOutputFrameRate(),
      workConfig.getOutputChannels()
    );
    shipWork = new ShipWorkImpl(
      telemetry,
      dubWork,
      broadcastFactory
    );
  }

  /**
   @return true if initialized
   */
  private boolean isInitialized() {
    return Objects.nonNull(craftWork) && Objects.nonNull(dubWork) && Objects.nonNull(shipWork);
  }

  /**
   Log and of segment message of error that job failed while (message)

   @param msgWhile phrased like "Doing work"
   @param e        exception (optional)
   */
  void didFailWhile(String msgWhile, Exception e) {
    LOG.error("Failed while {}: {}", msgWhile, formatStackTrace(e), e);
    // This will cascade-send the finish() instruction to dub and ship
    if (Objects.nonNull(shipWork)) {
      shipWork.finish();
    }
    updateState(WorkState.Failed);
  }
}
