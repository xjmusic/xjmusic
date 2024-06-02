// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_VOICE_H
#define PROGRAM_VOICE_H

#include <string>
#include <utility>

#include "Instrument.h"

namespace Content {

  class ProgramVoice : public Entity {
  public:
    ProgramVoice() = default;
    UUID programId;
    Instrument::Type type{Instrument::Type::Drum};
    std::string name;
    float order{};
  };

}// namespace Content

#endif//PROGRAM_VOICE_H
