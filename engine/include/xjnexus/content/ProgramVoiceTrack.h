// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_VOICE_TRACK_H
#define PROGRAM_VOICE_TRACK_H

#include <string>
#include <utility>

#include "Entity.h"

namespace Content {

  class ProgramVoiceTrack : public Entity {
  public:
    ProgramVoiceTrack() = default;
    UUID programId;
    std::string programVoiceId;
    std::string name;
    float order{};
  };

}// namespace Content

#endif//PROGRAM_VOICE_TRACK_H
