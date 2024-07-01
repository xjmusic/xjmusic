// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

// TODO implement this

#include "xjmusic/work/FabricationManager.h"

using namespace XJ;

  FabricationManager::FabricationManager(
    CraftFactory *craftFactory,
    FabricatorFactory* fabricatorFactory,
    SegmentEntityStore* store
  ) {
    this->craftFactory = craftFactory;
    this->fabricatorFactory = fabricatorFactory;
    this->entityStore = store;
  }

void FabricationManager::start(
      ContentEntityStore *content,
      FabricationSettings config) {
    this->content = content;
    this->config = config;
    spdlog::debug("Did set work configuration: {}", config);

    spdlog::debug("Did set model content: {}", this->content);

    try {
      entityStore->clear();
    } catch (std::exception e) {
      spdlog::error("Failed to clear entity store", e);
    }
    spdlog::debug("Did clear entity store");

    // get system milliseconds UTC now
    startedAtMillis = std::chrono::duration_cast<std::chrono::milliseconds>(std::chrono::system_clock::now().time_since_epoch()).count();
    isAudioLoaded = false;
    state = Starting;
    spdlog::debug("Did update work state to Starting");

    running = true;
    spdlog::debug("Did set running to true");
  }

  void FabricationManager::finish(const bool cancelled) {
    running = false;

    state = cancelled ? Cancelled : Done;
  }

void FabricationManager::tick() {
    this->runControlCycle();
    this->runCraftCycle();
    this->runDubCycle();
  }

FabricationState FabricationManager::getWorkState() {
    return state;
  }

 void FabricationManager::doOverrideMacro(Program macroProgram) {
    craftWork->doOverrideMacro(&macroProgram);
  }

std::optional<MemeTaxonomy*> getMemeTaxonomy() {
    try {
      auto templateConfig = new TemplateConfig(getSourceMaterial().getTemplates().stream().findFirst()
        .orElseThrow(() -> new ValueException("No template found in source material")));
      return std::optional(templateConfig.getMemeTaxonomy());
    } catch (std::exception e) {
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
  void updateState(FabricationState fabricationState) {
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
  void FabricationManager::runControlCycle() {
    spdlog::debug("Will run control cycle");
    try {
      switch (state) {

        case Starting: {
          updateState(FabricationState::PreparingAudio);
        }
        break;

        case PreparingAudio: {
          if (isAudioLoaded()) {
            updateState(FabricationState::PreparedAudio);
          }
        }
        break;

        case PreparedAudio : {
          updateState(FabricationState::Initializing);
          initialize();
        }
        break;

        case Initializing :{
          if (isInitialized()) {
            updateState(FabricationState::Active);
          }
        }
        break;

        case Active:
          case Standby:
        case Done:
        case Failed: {
          // no op
        }
        break;
      }
      spdlog::debug("Did run control cycle");

    } catch (std::exception e) {
      didFailWhile("running control cycle", e);
    }
  }

  /**
   Run the craft cycle
   */
  void runCraftCycle() {
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

    } catch (std::exception e) {
      didFailWhile("running craft cycle", e);
    }
  }

  /**
   Run the dub cycle
   */
  void runDubCycle() {
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

    } catch (std::exception e) {
      didFailWhile("running dub cycle", e);
    }
  }

  /**
   Run the ship cycle
   */
  void runShipCycle() {
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

    } catch (std::exception e) {
      didFailWhile("running ship cycle", e);
    }

    if (shipWork.isFinished()) {
      updateState(FabricationState.Done);
      spdlog::info("Fabrication work done");
    }
  }

  void FabricationManager::initialize() {
    craftWork =new CraftWork(
      craftFactory,
      fabricatorFactory,
      entityStore,
      content,
      config.persistenceWindowSeconds,
      config.craftAheadSeconds
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
        if (!memes.empty()) doOverrideMemes(memes);
      });
    }
  }

  /**
   @return true if initialized
   */
  boolean isInitialized() {
    return Objects.nonNull(craftWork) && Objects.nonNull(dubWork) && Objects.nonNull(shipWork);
  }

  /**
   Log and of segment message of error that job failed while (message)

   @param msgWhile phrased like "Doing work"
   @param e        exception (optional)
   */
  void didFailWhile(String msgWhile, std::exception e) {
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
  void updateProgress(float progress) {
    if (Objects.nonNull(onProgress))
      onProgress.accept(progress);
  }

  /**
   Update the progress label

   @param label progress label
   */
  void updateProgressLabel(String label) {
    if (Objects.nonNull(onProgressLabel))
      onProgressLabel.accept(label);
  }

