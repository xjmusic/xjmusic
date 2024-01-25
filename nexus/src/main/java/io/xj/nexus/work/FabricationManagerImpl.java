// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.work;

import io.xj.hub.HubConfiguration;
import io.xj.hub.HubContent;
import io.xj.hub.TemplateConfig;
import io.xj.hub.meme.MemeTaxonomy;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.hub.tables.pojos.InstrumentAudio;
import io.xj.hub.tables.pojos.Program;
import io.xj.hub.util.StringUtils;
import io.xj.hub.util.ValueException;
import io.xj.nexus.audio_cache.AudioCache;
import io.xj.nexus.craft.CraftFactory;
import io.xj.nexus.fabricator.FabricatorFactory;
import io.xj.nexus.hub_client.HubClient;
import io.xj.nexus.hub_client.HubClientAccess;
import io.xj.nexus.hub_client.HubContentProvider;
import io.xj.nexus.mixer.MixerFactory;
import io.xj.nexus.persistence.NexusEntityStore;
import io.xj.nexus.ship.broadcast.BroadcastFactory;
import io.xj.nexus.telemetry.Telemetry;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static io.xj.hub.util.StringUtils.formatStackTrace;
import static io.xj.nexus.mixer.FixedSampleBits.FIXED_SAMPLE_BITS;

public class FabricationManagerImpl implements FabricationManager {
  private static final Logger LOG = LoggerFactory.getLogger(FabricationManagerImpl.class);
  private final BroadcastFactory broadcastFactory;
  private final CraftFactory craftFactory;
  private final AudioCache audioCache;
  private final FabricatorFactory fabricatorFactory;
  private final HubClient hubClient;
  private final MixerFactory mixerFactory;
  private final NexusEntityStore entityStore;
  private final Telemetry telemetry;
  private final AtomicReference<HubContent> hubContent = new AtomicReference<>();
  private final AtomicReference<FabricationState> state = new AtomicReference<>(FabricationState.Standby);
  private final AtomicBoolean isAudioLoaded = new AtomicBoolean(false);
  private final AtomicLong startedAtMillis = new AtomicLong(0);
  private final AtomicBoolean running = new AtomicBoolean(false);

  @Nullable
  private CraftWork craftWork;

  @Nullable
  private DubWork dubWork;

  @Nullable
  private ShipWork shipWork;

  @Nullable
  private FabricationSettings workConfig;

  @Nullable
  private HubConfiguration hubConfig;

  @Nullable
  private HubClientAccess hubAccess;

  @Nullable
  private Consumer<Float> onProgress;

  @Nullable
  private Consumer<FabricationState> onStateChange;

  @Nullable
  private Runnable afterFinished;

  public FabricationManagerImpl(
    Telemetry telemetry,
    BroadcastFactory broadcastFactory,
    CraftFactory craftFactory,
    AudioCache audioCache,
    FabricatorFactory fabricatorFactory,
    HubClient hubClient,
    MixerFactory mixerFactory,
    NexusEntityStore store
  ) {
    this.broadcastFactory = broadcastFactory;
    this.craftFactory = craftFactory;
    this.audioCache = audioCache;
    this.fabricatorFactory = fabricatorFactory;
    this.hubClient = hubClient;
    this.mixerFactory = mixerFactory;
    this.entityStore = store;
    this.telemetry = telemetry;
  }

  @Override
  public void start(
    FabricationSettings workConfig,
    HubConfiguration hubConfig,
    HubClientAccess hubAccess
  ) {
    this.workConfig = workConfig;
    LOG.debug("Did set work configuration: {}", workConfig);

    this.hubConfig = hubConfig;
    LOG.debug("Did set hub configuration: {}", hubConfig);

    this.hubAccess = hubAccess;
    LOG.debug("Did set hub access: {}", hubAccess);

    audioCache.initialize(
      workConfig.getContentStoragePathPrefix(),
      hubConfig.getAudioBaseUrl(),
      workConfig.getOutputFrameRate(),
      FIXED_SAMPLE_BITS,
      workConfig.getOutputChannels()
    );
    LOG.debug("Did initialize audio cache");

    try {
      entityStore.clear();
    } catch (Exception e) {
      LOG.error("Failed to clear entity store", e);
    }
    LOG.debug("Did clear entity store");

    startedAtMillis.set(System.currentTimeMillis());
    isAudioLoaded.set(false);
    updateState(FabricationState.Starting);
    LOG.debug("Did update work state to Starting");

    running.set(true);
    LOG.debug("Did set running to true");

    new Thread(() -> {
      while (running.get()) {
        this.runControlCycle();
        this.runCraftCycle();
        this.runDubCycle();
      }
    }).start();
    LOG.debug("Did start thread with control/craft/dub cycles");

    new Thread(() -> {
      while (running.get()) {
        this.runShipCycle();
      }
    }).start();
    LOG.debug("Did start thread with ship cycle");

    telemetry.startTimer();
  }

