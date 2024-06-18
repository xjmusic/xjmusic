// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_SEQUENCE_CHORD_H
#define PROGRAM_SEQUENCE_CHORD_H

#include <string>
#include <utility>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

namespace XJ {

  class ProgramSequenceChord : public ContentEntity {
  public:

    ProgramSequenceChord() = default;

    UUID programId;
    std::string programSequenceId;
    std::string name;
    float position{};
  };

}// namespace XJ

#endif//PROGRAM_SEQUENCE_CHORD_H
