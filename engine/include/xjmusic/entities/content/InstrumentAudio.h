// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef INSTRUMENT_AUDIO_H
#define INSTRUMENT_AUDIO_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"

namespace XJ {

  class InstrumentAudio : public Entity {
  public:

    InstrumentAudio() = default;

    UUID id;
    UUID instrumentId;
    std::string name;
    std::string waveformKey;
    float transientSeconds{};
    float loopBeats{};
    float tempo{};
    float intensity{};
    std::string event;
    float volume{};
    std::string tones;
  };

}// namespace XJ

#endif//INSTRUMENT_AUDIO_H
