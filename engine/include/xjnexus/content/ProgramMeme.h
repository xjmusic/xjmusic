// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_MEME_H
#define PROGRAM_MEME_H

#include <string>
#include <utility>

#include "Entity.h"

namespace Content {

  class ProgramMeme : public Entity {
  public:
    ProgramMeme() = default;
    UUID programId;
    std::string name;
  };

}// namespace Content

#endif//PROGRAM_MEME_H
