// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.engine.work;

import io.xj.hub.HubContent;
import io.xj.hub.TemplateConfig;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.meme.MemeTaxonomy;
import io.xj.hub.pojos.Instrument;
import io.xj.hub.pojos.InstrumentAudio;
import io.xj.hub.pojos.Program;
import io.xj.hub.util.StringUtils;
import io.xj.hub.util.ValueException;
import io.xj.engine.audio.AudioCache;
import io.xj.engine.craft.CraftFactory;
import io.xj.engine.fabricator.FabricatorFactory;
import io.xj.engine.mixer.MixerFactory;
import io.xj.engine.fabricator.FabricationEntityStore;
import io.xj.engine.ship.broadcast.BroadcastFactory;
import io.xj.engine.telemetry.Telemetry;
import jakarta.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static io.xj.hub.util.StringUtils.formatStackTrace;
import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;
import static io.xj.engine.mixer.FixedSampleBits.FIXED_SAMPLE_BITS;

public class FabricationManagerImpl implements FabricationManager {
  private static final Logger LOG = LoggerFactory.getLogger(FabricationManagerImpl.class);
  private final BroadcastFactory broadcastFactory;
  private final CraftFactory craftFactory;
  private final AudioCache audioCache;
  private final FabricatorFactory fabricatorFactory;
  private final MixerFactory mixerFactory;
  private final FabricationEntityStore entityStore;
  private final Telemetry telemetry;
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
  private FabricationSettings config;

  @Nullable
  private Consumer<Float> onProgress;

  @Nullable
  private Consumer<String> onProgressLabel;

  @Nullable
  private Consumer<FabricationState> onStateChange;

  @Nullable
  private HubContent content;

  public FabricationManagerImpl(
    Telemetry telemetry,
    BroadcastFactory broadcastFactory,
    CraftFactory craftFactory,
    AudioCache audioCache,
    FabricatorFactory fabricatorFactory,
    MixerFactory mixerFactory,
    FabricationEntityStore store
  ) {
    this.broadcastFactory = broadcastFactory;
    this.craftFactory = craftFactory;
    this.audioCache = audioCache;
    this.fabricatorFactory = fabricatorFactory;
    this.mixerFactory = mixerFactory;
    this.entityStore = store;
    this.telemetry = telemetry;
  }

