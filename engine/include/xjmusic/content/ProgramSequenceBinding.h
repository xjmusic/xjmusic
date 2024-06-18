// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_SEQUENCE_BINDING_H
#define PROGRAM_SEQUENCE_BINDING_H

#include <string>
#include <utility>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

namespace XJ {

  class ProgramSequenceBinding : public ContentEntity {
  public:

    ProgramSequenceBinding() = default;

    UUID programId;
    std::string programSequenceId;
    int offset{};
  };

}// namespace XJ

#endif//PROGRAM_SEQUENCE_BINDING_H
