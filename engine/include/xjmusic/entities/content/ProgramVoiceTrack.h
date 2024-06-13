// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_VOICE_TRACK_H
#define PROGRAM_VOICE_TRACK_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"
#include "ContentEntity.h"

namespace XJ {

  class ProgramVoiceTrack : public ContentEntity {
  public:

    ProgramVoiceTrack() = default;

    UUID programId;
    std::string programVoiceId;
    std::string name;
    float order{};
  };

}// namespace XJ

#endif//PROGRAM_VOICE_TRACK_H
