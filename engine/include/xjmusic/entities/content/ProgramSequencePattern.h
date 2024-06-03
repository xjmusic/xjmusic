// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_SEQUENCE_PATTERN_H
#define PROGRAM_SEQUENCE_PATTERN_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"

namespace XJ {

  class ProgramSequencePattern : public Entity {
  public:
    ProgramSequencePattern() = default;
    UUID programId;
    std::string programSequenceId;
    std::string programVoiceId;
    std::string name;
    int total{};
  };

}// namespace XJ

#endif//PROGRAM_SEQUENCE_PATTERN_H
