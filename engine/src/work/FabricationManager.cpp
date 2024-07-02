// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <spdlog/spdlog.h>

#include "xjmusic/work/FabricationManager.h"

using namespace XJ;

FabricationManager::FabricationManager(
    CraftFactory *craftFactory,
    FabricatorFactory *fabricatorFactory,
    SegmentEntityStore *store) {
  this->craftFactory = craftFactory;
  this->fabricatorFactory = fabricatorFactory;
  this->entityStore = store;
}

void FabricationManager::start(
    ContentEntityStore *content,
    const FabricationSettings &config) {
  this->content = content;
  this->config = config;
  spdlog::debug("Did set work configuration: {}", config.toString());

  try {
    entityStore->clear();
  } catch (std::exception e) {
    spdlog::warn("Failed to clear entity store", e.what());
  }
  spdlog::debug("Did clear entity store");

  // get system milliseconds UTC now
  startedAtMillis = std::chrono::duration_cast<std::chrono::milliseconds>(
                        std::chrono::system_clock::now().time_since_epoch())
                        .count();
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

std::set<ActiveAudio> FabricationManager::runCycle(unsigned long long atChainMicros) {
  this->runControlCycle();
  this->runCraftCycle(atChainMicros);
  return this->runDubCycle(atChainMicros);
}

FabricationState FabricationManager::getWorkState() const {
  return state;
}

void FabricationManager::doOverrideMacro(const Program *macroProgram) const {
  if (craftWork == nullptr)
    return;
  craftWork->doOverrideMacro(macroProgram);
}

std::optional<MemeTaxonomy> FabricationManager::getMemeTaxonomy() const {
  try {
    const auto tmpls = getSourceMaterial()->getTemplates();
    if (tmpls.empty())
      throw std::runtime_error("No template found in source material");
    auto templateConfig = TemplateConfig(*getSourceMaterial()->getTemplates().begin());
    return {MemeTaxonomy(templateConfig.memeTaxonomy)};
  } catch (std::exception e) {
    spdlog::error("Failed to get meme taxonomy from template config: {}", e.what());
    return std::nullopt;
  }
}

std::vector<const Program *> FabricationManager::getAllMacroPrograms() const {
  auto programs = getSourceMaterial()->getProgramsOfType(Program::Type::Macro);
  auto sortedPrograms = std::vector(programs.begin(), programs.end());
  std::sort(sortedPrograms.begin(), sortedPrograms.end(), [](const Program *a, const Program *b) {
    return a->name.compare(b->name);
  });
  return sortedPrograms;
}

void FabricationManager::doOverrideMemes(const std::set<std::string> &memes) const {
  if (craftWork == nullptr)
    throw std::runtime_error("Craft work is not initialized");
  if (dubWork == nullptr)
    throw std::runtime_error("Dub work is not initialized");
  craftWork->doOverrideMemes(memes);
}

bool FabricationManager::getAndResetDidOverride() const {
  if (craftWork == nullptr) return false;
  return craftWork->getAndResetDidOverride();
}

void FabricationManager::setIntensityOverride(std::optional<float> intensity) const {
  if (dubWork == nullptr) return;
  dubWork->setIntensityOverride(intensity);
}

SegmentEntityStore *FabricationManager::getEntityStore() const {
  return entityStore;
}

void FabricationManager::reset() {
  craftWork = nullptr;
  dubWork = nullptr;
}

ContentEntityStore *FabricationManager::getSourceMaterial() const {
  if (craftWork == nullptr)
    throw std::runtime_error("Craft work is not initialized");
  return craftWork->getSourceMaterial();
}

void FabricationManager::updateState(const FabricationState fabricationState) {
  state = fabricationState;
  spdlog::debug("Did update work state to {} but there is no listener to notify", toString(fabricationState));
}

/**
   Run the control cycle, which prepares fabrication and moves the machine into the active state
   */
void FabricationManager::runControlCycle() {
  spdlog::debug("Will run control cycle");
  try {
    switch (state) {

      case Starting: {
        updateState(Initializing);
        initialize();
      } break;

      case Initializing: {
        if (isInitialized()) {
          updateState(Active);
        }
      } break;

      case Active:
      case Standby:
      case Done:
      case Failed:
      default: {
        // no op
      } break;
    }
    spdlog::debug("Did run control cycle");

  } catch (std::exception e) {
    didFailWhile("running control cycle", e);
  }
}

void FabricationManager::runCraftCycle(const unsigned long long atChainMicros) {
  if (state != Active) {
    spdlog::debug("Will not run craft cycle because work state is {}", toString(state));
    return;
  }
  if (craftWork == nullptr)
    throw std::runtime_error("Craft work is not initialized");
  if (dubWork == nullptr)
    throw std::runtime_error("Dub work is not initialized");
  if (craftWork == nullptr)
    throw std::runtime_error("Craft work is not initialized");

  try {
    spdlog::debug("Will run craft cycle");
    craftWork->runCycle(atChainMicros);
    spdlog::debug("Did run craft cycle");

  } catch (std::exception e) {
    didFailWhile("running craft cycle", e);
  }
}

std::set<ActiveAudio> FabricationManager::runDubCycle(const unsigned long long atChainMicros) {
  if (state != Active) {
    spdlog::debug("Will not run dub cycle because work state is {}", toString(state));
    return {};
  }
  if (dubWork == nullptr)
    throw std::runtime_error("Dub work is not initialized");

  try {
    spdlog::debug("Will run dub cycle");
    return dubWork->runCycle(atChainMicros);

  } catch (std::exception e) {
    didFailWhile("running dub cycle", e);
    return {};
  }
}

void FabricationManager::initialize() {
  craftWork = new CraftWork(
      craftFactory,
      fabricatorFactory,
      entityStore,
      content,
      config.persistenceWindowSeconds,
      config.craftAheadSeconds);
  dubWork = new DubWork(
      craftWork,
      config.dubAheadSeconds);

  // If memes/macro already engaged at fabrication start (which is always true in a manual control mode),
  // the first segment should be governed by that selection https://github.com/xjmusic/xjmusic/issues/201
  switch (config.controlMode) {
    case Fabricator::ControlMode::Macro: {
      auto programs = getAllMacroPrograms();
      if (!programs.empty())
        doOverrideMacro(*programs.begin());
    } break;
    case Fabricator::ControlMode::Taxonomy: {
      auto taxonomy = getMemeTaxonomy();
      if (taxonomy.has_value()) {
        std::set<std::string> memes;
        for (auto category: taxonomy.value().getCategories()) {
          if (category.getMemes().size() > 0)
            memes.insert(*category.getMemes().begin());
        }
        if (!memes.empty()) doOverrideMemes(memes);
      }
    } break;
    default:
      break;
  }
}

bool FabricationManager::isInitialized() const {
  return craftWork != nullptr && dubWork != nullptr;
}

void FabricationManager::didFailWhile(std::string msgWhile, std::exception e) {
  spdlog::error("Failed while {}: {}", msgWhile, e.what());
  // This will cascade-send the finish() instruction to dub and ship
  if (craftWork != nullptr)
    craftWork->finish();
  updateState(Failed);
}

std::string FabricationManager::toString(const FabricationState state) {
  switch (state) {
    default:
    case Standby:
      return "Standby";
    case Starting:
      return "Starting";
    case Initializing:
      return "Initializing";
    case Active:
      return "Active";
    case Done:
      return "Done";
    case Cancelled:
      return "Cancelled";
    case Failed:
      return "Failed";
  }
}