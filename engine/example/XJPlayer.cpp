// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <SDL2/SDL.h>

#include <iostream>

#include <ftxui/component/component.hpp>
#include <ftxui/component/screen_interactive.hpp>

#include "xjmusic/Engine.h"
#include "xjmusic/util/CsvUtils.h"
#include "XJPlayer.h"


const Uint32 MICROSECONDS_PER_MILLISECOND = 1000;

const Uint32 CYCLE_MILLISECONDS = 100;

void XJPlayer::RunEngine(const Template *CurrentTemplate) {
  // Use SDL to open an audio output buffer
  if (SDL_Init(SDL_INIT_AUDIO) < 0) {
    std::cerr << "SDL_Init failed: " << SDL_GetError() << std::endl;
    return;
  }

  // Set up an exit condition
  screen.Post([&] { running = false; });

  // Record the current start time
  const Uint32 StartTime = SDL_GetTicks();

  // Start the engine
  engine->start(CurrentTemplate->id);

  // Spin off the screen UI on its own thread
  auto document = BuildRunningUI();
  std::thread uiThread([this, &document] { screen.Loop(document); });
  uiThread.detach(); // Detach the thread to run independently

  // The main loop
  while (running) {
    // Process events
    SDL_Event Event;
    while (SDL_PollEvent(&Event)) {
      switch (Event.type) {
        case SDL_QUIT:
          running = false;
          break;
        default:
          break;
      }
    }

    // Calculate the time since the start
    ElapsedMillis = SDL_GetTicks() - StartTime;

    // Update the XJ
    std::set<ActiveAudio> ActiveAudios = engine->RunCycle(ElapsedMillis * MICROSECONDS_PER_MILLISECOND);

/*
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
*/

    screen.Print();

    // Sleep for a bit
    SDL_Delay(CYCLE_MILLISECONDS);
  }
  screen.Exit();
}

const Template *XJPlayer::SelectTemplate() {
  using namespace ftxui;

  std::vector<const Template *> AllTemplates;
  for (const Template *Template: engine->getProjectContent()->getTemplates()) {
    AllTemplates.push_back(Template);
  }
  std::sort(AllTemplates.begin(), AllTemplates.end(), [](const Template *a, const Template *b) {
    return a->name < b->name;
  });

  // Extract template names
  std::vector<std::string> templateNames;
  templateNames.reserve(AllTemplates.size());
  for (const auto &tmpl: AllTemplates) {
    templateNames.push_back(tmpl->name);
  }

  int selected = 0;
  MenuOption option;
  option.on_enter = screen.ExitLoopClosure();

  auto header_text = Renderer([] {
    return text("Select a template and press ENTER") | bold;
  });
  auto template_menu = Menu(&templateNames, &selected, option);
  auto container = Container::Vertical({
                                           header_text,
                                           template_menu,
                                       });

  auto document = Renderer(container, [&] {
    return vbox({
                    header_text->Render(),
                    separator(),
                    template_menu->Render(),
                }) |
           border;
  });

  screen.Clear();
  screen.Loop(document);

  // Return the selected template
  if (selected >= 0 && selected < AllTemplates.size()) {
    return AllTemplates[selected];
  } else {
    throw std::invalid_argument("Invalid template index");
  }
}

XJPlayer::XJPlayer(const std::string &pathToProjectFile)
    : engine(std::make_unique<Engine>(
    pathToProjectFile,
    Fabricator::ControlMode::Auto,
    std::nullopt,
    std::nullopt,
    std::nullopt
)), screen(ScreenInteractive::TerminalOutput()) {
  ElapsedMillis = 0;
}

void XJPlayer::Start() {
  RunEngine(SelectTemplate());
}

std::shared_ptr<ComponentBase> XJPlayer::BuildRunningUI() {
  std::vector<std::string> tab_values{
      "stats",
      "sounds",
      "content",
  };
  auto tab_stats_placeholder = Renderer([this] {
    return vbox({
                    text("Time Elapsed: " + std::to_string(ElapsedMillis / 1000) + "s"),
                    text("Meme Taxonomy: " + engine->getProjectContent()->getMemeTaxonomy().toString()),
                });
  });

  auto tab_sounds_placeholder = Renderer([] {
    return text("Sounds placeholder");
  });

  auto tab_content_placeholder = Renderer([] {
    return text("Content placeholder");
  });

  int tab_selected = 0;
  auto tab_toggle = Toggle(&tab_values, &tab_selected);
  auto tab_container = Container::Tab(
      {
          tab_stats_placeholder,
          tab_sounds_placeholder,
          tab_content_placeholder,
      },
      &tab_selected);

  auto container = Container::Vertical({
                                           tab_toggle,
                                           tab_container,
                                       });

  auto document = Renderer(container, [&] {
    return vbox({
                    tab_toggle->Render(),
                    separator(),
                    tab_container->Render(),
                }) |
           border;
  });

  return document;
}
