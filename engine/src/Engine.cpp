// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/Engine.h"

using namespace XJ;

Engine::Engine(
    SegmentEntityStore *store,
    ContentEntityStore *content,
    const WorkSettings &config) {
  workManager = new WorkManager(store, content, config);
}

void Engine::start() const {
  workManager->start();
}

void Engine::finish(bool cancelled) const {
  workManager->finish(cancelled);
}

std::set<ActiveAudio> Engine::runCycle(unsigned long long atChainMicros) const {
  return workManager->runCycle(atChainMicros);
}

SegmentEntityStore *Engine::getEntityStore() const {
  return workManager->getEntityStore();
}

ContentEntityStore *Engine::getSourceMaterial() const {
  return workManager->getSourceMaterial();
}

WorkState Engine::getWorkState() const {
  return workManager->getState();
}

std::optional<MemeTaxonomy> Engine::getMemeTaxonomy() const {
  return workManager->getMemeTaxonomy();
}

std::vector<const Program *> Engine::getAllMacroPrograms() const {
  return workManager->getAllMacroPrograms();
}

void Engine::doOverrideMacro(const Program *macroProgram) {
  return workManager->doOverrideMacro(macroProgram);
}

void Engine::doOverrideMemes(const std::set<std::string> &memes) {
  return workManager->doOverrideMemes(memes);
}

void Engine::setIntensityOverride(std::optional<float> intensity) {
  return workManager->setIntensityOverride(intensity);
}

Engine::~Engine() {
  delete workManager;
}

