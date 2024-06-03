// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_VOICE_TRACK_H
#define PROGRAM_VOICE_TRACK_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"

namespace XJ {

  class ProgramVoiceTrack : public Entity {
  public:

    ProgramVoiceTrack() = default;

    UUID id;
    UUID programId;
    std::string programVoiceId;
    std::string name;
    float order{};
  };

}// namespace XJ

#endif//PROGRAM_VOICE_TRACK_H
