// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <SDL2/SDL.h>

#include <iostream>

// todo #include <ftxui/dom/elements.hpp>
// todo #include <ftxui/screen/screen.hpp>
// todo #include <ftxui/screen/string.hpp>

#include "xjmusic/Engine.h"
#include "xjmusic/util/CsvUtils.h"

const Uint32 MICROSECONDS_PER_MILLISECOND = 1000;

const Uint32 CYCLE_MILLISECONDS = 100;

/**
 * Runs the main loop of the application.
 *
 * @param XJ The XJ to Run.
 */
static void Run(Engine *XJ, const Template *CurrentTemplate) {
  // Use SDL to open an audio output buffer
  if (SDL_Init(SDL_INIT_AUDIO) < 0) {
    std::cerr << "SDL_Init failed: " << SDL_GetError() << std::endl;
    return;
  }

  std::cout << "Will start template: " << CurrentTemplate->name << std::endl;
  std::cout << "Meme Taxonomy: " << XJ->getMemeTaxonomy()->toString() << std::endl;

  // Start the engine
  XJ->start(CurrentTemplate->id);

  // Record the current start time
  const Uint32 StartTime = SDL_GetTicks();
  Uint32 ElapsedTime;

  // Whether we are Running
  bool Running = true;

  // The main loop
  while (Running) {
    // Process events
    SDL_Event Event;
    while (SDL_PollEvent(&Event)) {
      switch (Event.type) {
        case SDL_QUIT:
          Running = false;
          break;
        default:
          break;
      }
    }

    // Calculate the time since the start
    ElapsedTime = SDL_GetTicks() - StartTime;

    // Update the XJ
    std::set<ActiveAudio> ActiveAudios = XJ->RunCycle(ElapsedTime * MICROSECONDS_PER_MILLISECOND);



    // Print the active audios
    if (!ActiveAudios.empty()) {
      std::cout << "Active Audios:" << std::endl;
      for (const ActiveAudio &ActiveAudio: ActiveAudios) {
        std::cout << "Active Audio: " << ActiveAudio.getId() << " at " << ActiveAudio.getStartAtChainMicros()
                  << std::endl;
      }
    } else {
      std::cout << "No active audios" << std::endl;
    }

    // Sleep for a bit
    SDL_Delay(CYCLE_MILLISECONDS);
  }
}

/**
 * Selects a template from the Engine.
 * @param XJ  The Engine to select the template from.
 * @return  The selected template.
 */
static const Template *SelectTemplate(Engine *XJ) {
  std::cout << "[Templates]" << std::endl;
  std::vector<const Template *> AllTemplates;
  for (const Template *Template: XJ->getProjectContent()->getTemplates()) {
    AllTemplates.push_back(Template);
  }
  std::sort(AllTemplates.begin(), AllTemplates.end(), [](const Template *a, const Template *b) {
    return a->name < b->name;
  });
  for (int i = 0; i < AllTemplates.size(); i++) {
    const Template *Template = AllTemplates[i];
    std::cout << "  " << i << ": " << Template->name << std::endl;
  }
  // get keyboard input of a number followed by the enter key
  int TemplateIndex = -1;
  std::cout << "Enter the number of the template to start: ";
  std::cin >> TemplateIndex;
  if (TemplateIndex < 0 || TemplateIndex >= AllTemplates.size()) {
    std::cerr << "Invalid template index" << std::endl;
    throw std::invalid_argument("Invalid template index");
  }
  return AllTemplates[TemplateIndex];
}


/**
 * Main entry point of the application.
 * @param argc  The number of arguments passed to the application.
 * @param argv  The arguments passed to the application.
 * @return    The exit code of the application.
 */
int main(int argc, char *argv[]) {
  // Check if at least one argument was passed
  if (argc <= 1) {
    std::cout << "Must pass the path to an XJ music workstation .xj project as the first argument!" << std::endl;
    return -1;
  }
  std::string pathToProjectFile = argv[1];
  std::cout << "Will open project: " << pathToProjectFile << std::endl;

  try {
    const std::unique_ptr<Engine> XJ = std::make_unique<Engine>(
        pathToProjectFile,
        Fabricator::ControlMode::Auto,
        std::nullopt,
        std::nullopt,
        std::nullopt
    );
    const Template *CurrentTemplate = SelectTemplate(XJ.get());
    Run(XJ.get(), CurrentTemplate);

  } catch (const std::exception &e) {
    std::cerr << "Error: " << e.what() << std::endl;
    return -1;
  }
  return 0;
}

