// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_SEQUENCE_BINDING_H
#define PROGRAM_SEQUENCE_BINDING_H

#include <string>
#include <utility>

#include "Entity.h"

namespace Content {

  class ProgramSequenceBinding : public Entity {
  public:
    ProgramSequenceBinding() = default;
    UUID programId;
    std::string programSequenceId;
    int offset{};
  };

}// namespace Content

#endif//PROGRAM_SEQUENCE_BINDING_H
