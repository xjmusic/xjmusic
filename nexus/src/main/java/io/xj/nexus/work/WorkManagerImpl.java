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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
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
  private final long dubCycleMillis;
  private final int mixerSeconds;
  private final int outputFileNumberDigits;
  private final int pcmChunkSizeBytes;
  private final int cycleAudioBytes;
  private final long shipCycleMillis;
  private final String tempFilePathPrefix;
  private final ExecutorService executor;
  private boolean isFileOutputMode;
  private final AtomicReference<WorkState> state = new AtomicReference<>(WorkState.Standby);
  private final AtomicBoolean isAudioLoaded = new AtomicBoolean(false);
  private int engineCylinder = 0;

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

  private final AtomicReference<HubContent> hubContent = new AtomicReference<>();

  @Nullable
  private Consumer<Float> onProgress;

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
    @Value("${dub.cycle.millis}") long dubCycleMillis,
    @Value("${mixer.timeline.seconds}") int mixerSeconds,
    @Value("${output.file.number.digits}") int outputFileNumberDigits,
    @Value("${output.pcm.chunk.size.bytes}") int pcmChunkSizeBytes,
    @Value("${ship.cycle.audio.bytes}") int cycleAudioBytes,
    @Value("${ship.cycle.millis}") long shipCycleMillis,
    @Value("${temp.file.path.prefix}") String tempFilePathPrefix,
    @Value("${thread.pool.size}") int threadPoolSize
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
    this.dubCycleMillis = dubCycleMillis;
    this.mixerSeconds = mixerSeconds;
    this.outputFileNumberDigits = outputFileNumberDigits;
    this.pcmChunkSizeBytes = pcmChunkSizeBytes;
    this.cycleAudioBytes = cycleAudioBytes;
    this.shipCycleMillis = shipCycleMillis;
    this.tempFilePathPrefix = tempFilePathPrefix;

    executor = Executors.newFixedThreadPool(threadPoolSize);
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

    isFileOutputMode = workConfig.getOutputMode() == OutputMode.FILE;
    isAudioLoaded.set(false);
    state.set(WorkState.Starting);
  }

  @Override
  public void finish() {
    // This will cascade-send the finish() instruction to dub and ship
    if (Objects.nonNull(shipWork)) {
      shipWork.finish();
    }
    state.set(WorkState.Done);
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
          state.set(WorkState.LoadingContent);
          startLoadingContent();
          LOG.info("Fabrication work starting");
        }

        case LoadingContent -> {
          if (isContentLoaded()) {
            state.set(WorkState.LoadedContent);
            LOG.info("Fabrication work loaded content");
          }
        }

        case LoadedContent -> {
          state.set(WorkState.LoadingAudio);
          startLoadingAudio();
          LOG.info("Fabrication work loading audio");
        }

        case LoadingAudio -> {
          if (isAudioLoaded()) {
            state.set(WorkState.LoadedAudio);
            LOG.info("Fabrication work loaded audio");
          }
        }

        case LoadedAudio -> {
          state.set(WorkState.Initializing);
          initialize();
          LOG.info("Fabrication work initialized");
        }

        case Initializing -> {
          if (isInitialized()) {
            state.set(WorkState.Active);
            LOG.info("Fabrication work active");
          }
        }

        case Active -> fireActiveEngineCylinder();

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

    executor.submit(() -> {
      try {
        HubContent content = hubContentProvider.call();
        hubContent.set(content);
      } catch (Exception e) {
        didFailWhile("loading content", e);
      }
    });
  }

  private boolean isContentLoaded() {
    return Objects.nonNull(hubContent.get());
  }

  private void startLoadingAudio() {
    assert Objects.nonNull(workConfig);
    assert Objects.nonNull(hubConfig);

    ExecutorService executor = Executors.newSingleThreadExecutor();
    executor.submit(new AudioPreloader(
      workConfig.getContentStoragePathPrefix(),
      hubConfig.getAudioBaseUrl(),
      (int) workConfig.getOutputFrameRate(),
      workConfig.getOutputChannels()
    ));
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
      segmentManager, fileStore,
      store, hubContent.get(),
      workConfig.getInputMode(),
      workConfig.getOutputMode(),
      hubConfig.getAudioBaseUrl(),
      hubConfig.getShipBaseUrl(),
      tempFilePathPrefix,
      workConfig.getOutputFrameRate(),
      workConfig.getOutputChannels(),
      workConfig.getCraftAheadSeconds()
    );
    dubWork = new DubWorkImpl(
      craftWork,
      dubAudioCache,
      mixerFactory,
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

  /**
   The analogy here is an engine with multiple cylinders, where the cylinders (Craft, Dub, Ship) fire in sequence.
   */
  private void fireActiveEngineCylinder() {
    engineCylinder = (engineCylinder + 1) % 3;
    switch (engineCylinder) {

      // Cylinder 0: Craft
      case 0 -> executor.submit(() -> {
        if (Objects.nonNull(craftWork)) craftWork.runCycle();
      });

      // Cylinder 1: Dub
      case 1 -> executor.submit(() -> {
        if (Objects.nonNull(dubWork)) dubWork.runCycle();
      });

      // Cylinder 2: Ship
      case 2 -> executor.submit(() -> {
        if (Objects.nonNull(shipWork)) {
          shipWork.runCycle();
          if (isFileOutputMode) {
            updateProgress(shipWork.getProgress());
          }
          if (shipWork.isFinished()) {
            state.set(WorkState.Done);
            LOG.info("Fabrication work done");
          }
        }
      });
    }
  }

  /**
   Log and of segment message of error that job failed while (message)@param shipKey  (optional) ship key

   @param msgWhile phrased like "Doing work"
   @param e        exception (optional)
   */
  void didFailWhile(String msgWhile, Exception e) {
    LOG.error("Failed while {}", msgWhile, e);
    finish();
  }

  private boolean isInitialized() {
    return Objects.nonNull(craftWork) && Objects.nonNull(dubWork) && Objects.nonNull(shipWork);
  }

  private class AudioPreloader implements Runnable {
    private final String contentStoragePathPrefix;
    private final String audioBaseUrl;
    private final int outputFrameRate;
    private final int outputChannels;

    private AudioPreloader(
      String contentStoragePathPrefix,
      String audioBaseUrl,
      int outputFrameRate,
      int outputChannels
    ) {
      this.contentStoragePathPrefix = contentStoragePathPrefix;
      this.audioBaseUrl = audioBaseUrl;
      this.outputFrameRate = outputFrameRate;
      this.outputChannels = outputChannels;
    }

    @Override
    public void run() {
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
  }
}
