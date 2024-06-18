// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_SEQUENCE_BINDING_MEME_H
#define PROGRAM_SEQUENCE_BINDING_MEME_H

#include <string>
#include <utility>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

namespace XJ {

  class ProgramSequenceBindingMeme : public ContentEntity {
  public:

    ProgramSequenceBindingMeme() = default;

    UUID programId;
    std::string programSequenceBindingId;
    std::string name;
  };

}// namespace XJ

#endif//PROGRAM_SEQUENCE_BINDING_MEME_H
