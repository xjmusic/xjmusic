// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_SEQUENCE_BINDING_MEME_H
#define PROGRAM_SEQUENCE_BINDING_MEME_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"

namespace XJ {

  class ProgramSequenceBindingMeme : public Entity {
  public:

    ProgramSequenceBindingMeme() = default;

    UUID id;
    UUID programId;
    std::string programSequenceBindingId;
    std::string name;
  };

}// namespace XJ

#endif//PROGRAM_SEQUENCE_BINDING_MEME_H
