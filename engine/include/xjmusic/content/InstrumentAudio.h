// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef INSTRUMENT_AUDIO_H
#define INSTRUMENT_AUDIO_H

#include <string>
#include <utility>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

namespace XJ {

  class InstrumentAudio : public ContentEntity {
  public:

    InstrumentAudio() = default;

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
