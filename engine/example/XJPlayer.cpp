// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.


#include <iostream>

#include <SDL2/SDL.h>
#include <ftxui/component/component.hpp>
#include <ftxui/component/screen_interactive.hpp>
#include <utility>

#include "xjmusic/Engine.h"

#include "XJPlayer.h"


XJPlayer::XJPlayer(
    const std::string &pathToProjectFile,
    Fabricator::ControlMode controlMode,
    std::optional<std::string> templateName,
    std::optional<int> craftAheadSeconds,
    std::optional<int> dubAheadSeconds,
    std::optional<int> deadlineSeconds,
    std::optional<int> persistenceWindowSeconds) : EngineUiBase(pathToProjectFile,
                                                                controlMode,
                                                                craftAheadSeconds,
                                                                dubAheadSeconds,
                                                                deadlineSeconds,
                                                                persistenceWindowSeconds
) {
  this->templateName = std::move(templateName);
}

void XJPlayer::Start() {
  RunEngine(templateName.has_value() ? templateName.value() : SelectTemplate()->id);
}

void XJPlayer::RunEngine(const std::string &templateIdentifier) {
  // Use SDL to open an audio output buffer
  if (SDL_Init(SDL_INIT_AUDIO) < 0) {
    std::cerr << "SDL_Init failed: " << SDL_GetError() << std::endl;
    return;
  }

  // Set up an exit condition
  screen.Post([&] { running = false; });

  // Start the engine
  engine->start(templateIdentifier);

  // Spin off the screen UI on its own thread
  auto document = BuildRunningUI();
  std::thread runEngineThread([this] {
    // Record the current start time
    const Uint32 StartTime = SDL_GetTicks();

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
      AtChainMicros = (SDL_GetTicks() - StartTime) * MICROS_PER_MILLI;

      // Run the engine cycle
      RunEngineCycle();

      // Update the screen
      screen.PostEvent(ftxui::Event::Custom);

      // Sleep for a bit
      SDL_Delay(CYCLE_MILLISECONDS);
    }
    screen.Exit();
  });
  runEngineThread.detach(); // Detach the thread to run independently

  screen.Loop(document);
}

void XJPlayer::RunEngineCycle() {
  for (AudioScheduleEvent event : engine->RunCycle(AtChainMicros)) {
    switch (event.type) {
      case AudioScheduleEvent::EType::Create:
        // NEXT: implement XJPlayer creating a new scheduled audio
        ActiveAudios.emplace(event.audio.getId(), event.audio);
        break;
      case AudioScheduleEvent::EType::Update:
        // NEXT: implement XJPlayer updating an existing scheduled audio
        ActiveAudios.emplace(event.audio.getId(), event.audio);
        break;
      case AudioScheduleEvent::EType::Delete:
        // NEXT: implement XJPlayer deleting a scheduled audio
        ActiveAudios.erase(event.audio.getId());
        break;
    }
  }
}


