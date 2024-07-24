// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_XJ_PLAYER_H
#define XJMUSIC_XJ_PLAYER_H

#include <memory>

#include <ftxui/component/screen_interactive.hpp>

#include "xjmusic/Engine.h"

using namespace XJ;
using namespace ftxui;

class XJPlayer {
  std::unique_ptr<Engine> engine;
  ScreenInteractive screen;

public:

  /**
   * Construct a new XJPlayer.
   * @param pathToProjectFile  The path to the .xj project file from which to load content.
   */
  explicit XJPlayer(const std::string &pathToProjectFile);

  /**
   * Starts the XJPlayer.
   */
  void Start();

protected:
  /**
   * The running state of the XJPlayer.
   */
  bool running = true;

  /**
   * The elapsed time in milliseconds since starting playback.
   */
  Uint32 ElapsedMillis{};

  /**
   * Selects a template from the Engine.
   * @param XJ  The Engine to select the template from.
   * @return  The selected template.
   */
  const Template *SelectTemplate();

  /**
   * Runs the engine
   * @param CurrentTemplate  The template to run.
   */
  void RunEngine(const Template *CurrentTemplate);

  /**
   * Shows the running UI.
   * This should be run on a separate thread.
   */
  std::shared_ptr<ComponentBase> BuildRunningUI();
};

#endif //XJMUSIC_XJ_PLAYER_H