  @Override
  public void finish(boolean cancelled) {
    running.set(false);

    // Shutting down ship work will cascade-send the finish() instruction to dub and ship
    if (Objects.nonNull(shipWork)) {
      shipWork.finish();
    }

    updateState(cancelled ? FabricationState.Cancelled : FabricationState.Done);
    if (Objects.nonNull(afterFinished)) {
      afterFinished.run();
    }

    audioCache.invalidateAll();
  }

  @Override
  public FabricationState getWorkState() {
    return state.get();
  }

  @Override
  public boolean isHealthy() {
    return getWorkState() != FabricationState.Failed;
  }

  @Override
  public void setOnProgress(@Nullable Consumer<Float> onProgress) {
    this.onProgress = onProgress;
  }

  @Override
  public void setOnStateChange(@Nullable Consumer<FabricationState> onStateChange) {
    this.onStateChange = onStateChange;
  }

  @Override
  public void setAfterFinished(@Nullable Runnable afterFinished) {
    this.afterFinished = afterFinished;
  }

  @Override
  public void doOverrideMacro(Program macroProgram) {
    assert Objects.nonNull(craftWork);
    assert Objects.nonNull(dubWork);
    craftWork.doOverrideMacro(macroProgram);
  }

  @Override
  public void resetOverrideMacro() {
    assert Objects.nonNull(craftWork);
    craftWork.resetOverrideMacro();
  }

  @Override
  public Optional<MemeTaxonomy> getMemeTaxonomy() {
    try {
      var templateConfig = new TemplateConfig(getSourceMaterial().getTemplates().stream().findFirst()
        .orElseThrow(() -> new ValueException("No template found in source material")));
      return Optional.of(templateConfig.getMemeTaxonomy());
    } catch (ValueException e) {
      LOG.error("Failed to get meme taxonomy from template config", e);
      return Optional.empty();
    }
  }

  @Override
  public void doOverrideMemes(Collection<String> memes) {
    assert Objects.nonNull(craftWork);
    assert Objects.nonNull(dubWork);
    craftWork.doOverrideMemes(memes);
  }

  @Override
  public void resetOverrideMemes() {
    assert Objects.nonNull(craftWork);
    craftWork.resetOverrideMemes();
  }

  @Override
  public boolean getAndResetDidOverride() {
    if (Objects.isNull(craftWork)) return false;
    return craftWork.getAndResetDidOverride();
  }

  @Override
  public NexusEntityStore getEntityStore() {
    return entityStore;
  }

