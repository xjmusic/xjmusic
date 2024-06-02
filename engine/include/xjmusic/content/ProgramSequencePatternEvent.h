// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_SEQUENCE_PATTERN_EVENT_H
#define PROGRAM_SEQUENCE_PATTERN_EVENT_H

#include <string>
#include <utility>

#include "Entity.h"

namespace Content {

  class ProgramSequencePatternEvent : public Entity {
  public:
    ProgramSequencePatternEvent() = default;
    UUID programId;
    std::string programSequencePatternId;
    std::string programVoiceTrackId;
    float velocity{};
    float position{};
    float duration{};
    std::string tones;
  };

}// namespace Content

#endif//PROGRAM_SEQUENCE_PATTERN_EVENT_H