  @Override
  public void start(
    HubContent content,
    FabricationSettings config
  ) {
    this.content = content;
    this.config = config;
    LOG.debug("Did set work configuration: {}", config);

    LOG.debug("Did set hub content: {}", this.content);

    audioCache.initialize(
      config.getOutputFrameRate(),
      FIXED_SAMPLE_BITS,
      config.getOutputChannels()
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

    audioCache.invalidateAll();
  }

  @Override
  public FabricationState getWorkState() {
    return state.get();
  }

  @Override
  public void setOnProgress(@Nullable Consumer<Float> onProgress) {
    this.onProgress = onProgress;
  }

  @Override
  public void setOnProgressLabel(@Nullable Consumer<String> onProgressLabel) {
    this.onProgressLabel = onProgressLabel;
  }

  @Override
  public void setOnStateChange(@Nullable Consumer<FabricationState> onStateChange) {
    this.onStateChange = onStateChange;
  }

  @Override
  public void doOverrideMacro(Program macroProgram) {
    Objects.requireNonNull(craftWork);
    Objects.requireNonNull(dubWork);
    craftWork.doOverrideMacro(macroProgram);
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
  public List<Program> getAllMacroPrograms() {
    return getSourceMaterial().getProgramsOfType(ProgramType.Macro).stream()
      .sorted(Comparator.comparing(Program::getName))
      .toList();
  }

  @Override
  public void doOverrideMemes(Collection<String> memes) {
    Objects.requireNonNull(craftWork);
    Objects.requireNonNull(dubWork);
    craftWork.doOverrideMemes(memes);
  }

  @Override
  public boolean getAndResetDidOverride() {
    if (Objects.isNull(craftWork)) return false;
    return craftWork.getAndResetDidOverride();
  }

  @Override
  public void setIntensityOverride(@Nullable Double intensity) {
    if (Objects.isNull(dubWork)) return;
    dubWork.setIntensityOverride(intensity);
  }

  @Override
  public FabricationEntityStore getEntityStore() {
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
          updateState(FabricationState.PreparingAudio);
          startPreparingAudio();
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
    Objects.requireNonNull(config);
    Objects.requireNonNull(craftWork);
    Objects.requireNonNull(shipWork);
    Objects.requireNonNull(dubWork);

    try {
      LOG.debug("Will run craft cycle");
      craftWork.runCycle(
        shipWork.getShippedToChainMicros().map(m -> m + config.getMixerLengthSeconds() * MICROS_PER_SECOND).orElse(0L),
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
    Objects.requireNonNull(config);
    Objects.requireNonNull(dubWork);
    Objects.requireNonNull(shipWork);

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
    Objects.requireNonNull(config);
    Objects.requireNonNull(shipWork);

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
   Start loading audio
   */
  private void startPreparingAudio() {
    Objects.requireNonNull(config);
    Objects.requireNonNull(content);

    int preparedAudios = 0;
    int preparedInstruments = 0;

    var instruments = new ArrayList<>(content.getInstruments());
    var audios = new ArrayList<>(content.getInstrumentAudios());
    LOG.debug("Will start loading audio");
    updateProgress(0.0f);
    updateProgressLabel(String.format("Prepared 0/%d audios for 0/%d instruments", audios.size(), instruments.size()));
    try {
      for (Instrument instrument : instruments) {
        for (InstrumentAudio audio : audios.stream()
          .filter(a -> Objects.equals(a.getInstrumentId(), instrument.getId()))
          .sorted(Comparator.comparing(InstrumentAudio::getName))
          .toList()) {
          if (!Objects.equals(state.get(), FabricationState.PreparingAudio)) {
            // Workstation canceling preloading should cease resampling audio files https://github.com/xjmusic/workstation/issues/278
            return;
          }
          if (!StringUtils.isNullOrEmpty(audio.getWaveformKey())) {
            LOG.debug("Will preload audio for instrument {} with waveform key {}", instrument.getName(), audio.getWaveformKey());
            audioCache.prepare(audio);
            LOG.debug("Did preload audio OK");
            updateProgress((float) preparedAudios / audios.size());
            updateProgressLabel(String.format("Prepared %d/%d audios for %d/%d instruments", preparedAudios, audios.size(), preparedInstruments, instruments.size()));
            preparedAudios++;
          }
        }
        preparedInstruments++;
      }
      updateProgress(1.0f);
      updateProgressLabel(String.format("Prepared %d audios for %d instruments", preparedAudios, preparedInstruments));
      isAudioLoaded.set(true);
      LOG.info("Preloaded {} audios from {} instruments", preparedAudios, instruments.size());

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
    Objects.requireNonNull(config);
    Objects.requireNonNull(content);

    craftWork = new CraftWorkImpl(
      telemetry,
      craftFactory,
      fabricatorFactory,
      entityStore,
      audioCache,
      config.getPersistenceWindowSeconds(),
      config.getCraftAheadSeconds(),
      config.getMixerLengthSeconds(),
      config.getOutputFrameRate(),
      config.getOutputChannels(),
      content
    );
    dubWork = new DubWorkImpl(
      telemetry,
      craftWork,
      mixerFactory,
      config.getMixerLengthSeconds(),
      config.getDubAheadSeconds(),
      config.getOutputFrameRate(),
      config.getOutputChannels()
    );
    shipWork = new ShipWorkImpl(
      telemetry,
      dubWork,
      broadcastFactory
    );

    // If memes/macro already engaged at fabrication start (which is always true in a manual control mode),
    // the first segment should be governed by that selection https://github.com/xjmusic/workstation/issues/201
    switch (config.getMacroMode()) {
      case MACRO -> getAllMacroPrograms().stream()
        .min(Comparator.comparing(Program::getName))
        .ifPresent(this::doOverrideMacro);
      case TAXONOMY -> getMemeTaxonomy().ifPresent(memeTaxonomy -> {
        var memes = memeTaxonomy.getCategories().stream()
          .map(category -> category.getMemes().stream().findFirst().orElse(null))
          .filter(Objects::nonNull)
          .collect(Collectors.toCollection(LinkedHashSet::new));
        if (!memes.isEmpty()) doOverrideMemes(memes);
      });
    }
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

  /**
   Update the progress

   @param progress progress
   */
  private void updateProgress(float progress) {
    if (Objects.nonNull(onProgress))
      onProgress.accept(progress);
  }

  /**
   Update the progress label

   @param label progress label
   */
  private void updateProgressLabel(String label) {
    if (Objects.nonNull(onProgressLabel))
      onProgressLabel.accept(label);
  }
}
