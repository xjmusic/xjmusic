// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_XJ_PLAYER_H
#define XJMUSIC_XJ_PLAYER_H

#include <memory>

#include "xjmusic/Engine.h"

#include "App.h"

using namespace XJ;
using namespace ftxui;

class XJPlayer : public App {
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
   * Run the engine for the given template until quit
   * @param CurrentTemplate  The template to run.
   */
  void RunEngine(const Template *CurrentTemplate);

  /**
   * Run one engine cycle.
   */
  void RunEngineCycle();

};

#endif //XJMUSIC_XJ_PLAYER_H
