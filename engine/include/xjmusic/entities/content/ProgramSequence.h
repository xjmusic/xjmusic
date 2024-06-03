// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_SEQUENCE_H
#define PROGRAM_SEQUENCE_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"

namespace XJ {

  class ProgramSequence : public Entity {
  public:

    ProgramSequence() = default;

    UUID id;
    UUID programId;
    std::string name;
    std::string key;
    float intensity{};
    int total{};
  };

}// namespace XJ

#endif//PROGRAM_SEQUENCE_H
