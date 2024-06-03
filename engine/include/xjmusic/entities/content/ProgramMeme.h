// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_MEME_H
#define PROGRAM_MEME_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"

namespace XJ {

  class ProgramMeme : public Entity {
  public:

    ProgramMeme() = default;

    UUID id;
    UUID programId;
    std::string name;
  };

}// namespace XJ

#endif//PROGRAM_MEME_H
