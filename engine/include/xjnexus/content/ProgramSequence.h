// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_SEQUENCE_H
#define PROGRAM_SEQUENCE_H

#include <string>
#include <utility>

#include "Entity.h"

namespace Content {

  class ProgramSequence : public Entity {
  public:
    ProgramSequence() = default;
    UUID programId;
    std::string name;
    std::string key;
    float intensity{};
    int total{};
  };

}// namespace Content

#endif//PROGRAM_SEQUENCE_H
