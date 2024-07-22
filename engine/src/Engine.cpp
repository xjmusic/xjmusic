// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <filesystem>
#include <fstream>

#include <spdlog/spdlog.h>

#include "xjmusic/Engine.h"


using namespace XJ;

// Step 1: Define an array of audio file extensions
const std::array<std::string, 7> audioExtensions = {"mp3", "wav", "flac", "aac", "ogg", "aif", "aiff"};

Engine::Engine(
    const std::string &pathToProjectFile,
    const Fabricator::ControlMode controlMode,
    const std::optional<int> craftAheadSeconds,
    const std::optional<int> dubAheadSeconds,
    const std::optional<int> persistenceWindowSeconds) {
  this->pathToProjectFile = pathToProjectFile;
  settings.controlMode = controlMode;
  if (craftAheadSeconds.has_value()) settings.craftAheadSeconds = craftAheadSeconds.value();
  if (dubAheadSeconds.has_value()) settings.dubAheadSeconds = dubAheadSeconds.value();
  if (persistenceWindowSeconds.has_value()) settings.persistenceWindowSeconds = persistenceWindowSeconds.value();
  store = std::make_unique<SegmentEntityStore>();
  projectContent = std::make_unique<ContentEntityStore>();
  templateContent = std::make_unique<ContentEntityStore>();

  // Load the project content before creating the WorkManager
  loadProjectContent();

  work = std::make_unique<WorkManager>(store.get(), projectContent.get(), settings);
}


void Engine::start(const std::optional<std::string> &templateIdentifier) {
  const std::optional<const Template *> tmpl = templateIdentifier.has_value()
                                                   ? projectContent->getTemplateByIdentifier(templateIdentifier.value())
                                                   : projectContent->getFirstTemplate();
  templateContent->clear();
  if (tmpl.has_value()) {
    const auto content = work->getSourceMaterial()->forTemplate(tmpl.value());
    templateContent->put(&content);
  } else {
    templateContent->put(projectContent.get());
  }
  work->start();
}

void Engine::finish(const bool cancelled) const {
  work->finish(cancelled);
}

std::set<ActiveAudio> Engine::runCycle(const unsigned long long atChainMicros) const {
  return work->runCycle(atChainMicros);
}

SegmentEntityStore *Engine::getSegmentStore() const {
  return store.get();
}

ContentEntityStore *Engine::getProjectContent() const {
  return projectContent.get();
}

ContentEntityStore *Engine::getTemplateContent() const {
  return templateContent.get();
}

WorkState Engine::getWorkState() const {
  return work->getState();
}

std::optional<MemeTaxonomy> Engine::getMemeTaxonomy() const {
  return work->getMemeTaxonomy();
}

std::vector<const Program *> Engine::getAllMacroPrograms() const {
  return work->getAllMacroPrograms();
}

void Engine::doOverrideMacro(const Program *macroProgram) const {
  return work->doOverrideMacro(macroProgram);
}

void Engine::doOverrideMemes(const std::set<std::string> &memes) const {
  return work->doOverrideMemes(memes);
}

void Engine::setIntensityOverride(const std::optional<float> intensity) const {
  return work->setIntensityOverride(intensity);
}

std::filesystem::path Engine::getPathToBuildDirectory() {
  return pathToBuildDirectory;
}

Engine::~Engine() = default;

void Engine::loadProjectContent() {
  projectContent->clear();

  // Assert that pathToProjectFile ends in .xj
  if (pathToProjectFile.length() < 3 || pathToProjectFile.substr(pathToProjectFile.length() - 3) != ".xj") {
    throw std::invalid_argument("Path to project file must end in .xj -- invalid project file: " + pathToProjectFile);
  }

  // Assert that pathToProjectFile exists
  if (!std::filesystem::exists(pathToProjectFile)) {
    throw std::invalid_argument("Path to project file does not exist: " + pathToProjectFile);
  }

  // Load the project file and deserialize it from JSON into a ContentEntityStore via the nlohmann::json library
  std::ifstream projectFile(pathToProjectFile);
  auto projectFileContent = ContentEntityStore(projectFile);
  auto project = projectFileContent.getProject();
  if (!project.has_value()) {
    throw std::runtime_error("Project file does not contain a project: " + pathToProjectFile);
  }
  spdlog::info("Loaded project \"{}\" from file {}", project.value()->name, pathToProjectFile);

  // Crawl the build folder in the folder containing the project file
  const std::filesystem::path projectPath(pathToProjectFile);
  const std::filesystem::path folderContainingProject = projectPath.parent_path();
  // get the "build" folder path
  pathToBuildDirectory = folderContainingProject / "build";
  try {
    for (const auto &entry: std::filesystem::recursive_directory_iterator(pathToBuildDirectory)) {
      if (entry.is_regular_file()) {
        // Get the extension and convert it to lowercase
        std::string extension = entry.path().extension().string().substr(1);
        std::transform(extension.begin(), extension.end(), extension.begin(),
                       [](const unsigned char c) { return std::tolower(c); });

        if (extension == "json") {
          // If file is a .json file, load it into the content entity store
          std::ifstream jsonFile(entry.path());
          auto subFileContent = ContentEntityStore(jsonFile);
          projectContent.get()->put(&subFileContent);
          spdlog::info("Loaded content from JSON file: {}", entry.path().string());
        }
      }
    }

  } catch (const std::filesystem::filesystem_error &e) {
    spdlog::error("Filesystem error! {}", e.what());
  } catch (const std::exception &e) {
    spdlog::error("General error! {}", e.what());
  }
}
