// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <SDL2/SDL.h>
#include <unordered_map>
#include <string>
#include <stdexcept>
#include <utility>

#include "EngineScheduledAudio.h"

EngineScheduledAudio::EngineScheduledAudio(
    const std::filesystem::path& audioPathPrefix,
    SDL_AudioDeviceID deviceId,
    const ActiveAudio &activeAudio
) : ActiveAudio(
    activeAudio.getPick(),
    activeAudio.getInstrument(),
    activeAudio.getAudio(),
    activeAudio.getStartAtChainMicros(),
    activeAudio.getStopAtChainMicros(),
    activeAudio.getFromVolume(),
    activeAudio.getToVolume()
) {
  std::string filePath = audioPathPrefix.string() + activeAudio.getAudio()->waveformKey;
  this->deviceId = deviceId;
  if (SDL_LoadWAV(filePath.c_str(), &wavSpec, &wavBuffer, &wavLength) == nullptr) {
    throw std::runtime_error("Failed to load audio waveform file: " + filePath);
  }
}

void EngineScheduledAudio::Play() {
  // TODO actual playback properties
  SDL_QueueAudio(deviceId, wavBuffer, wavLength);
  SDL_PauseAudioDevice(deviceId, 0);
}

void EngineScheduledAudio::Update(const ActiveAudio &newActiveAudio) {
  pick = newActiveAudio.getPick();
  instrument = newActiveAudio.getInstrument();
  audio = newActiveAudio.getAudio();
  startAtChainMicros = newActiveAudio.getStartAtChainMicros();
  stopAtChainMicros = newActiveAudio.getStopAtChainMicros();
  fromVolume = newActiveAudio.getFromVolume();
  toVolume = newActiveAudio.getToVolume();
  // TODO additional logic to handle updating the playback properties (volume and end time) if necessary
}

void EngineScheduledAudio::Stop() {
  // TODO additional logic to handle stopping the playback if necessary
  SDL_ClearQueuedAudio(deviceId);
  SDL_CloseAudioDevice(deviceId);
  SDL_FreeWAV(wavBuffer);
}
