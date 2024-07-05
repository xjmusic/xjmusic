// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_INSTRUMENT_AUDIO_H
#define XJMUSIC_INSTRUMENT_AUDIO_H

#include <string>

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

  /**
   * Parse a InstrumentAudio from a JSON object
   * @param json  input
   * @param entity  output
   */
  inline void from_json(const json &json, InstrumentAudio &entity) {
    EntityUtils::setRequired(json, "id", entity.id);
    EntityUtils::setRequired(json, "instrumentId", entity.instrumentId);
    EntityUtils::setIfNotNull(json, "name", entity.name);
    EntityUtils::setIfNotNull(json, "waveformKey", entity.waveformKey);
    EntityUtils::setIfNotNull(json, "transientSeconds", entity.transientSeconds);
    EntityUtils::setIfNotNull(json, "loopBeats", entity.loopBeats);
    EntityUtils::setIfNotNull(json, "tempo", entity.tempo);
    EntityUtils::setIfNotNull(json, "intensity", entity.intensity);
    EntityUtils::setIfNotNull(json, "event", entity.event);
    EntityUtils::setIfNotNull(json, "volume", entity.volume);
    EntityUtils::setIfNotNull(json, "tones", entity.tones);
  }

}// namespace XJ

#endif//XJMUSIC_INSTRUMENT_AUDIO_H
