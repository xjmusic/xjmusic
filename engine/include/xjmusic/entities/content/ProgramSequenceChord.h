// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_SEQUENCE_CHORD_H
#define PROGRAM_SEQUENCE_CHORD_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"

namespace XJ {

  class ProgramSequenceChord : public Entity {
  public:

    ProgramSequenceChord() = default;

    UUID id;
    UUID programId;
    std::string programSequenceId;
    std::string name;
    float position{};
  };

}// namespace XJ

#endif//PROGRAM_SEQUENCE_CHORD_H