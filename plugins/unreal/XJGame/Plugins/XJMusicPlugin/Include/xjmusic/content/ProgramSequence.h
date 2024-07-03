// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_SEQUENCE_H
#define PROGRAM_SEQUENCE_H

#include <string>
#include <utility>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

namespace XJ {

  class ProgramSequence : public ContentEntity {
  public:

    ProgramSequence() = default;

    UUID programId;
    std::string name;
    std::string key;
    float intensity{};
    int total{};
  };

}// namespace XJ

#endif//PROGRAM_SEQUENCE_H
