// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_SEQUENCE_BINDING_H
#define PROGRAM_SEQUENCE_BINDING_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"

namespace XJ {

  class ProgramSequenceBinding : public Entity {
  public:

    ProgramSequenceBinding() = default;

    UUID id;
    UUID programId;
    std::string programSequenceId;
    int offset{};
  };

}// namespace XJ

#endif//PROGRAM_SEQUENCE_BINDING_H
