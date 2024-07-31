// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <utility>

#include "xjmusic/audio/AudioScheduleEvent.h"

namespace XJ {

// Map and reverse-map of AudioScheduleEvent::EType enum values to their string representations
  static const std::map<AudioScheduleEvent::EType, std::string> typeValueNames = {
      {AudioScheduleEvent::EType::Create,  "Create"},
      {AudioScheduleEvent::EType::Update,  "Update"},
      {AudioScheduleEvent::EType::Delete,  "Delete"},
  };
  static const std::map<std::string, AudioScheduleEvent::EType> typeNameValues = EntityUtils::reverseMap(typeValueNames);

  AudioScheduleEvent::AudioScheduleEvent(
      const AudioScheduleEvent::EType type,
      ActiveAudio audio)
      : type(type),
        schedule(std::move(audio)) {}

  std::string AudioScheduleEvent::toString(const AudioScheduleEvent::EType &type) {
    return typeValueNames.at(type);
  }

  AudioScheduleEvent::EType AudioScheduleEvent::parseType(const std::string &value) {
    if (typeNameValues.count(value) == 0) {
      return AudioScheduleEvent::EType::Update;
    }
    return typeNameValues.at(value);
  }


} // XJ