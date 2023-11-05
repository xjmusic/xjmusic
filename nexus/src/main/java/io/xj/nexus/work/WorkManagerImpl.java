// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.HubConfiguration;
import io.xj.hub.HubContent;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.util.StringUtils;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.filestore.FileStoreProvider;
import io.xj.lib.mixer.MixerFactory;
import io.xj.lib.telemetry.MultiStopwatch;
import io.xj.nexus.OutputMode;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.dub.DubAudioCache;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.hub_client.HubContentProvider;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.persistence.SegmentManager;
import io.xj.nexus.ship.broadcast.BroadcastFactory;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

@Service
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
  private final int mixerSeconds;
  private final int outputFileNumberDigits;
  private final int pcmChunkSizeBytes;
  private final int cycleAudioBytes;
  private final long cycleMillis;
  private final long craftCycleMillis;
  private long nextCraftCycleMillis;
  private final long dubCycleMillis;
  private long nextDubCycleMillis;
  private final long shipCycleMillis;
  private long nextShipCycleMillis;
  private final long reportCycleMillis;
  private long nextReportCycleMillis;

  private final String tempFilePathPrefix;
  private final AtomicReference<HubContent> hubContent = new AtomicReference<>();
  private final AtomicReference<WorkState> state = new AtomicReference<>(WorkState.Standby);
  private final AtomicBoolean isAudioLoaded = new AtomicBoolean(false);
  private final AtomicLong startedAtMillis = new AtomicLong(0);

  private boolean isFileOutputMode;

  private MultiStopwatch timer;

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
    BroadcastFactory broadcastFactory,
    CraftFactory craftFactory,
    DubAudioCache dubAudioCache,
    EntityFactory entityFactory,
    FabricatorFactory fabricatorFactory,
    FileStoreProvider fileStore,
    HubClient hubClient,
    MixerFactory mixerFactory,
    NexusEntityStore store,
    SegmentManager segmentManager,
    @Value("${mixer.timeline.seconds}") int mixerSeconds,
    @Value("${output.file.number.digits}") int outputFileNumberDigits,
    @Value("${output.pcm.chunk.size.bytes}") int pcmChunkSizeBytes,
    @Value("${ship.cycle.audio.bytes}") int cycleAudioBytes,
    @Value("${temp.file.path.prefix}") String tempFilePathPrefix,
    @Value("${ship.cycle.millis}") long shipCycleMillis,
    @Value("${dub.cycle.millis}") long dubCycleMillis,
    @Value("${craft.cycle.millis}") long craftCycleMillis
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
    this.mixerSeconds = mixerSeconds;
    this.outputFileNumberDigits = outputFileNumberDigits;
    this.pcmChunkSizeBytes = pcmChunkSizeBytes;
    this.cycleAudioBytes = cycleAudioBytes;
    this.tempFilePathPrefix = tempFilePathPrefix;
    this.shipCycleMillis = shipCycleMillis;
    this.dubCycleMillis = dubCycleMillis;
    this.craftCycleMillis = craftCycleMillis;

    timer = MultiStopwatch.start();

    cycleMillis = Math.min(dubCycleMillis, Math.min(shipCycleMillis, craftCycleMillis));
    reportCycleMillis = Math.max(dubCycleMillis, Math.max(shipCycleMillis, craftCycleMillis));
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
    scheduler.scheduleAtFixedRate(this::runCycle, 0, cycleMillis, TimeUnit.MILLISECONDS);

    timer = MultiStopwatch.start();
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

    timer.stop();
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
      craftFactory,
      entityFactory,
      fabricatorFactory,
      segmentManager,
      fileStore,
      store, hubContent.get(),
      workConfig.getInputMode(),
      workConfig.getOutputMode(),
      hubConfig.getAudioBaseUrl(),
      hubConfig.getShipBaseUrl(),
      tempFilePathPrefix,
      workConfig.getOutputFrameRate(),
      workConfig.getOutputChannels()
    );
    dubWork = new DubWorkImpl(
      craftWork,
      dubAudioCache,
      mixerFactory,
      workConfig.getContentStoragePathPrefix(),
      hubConfig.getAudioBaseUrl(),
      mixerSeconds,
      workConfig.getOutputFrameRate(),
      workConfig.getOutputChannels()
    );
    shipWork = new ShipWorkImpl(
      dubWork,
      broadcastFactory,
      workConfig.getOutputMode(),
      workConfig.getOutputFileMode(),
      workConfig.getOutputSeconds(),
      cycleAudioBytes,
      workConfig.getInputTemplateKey(),
      workConfig.getOutputPathPrefix(),
      outputFileNumberDigits,
      pcmChunkSizeBytes
    );
  }

  private void runFabricationCycle() {
    assert Objects.nonNull(workConfig);
    assert Objects.nonNull(shipWork);
    assert Objects.nonNull(dubWork);
    assert Objects.nonNull(craftWork);

    long now = System.currentTimeMillis();

    // Ship
    timer.section("Ship");
    if (now >= nextShipCycleMillis) {
      nextShipCycleMillis = now + shipCycleMillis;
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
    timer.section("Dub");
    if (now >= nextDubCycleMillis) {
      nextDubCycleMillis = now + dubCycleMillis;
      dubWork.runCycle(shipWork.getShippedToChainMicros().map(m -> m +workConfig.getDubAheadMicros()).orElse(0L));
    }

    // Craft
    timer.section("Craft");
    if (Objects.nonNull(craftWork) && now >= nextCraftCycleMillis) {
      nextCraftCycleMillis = now + craftCycleMillis;
      craftWork.runCycle(shipWork.getShippedToChainMicros().map(m -> m +workConfig.getCraftAheadMicros()).orElse(0L));
    }

    // End lap & report if needed
    timer.section("Standby");
    timer.lap();
    LOG.debug("Lap time: {}", timer.lapToString());
    timer.clearLapSections();
    if (now >= nextReportCycleMillis) {
      nextReportCycleMillis = now + reportCycleMillis;
      LOG.info("Fabrication time: {}", timer.getTotalText());
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
