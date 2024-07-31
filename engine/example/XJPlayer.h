// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_XJ_PLAYER_H
#define XJMUSIC_XJ_PLAYER_H

#include "xjmusic/Engine.h"

#include "EngineUiBase.h"

using namespace XJ;
using namespace ftxui;

class XJPlayer : public EngineUiBase {
  std::optional<std::string> templateName;
  SDL_AudioSpec outputSpec{};

public:
  /**
  * Construct a new XJPlayer
  * @param pathToProjectFile     path to the .xj project file from which to load content
  * @param controlMode      the fabrication control mode
  * @param templateName     (optional) the name of the template to start with
  * @param craftAheadSeconds (optional) how many seconds ahead to craft
  * @param dubAheadSeconds  (optional) how many seconds ahead to dub
  * @param deadlineSeconds  (optional) audio scheduling deadline in seconds
  * @param persistenceWindowSeconds (optional) how long to keep segments in memory
  */
  explicit XJPlayer(
      const std::string &pathToProjectFile,
      Fabricator::ControlMode controlMode,
      std::optional<std::string> templateName,
      std::optional<int> craftAheadSeconds,
      std::optional<int> dubAheadSeconds,
      std::optional<int> deadlineSeconds,
      std::optional<int> persistenceWindowSeconds,
      const SDL_AudioSpec &outputSpec);

  /**
   * Starts the XJPlayer.
   */
  void Start();

protected:

  /**
   * Run the engine for the given template until quit
   * @param templateIdentifier  The template to run.
   */
  void RunEngine(const std::string &templateIdentifier);

  /**
   * Run one engine cycle.
   * @param deviceId  The audio device ID.
   */
  void RunEngineCycle(SDL_AudioDeviceID deviceId);

};

#endif //XJMUSIC_XJ_PLAYER_H
