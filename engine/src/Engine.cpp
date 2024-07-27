// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <filesystem>
#include <fstream>

#include "xjmusic/Engine.h"


using namespace XJ;

Engine::Engine(
    const std::optional<std::string> &pathToProjectFile,
    const std::optional<Fabricator::ControlMode> controlMode,
    const std::optional<int> craftAheadSeconds,
    const std::optional<int> dubAheadSeconds,
    const std::optional<int> persistenceWindowSeconds) {
  settings.controlMode = controlMode.has_value() ? controlMode.value() : Fabricator::ControlMode::Auto;
  if (craftAheadSeconds.has_value()) settings.craftAheadSeconds = craftAheadSeconds.value();
  if (dubAheadSeconds.has_value()) settings.dubAheadSeconds = dubAheadSeconds.value();
  if (persistenceWindowSeconds.has_value()) settings.persistenceWindowSeconds = persistenceWindowSeconds.value();
  store = std::make_unique<SegmentEntityStore>();
  projectContent = std::make_unique<ContentEntityStore>();
  templateContent = std::make_unique<ContentEntityStore>();

  // Load the project content before creating the WorkManager
  if (pathToProjectFile.has_value()) loadProjectContent(pathToProjectFile.value());
  templateContent->put(projectContent.get());
}


void Engine::start(const std::optional<std::string> &templateIdentifier) {
  work = std::make_unique<WorkManager>(store.get(), projectContent.get(), settings);
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
  if (!work) return;
  work->finish(cancelled);
}

std::set<AudioScheduleEvent> Engine::RunCycle(const unsigned long long atChainMicros) const {
  if (!work) return {};
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
  if (!work) return WorkState::Standby;
  return work->getState();
}

std::optional<MemeTaxonomy> Engine::getMemeTaxonomy() const {
  return templateContent->getMemeTaxonomy();
}

std::vector<const Program *> Engine::getAllMacroPrograms() const {
  if (!work) return {};
  return work->getAllMacroPrograms();
}

void Engine::doOverrideMacro(const Program *macroProgram) const {
  if (!work) return;
  work->doOverrideMacro(macroProgram);
}

void Engine::doOverrideMemes(const std::set<std::string> &memes) const {
  if (!work) return;
  work->doOverrideMemes(memes);
}

void Engine::setIntensityOverride(const std::optional<float> intensity) const {
  if (!work) return;
  work->setIntensityOverride(intensity);
}

std::filesystem::path Engine::getPathToBuildDirectory() {
  return pathToBuildDirectory;
}

WorkSettings Engine::getSettings() const {
  return settings;
}

Engine::~Engine() = default;

void Engine::loadProjectContent(const std::string &pathToProjectFile) {
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
  std::cout << "Did read project \"" << project.value()->name << "\" from file " << pathToProjectFile << std::endl;

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
          projectContent->put(&subFileContent);
          std::cout << "Loaded content from JSON file: " << entry.path().string() << std::endl;
        }
      }
    }

  } catch (const std::filesystem::filesystem_error &e) {
    std::cout << "Filesystem error! " << e.what() << std::endl;
  } catch (const std::exception &e) {
    std::cout << "General error! " << e.what() << std::endl;
  }

  std::cout << "Loaded project OK" << std::endl << std::endl;
}