  @Override
  public void reset() {
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

   @param fabricationState work state
   */
  private void updateState(FabricationState fabricationState) {
    state.set(fabricationState);
    if (Objects.nonNull(onStateChange)) {
      onStateChange.accept(fabricationState);
      LOG.debug("Did update work state to {} and notify listener", fabricationState);
    } else {
      LOG.debug("Did update work state to {} but there is no listener to notify", fabricationState);
    }
  }

  /**
   Run the control cycle, which prepares fabrication and moves the machine into the active state
   */
  private void runControlCycle() {
    LOG.debug("Will run control cycle");
    try {
      switch (state.get()) {

        case Starting -> {
          updateState(FabricationState.LoadingContent);
          startLoadingContent();
        }

        case LoadingContent -> {
          if (isContentLoaded()) {
            updateState(FabricationState.LoadedContent);
          }
        }

        case LoadedContent -> {
          updateState(FabricationState.PreparingAudio);
          startLoadingAudio();
        }

        case PreparingAudio -> {
          if (isAudioLoaded()) {
            updateState(FabricationState.PreparedAudio);
          }
        }

        case PreparedAudio -> {
          updateState(FabricationState.Initializing);
          initialize();
        }

        case Initializing -> {
          if (isInitialized()) {
            updateState(FabricationState.Active);
          }
        }

        case Active, Standby, Done, Failed -> {
          // no op
        }
      }
      LOG.debug("Did run control cycle");

    } catch (Exception e) {
      didFailWhile("running control cycle", e);
    }
  }

  /**
   Run the craft cycle
   */
  private void runCraftCycle() {
    if (!Objects.equals(state.get(), FabricationState.Active)) {
      LOG.debug("Will not run craft cycle because work state is {}", state.get());
      return;
    }
    assert Objects.nonNull(workConfig);
    assert Objects.nonNull(craftWork);
    assert Objects.nonNull(shipWork);
    assert Objects.nonNull(dubWork);

    try {
      LOG.debug("Will run craft cycle");
      craftWork.runCycle(
        shipWork.getShippedToChainMicros().orElse(0L),
        dubWork.getDubbedToChainMicros().orElse(0L)
      );
      LOG.debug("Did run craft cycle");

    } catch (Exception e) {
      didFailWhile("running craft cycle", e);
    }
  }

  /**
   Run the dub cycle
   */
  private void runDubCycle() {
    if (!Objects.equals(state.get(), FabricationState.Active)) {
      LOG.debug("Will not run dub cycle because work state is {}", state.get());
      return;
    }
    assert Objects.nonNull(workConfig);
    assert Objects.nonNull(dubWork);
    assert Objects.nonNull(shipWork);

    try {
      LOG.debug("Will run dub cycle");
      dubWork.runCycle(shipWork.getShippedToChainMicros().orElse(0L));
      LOG.debug("Did run dub cycle");

    } catch (Exception e) {
      didFailWhile("running dub cycle", e);
    }
  }

  /**
   Run the ship cycle
   */
  private void runShipCycle() {
    if (!Objects.equals(state.get(), FabricationState.Active)) {
      LOG.debug("Will not run ship cycle because work state is {}", state.get());
      return;
    }
    assert Objects.nonNull(workConfig);
    assert Objects.nonNull(shipWork);

    try {
      LOG.debug("Will run ship cycle");
      shipWork.runCycle();
      LOG.debug("Did run ship cycle");

    } catch (Exception e) {
      didFailWhile("running ship cycle", e);
    }

    if (shipWork.isFinished()) {
      updateState(FabricationState.Done);
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
      LOG.debug("Did update progress to {} and notify listener", progress);
    } else {
      LOG.debug("Did not update progress to {} because there is no listener to notify", progress);
    }
  }

  /**
   Start loading content
   */
  private void startLoadingContent() {
    assert Objects.nonNull(workConfig);
    hubContent.set(null);
    LOG.debug("Will start loading content");

    Callable<HubContent> hubContentProvider = new HubContentProvider(
      hubClient,
      hubConfig,
      hubAccess,
      workConfig.getInputMode(),
      workConfig.getInputTemplateKey()
    );

    try {
      hubContent.set(hubContentProvider.call());
      LOG.debug("Did load content");
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

    LOG.debug("Will start loading audio");
    try {
      var instruments = new ArrayList<>(hubContent.get().getInstruments());
      var audios = new ArrayList<>(hubContent.get().getInstrumentAudios());
      for (Instrument instrument : instruments) {
        for (InstrumentAudio audio : audios.stream()
          .filter(a -> Objects.equals(a.getInstrumentId(), instrument.getId()))
          .sorted(Comparator.comparing(InstrumentAudio::getName))
          .toList()) {
          if (!Objects.equals(state.get(), FabricationState.PreparingAudio)) {
            // Workstation canceling preloading should cease resampling audio files https://www.pivotaltracker.com/story/show/186209135
            return;
          }
          if (!StringUtils.isNullOrEmpty(audio.getWaveformKey())) {
            LOG.debug("Will preload audio for instrument {} with waveform key {}", instrument.getName(), audio.getWaveformKey());
            audioCache.prepare(audio);
            LOG.debug("Did preload audio OK");
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
      entityStore,
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
    updateState(FabricationState.Failed);
  }
}
