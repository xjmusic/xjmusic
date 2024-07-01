// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO implement this

#include "xjmusic/work/FabricationManager.h"

using namespace XJ;

  FabricationManager::FabricationManager(
    Telemetry telemetry,
    BroadcastFactory broadcastFactory,
    CraftFactory craftFactory,
    AudioCache audioCache,
    FabricatorFactory fabricatorFactory,
    MixerFactory mixerFactory,
    SegmentEntityStore store
  ) {
    this->broadcastFactory = broadcastFactory;
    this->craftFactory = craftFactory;
    this->audioCache = audioCache;
    this->fabricatorFactory = fabricatorFactory;
    this->mixerFactory = mixerFactory;
    this->entityStore = store;
    this->telemetry = telemetry;
  }

  @Override
  public void start(
    ContentEntityStore content,
    FabricationSettings config
  ) {
    this->content = content;
    this->config = config;
    spdlog::debug("Did set work configuration: {}", config);

    spdlog::debug("Did set model content: {}", this->content);

    audioCache.initialize(
      config.getOutputFrameRate(),
      FIXED_SAMPLE_BITS,
      config.getOutputChannels()
    );
    spdlog::debug("Did initialize audio cache");

    try {
      entityStore.clear();
    } catch (Exception e) {
      spdlog::error("Failed to clear entity store", e);
    }
    spdlog::debug("Did clear entity store");

    startedAtMillis.set(System.currentTimeMillis());
    isAudioLoaded.set(false);
    updateState(FabricationState.Starting);
    spdlog::debug("Did update work state to Starting");

    running.set(true);
    spdlog::debug("Did set running to true");

    new Thread(() -> {
      while (running.get()) {
        this->runControlCycle();
        this->runCraftCycle();
        this->runDubCycle();
      }
    }).start();
    spdlog::debug("Did start thread with control/craft/dub cycles");

    new Thread(() -> {
      while (running.get()) {
        this->runShipCycle();
      }
    }).start();
    spdlog::debug("Did start thread with ship cycle");

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
    this->onProgress = onProgress;
  }

  @Override
  public void setOnProgressLabel(@Nullable Consumer<String> onProgressLabel) {
    this->onProgressLabel = onProgressLabel;
  }

  @Override
  public void setOnStateChange(@Nullable Consumer<FabricationState> onStateChange) {
    this->onStateChange = onStateChange;
  }

  @Override
  public void doOverrideMacro(Program macroProgram) {
    Objects.requireNonNull(craftWork);
    Objects.requireNonNull(dubWork);
    craftWork.doOverrideMacro(macroProgram);
  }

  @Override
  public std::optional<MemeTaxonomy> getMemeTaxonomy() {
    try {
      auto templateConfig = new TemplateConfig(getSourceMaterial().getTemplates().stream().findFirst()
        .orElseThrow(() -> new ValueException("No template found in source material")));
      return std::optional.of(templateConfig.getMemeTaxonomy());
    } catch (ValueException e) {
      spdlog::error("Failed to get meme taxonomy from template config", e);
      return std::nullopt;
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
  public SegmentEntityStore getEntityStore() {
    return entityStore;
  }

  @Override
  public void reset() {
    craftWork = null;
    dubWork = null;
    shipWork = null;
  }

  @Override
  public ContentEntityStore getSourceMaterial() {
    return Objects.requireNonNull(craftWork).getSourceMaterial();
  }

  @Override
  public std::optional<Long> getShippedToChainMicros() {
    return Objects.nonNull(shipWork) ? shipWork.getShippedToChainMicros() : std::nullopt;
  }

  @Override
  public std::optional<Long> getDubbedToChainMicros() {
    return Objects.nonNull(dubWork) ? dubWork.getDubbedToChainMicros() : std::nullopt;
  }

  @Override
  public std::optional<Long> getCraftedToChainMicros() {
    return Objects.nonNull(craftWork) ? craftWork.getCraftedToChainMicros() : std::nullopt;
  }

  /**
   Update the current work state

   @param fabricationState work state
   */
  private void updateState(FabricationState fabricationState) {
    state.set(fabricationState);
    if (Objects.nonNull(onStateChange)) {
      onStateChange.accept(fabricationState);
      spdlog::debug("Did update work state to {} and notify listener", fabricationState);
    } else {
      spdlog::debug("Did update work state to {} but there is no listener to notify", fabricationState);
    }
  }

  /**
   Run the control cycle, which prepares fabrication and moves the machine into the active state
   */
  private void runControlCycle() {
    spdlog::debug("Will run control cycle");
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
      spdlog::debug("Did run control cycle");

    } catch (Exception e) {
      didFailWhile("running control cycle", e);
    }
  }

  /**
   Run the craft cycle
   */
  private void runCraftCycle() {
    if (!Objects.equals(state.get(), FabricationState.Active)) {
      spdlog::debug("Will not run craft cycle because work state is {}", state.get());
      return;
    }
    Objects.requireNonNull(config);
    Objects.requireNonNull(craftWork);
    Objects.requireNonNull(shipWork);
    Objects.requireNonNull(dubWork);

    try {
      spdlog::debug("Will run craft cycle");
      craftWork.runCycle(
        shipWork.getShippedToChainMicros().map(m -> m + config.getMixerLengthSeconds() * MICROS_PER_SECOND).orElse(0L),
        dubWork.getDubbedToChainMicros().orElse(0L)
      );
      spdlog::debug("Did run craft cycle");

    } catch (Exception e) {
      didFailWhile("running craft cycle", e);
    }
  }

  /**
   Run the dub cycle
   */
  private void runDubCycle() {
    if (!Objects.equals(state.get(), FabricationState.Active)) {
      spdlog::debug("Will not run dub cycle because work state is {}", state.get());
      return;
    }
    Objects.requireNonNull(config);
    Objects.requireNonNull(dubWork);
    Objects.requireNonNull(shipWork);

    try {
      spdlog::debug("Will run dub cycle");
      dubWork.runCycle(shipWork.getShippedToChainMicros().orElse(0L));
      spdlog::debug("Did run dub cycle");

    } catch (Exception e) {
      didFailWhile("running dub cycle", e);
    }
  }

  /**
   Run the ship cycle
   */
  private void runShipCycle() {
    if (!Objects.equals(state.get(), FabricationState.Active)) {
      spdlog::debug("Will not run ship cycle because work state is {}", state.get());
      return;
    }
    Objects.requireNonNull(config);
    Objects.requireNonNull(shipWork);

    try {
      spdlog::debug("Will run ship cycle");
      shipWork.runCycle();
      spdlog::debug("Did run ship cycle");

    } catch (Exception e) {
      didFailWhile("running ship cycle", e);
    }

    if (shipWork.isFinished()) {
      updateState(FabricationState.Done);
      spdlog::info("Fabrication work done");
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

    auto instruments = new ArrayList<>(content.getInstruments());
    auto audios = new ArrayList<>(content.getInstrumentAudios());
    spdlog::debug("Will start loading audio");
    updateProgress(0.0f);
    updateProgressLabel(String.format("Prepared 0/%d audios for 0/%d instruments", audios.size(), instruments.size()));
    try {
      for (Instrument instrument : instruments) {
        for (InstrumentAudio audio : audios.stream()
          .filter(a -> Objects.equals(a.getInstrumentId(), instrument.getId()))
          .sorted(Comparator.comparing(InstrumentAudio::getName))
          .toList()) {
          if (!Objects.equals(state.get(), FabricationState.PreparingAudio)) {
            // Workstation canceling preloading should cease resampling audio files https://github.com/xjmusic/xjmusic/issues/278
            return;
          }
          if (!StringUtils.isNullOrEmpty(audio.getWaveformKey())) {
            spdlog::debug("Will preload audio for instrument {} with waveform key {}", instrument.getName(), audio.getWaveformKey());
            audioCache.prepare(audio);
            spdlog::debug("Did preload audio OK");
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
      spdlog::info("Preloaded {} audios from {} instruments", preparedAudios, instruments.size());

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
    // the first segment should be governed by that selection https://github.com/xjmusic/xjmusic/issues/201
    switch (config.getMacroMode()) {
      case MACRO -> getAllMacroPrograms().stream()
        .min(Comparator.comparing(Program::getName))
        .ifPresent(this::doOverrideMacro);
      case TAXONOMY -> getMemeTaxonomy().ifPresent(memeTaxonomy -> {
        auto memes = memeTaxonomy.getCategories().stream()
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
    spdlog::error("Failed while {}: {}", msgWhile, formatStackTrace(e), e);
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

