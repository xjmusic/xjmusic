// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <SDL2/SDL.h>

#include <iostream>

#include <ftxui/screen/screen.hpp>
#include <ftxui/component/component.hpp>
#include <ftxui/component/screen_interactive.hpp>

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
static const Template *SelectTemplate(Engine *XJ, ftxui::ScreenInteractive *screen) {
  using namespace ftxui;

  std::cout << "[Templates]" << std::endl;
  std::vector<const Template *> AllTemplates;
  for (const Template *Template: XJ->getProjectContent()->getTemplates()) {
    AllTemplates.push_back(Template);
  }
  std::sort(AllTemplates.begin(), AllTemplates.end(), [](const Template *a, const Template *b) {
    return a->name < b->name;
  });

  // Extract template names
  std::vector<std::string> templateNames;
  for (const auto& tmpl : AllTemplates) {
    templateNames.push_back(tmpl->name);
  }

  // Define the selected index
  int selectedTemplateIndex = 0;

  // Create the radio menu
  auto radio = Radiobox(&templateNames, &selectedTemplateIndex);

  // todo instead of a button to confirm the selection, can we catch the enter key to submit the radio form? Is there a better type of form?
  // Create a button to confirm the selection
  auto button = Button("Select", [&] {
    screen->ExitLoopClosure();
  });

  // Compose the layout
  auto layout = Container::Vertical({
                                        radio,
                                        button,
                                    });

  // Create the screen
  screen->Loop(layout); // todo exit loop wheb selection is made

  // Return the selected template
  if (selectedTemplateIndex >= 0 && selectedTemplateIndex < AllTemplates.size()) {
    return AllTemplates[selectedTemplateIndex];
  } else {
    throw std::invalid_argument("Invalid template index");
  }
}

/**
 * Main entry point of the application.
 * @param argc  The number of arguments passed to the application.
 * @param argv  The arguments passed to the application.
 * @return    The exit code of the application.
 */
int main(int argc, char *argv[]) {
  using namespace ftxui;

  // Step 1: Create a ScreenInteractive instance
  auto screen = ScreenInteractive::TerminalOutput();

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
    const Template *CurrentTemplate = SelectTemplate(XJ.get(), &screen);
    Run(XJ.get(), CurrentTemplate);

  } catch (const std::exception &e) {
    std::cerr << "Error: " << e.what() << std::endl;
    return -1;
  }
  return 0;
}

