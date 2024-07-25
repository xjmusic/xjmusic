// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_XJ_PLAYER_H
#define XJMUSIC_XJ_PLAYER_H

#include "xjmusic/Engine.h"

#include "EngineUiBase.h"

using namespace XJ;
using namespace ftxui;

class XJPlayer : public EngineUiBase {
public:
  /**
  * Construct a new XJPlayer
  * @param pathToProjectFile     path to the .xj project file from which to load content
  * @param controlMode      the fabrication control mode
  * @param craftAheadSeconds (optional) how many seconds ahead to craft
  * @param dubAheadSeconds  (optional) how many seconds ahead to dub
  * @param persistenceWindowSeconds (optional) how long to keep segments in memory
  */
  explicit XJPlayer(
      const std::string &pathToProjectFile,
      Fabricator::ControlMode controlMode,
      std::optional<int> craftAheadSeconds,
      std::optional<int> dubAheadSeconds,
      std::optional<int> persistenceWindowSeconds
      );

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