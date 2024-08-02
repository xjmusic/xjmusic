// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_ENGINE_AUDIO_PLAYBACK_H
#define XJMUSIC_ENGINE_AUDIO_PLAYBACK_H

#include "xjmusic/audio/ActiveAudio.h"

using namespace XJ;

// Assuming EngineAudioSchedule is a class that handles audio playback
class EngineScheduledAudio : public ActiveAudio {
public:
  EngineScheduledAudio(
      const std::filesystem::path& audioPathPrefix,
      SDL_AudioDeviceID deviceId,
      const ActiveAudio &activeAudio
  );

  void Play();

  void Update(const ActiveAudio &newActiveAudio);

  void Stop();
  // Other necessary methods and members
private:
  SDL_AudioSpec wavSpec{};
  Uint32 wavLength{};
  Uint8 *wavBuffer{};
  SDL_AudioDeviceID deviceId;
};


#endif //XJMUSIC_ENGINE_AUDIO_PLAYBACK_H
