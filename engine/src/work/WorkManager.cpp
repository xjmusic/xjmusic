// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/work/WorkManager.h"

using namespace XJ;

WorkManager::WorkManager(
    SegmentEntityStore *store,
    ContentEntityStore *content,
    const WorkSettings &config) : craftWork(CraftWork(store,
                                                      content,
                                                      config.persistenceWindowSeconds,
                                                      config.craftAheadSeconds)),
                                  dubWork(DubWork(
                                      &craftWork,
                                      config.dubAheadSeconds)) {
  this->store = store;
  this->content = content;
  this->config = config;

  try {
    const auto templates = getSourceMaterial()->getTemplates();
    if (templates.empty()) {
      std::cerr << "No templates found in source material" << std::endl;
      return;
    }
    memeTaxonomy = {MemeTaxonomy((*templates.begin())->config.memeTaxonomy)};
  } catch (std::exception &e) {
    std::cerr << "Failed to get meme taxonomy from template config: " << e.what() << std::endl;
  }
}

void WorkManager::start() {
  // If memes/macro already engaged at fabrication start (which is always true in a manual control mode),
  // the first segment should be governed by that selection https://github.com/xjmusic/xjmusic/issues/201
  switch (config.controlMode) {
    case Fabricator::ControlMode::Macro: {
      auto programs = getAllMacroPrograms();
      if (!programs.empty())
        doOverrideMacro(*programs.begin());
    }
      break;
    case Fabricator::ControlMode::Taxonomy: {
      if (memeTaxonomy.has_value()) {
        std::set<std::string> memes;
        for (const auto &category: memeTaxonomy.value().getCategories()) {
          if (!category.getMemes().empty())
            memes.insert(*category.getMemes().begin());
        }
        if (!memes.empty()) doOverrideMemes(memes);
      }
    }
      break;
    default:
      break;
  }

  // get system milliseconds UTC now
  startedAtMillis = std::chrono::duration_cast<std::chrono::milliseconds>(
      std::chrono::system_clock::now().time_since_epoch())
      .count();
  isAudioLoaded = false;
  updateState(Active);
  craftWork.start();
  dubWork.start();
}

void WorkManager::finish(const bool cancelled) {
  updateState(cancelled ? Cancelled : Done);
}

std::set<AudioScheduleEvent> WorkManager::runCycle(const unsigned long long atChainMicros) {
  this->runCraftCycle(atChainMicros);
  std::set<AudioScheduleEvent> audioEvents;
  std::set<std::string> foundAudioIds;

  // check for new or updated audio
  for (auto audio: this->runDubCycle(atChainMicros)) {
    foundAudioIds.insert(audio.getId());
    if (activeAudioMap.find(audio.getId()) != activeAudioMap.end()) {
      // check if the audio has been modified; if so, update it
      if (activeAudioMap.at(audio.getId()) != audio) {
        activeAudioMap.emplace(audio.getId(), audio);
        audioEvents.emplace(AudioScheduleEvent(AudioScheduleEvent::EType::Update, audio));
      }
    } else {
      // create a new audio
      activeAudioMap.emplace(audio.getId(), audio);
      audioEvents.emplace(AudioScheduleEvent(AudioScheduleEvent::EType::Create, audio));
    }
  }

  // check for deleted audio
  for (auto it = activeAudioMap.begin(); it != activeAudioMap.end();) {
    if (foundAudioIds.find(it->first) == foundAudioIds.end()) {
      audioEvents.emplace(AudioScheduleEvent(AudioScheduleEvent::EType::Delete, it->second));
      it = activeAudioMap.erase(it);
    } else {
      ++it;
    }
  }

  return audioEvents;
}

WorkState WorkManager::getState() const {
  return state;
}

void WorkManager::doOverrideMacro(const Program *macroProgram) {
  craftWork.doOverrideMacro(macroProgram);
}

std::optional<MemeTaxonomy> WorkManager::getMemeTaxonomy() const {
  return this->memeTaxonomy;
}

std::vector<const Program *> WorkManager::getAllMacroPrograms() const {
  auto programs = content->getProgramsOfType(Program::Type::Macro);
  auto sortedPrograms = std::vector(programs.begin(), programs.end());
  std::sort(sortedPrograms.begin(), sortedPrograms.end(), [](const Program *a, const Program *b) {
    return a->name.compare(b->name);
  });
  return sortedPrograms;
}

void WorkManager::doOverrideMemes(const std::set<std::string> &memes) {
  craftWork.doOverrideMemes(memes);
}

bool WorkManager::getAndResetDidOverride() {
  return craftWork.getAndResetDidOverride();
}

void WorkManager::setIntensityOverride(std::optional<float> intensity) {
  dubWork.setIntensityOverride(intensity);
}

SegmentEntityStore *WorkManager::getEntityStore() const {
  return store;
}

ContentEntityStore *WorkManager::getSourceMaterial() const {
  return content;
}

void WorkManager::runCraftCycle(const unsigned long long atChainMicros) {
  if (state != Active) {
    // Will not Run craft cycle because work state
    return;
  }
  try {
    // Run craft cycle
    craftWork.runCycle(atChainMicros);

  } catch (std::exception &e) {
    didFailWhile("running craft cycle", e);
  }
}

std::set<ActiveAudio> WorkManager::runDubCycle(const unsigned long long atChainMicros) {
  if (state != Active) {
    // Will not Run dub cycle because work state
    return {};
  }

  try {
    // Run dub cycle
    return dubWork.runCycle(atChainMicros);

  } catch (std::exception &e) {
    didFailWhile("running dub cycle", e);
    return {};
  }
}

void WorkManager::didFailWhile(const std::string& msgWhile, const std::exception &e) {
  std::cerr << "Failed while " << msgWhile << ": " << e.what() << std::endl;
  // This will cascade-send the finish() instruction to dub and ship
  updateState(Failed);
}

std::string WorkManager::toString(const WorkState state) {
  switch (state) {
    case Standby:
      return "Standby";
    case Active:
      return "Active";
    case Done:
      return "Done";
    case Cancelled:
      return "Cancelled";
    case Failed:
      return "Failed";
  }
  return "Unknown";
}

void WorkManager::updateState(const WorkState fabricationState) {
  state = fabricationState;
}

