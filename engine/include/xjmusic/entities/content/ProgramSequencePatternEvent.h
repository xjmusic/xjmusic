// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_SEQUENCE_PATTERN_EVENT_H
#define PROGRAM_SEQUENCE_PATTERN_EVENT_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"

namespace XJ {

  class ProgramSequencePatternEvent : public Entity {
  public:

    ProgramSequencePatternEvent() = default;

    UUID id;
    UUID programId;
    std::string programSequencePatternId;
    std::string programVoiceTrackId;
    float velocity{};
    float position{};
    float duration{};
    std::string tones;
  };

}// namespace XJ

#endif//PROGRAM_SEQUENCE_PATTERN_EVENT_H
