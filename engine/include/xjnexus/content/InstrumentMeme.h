// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef INSTRUMENT_MEME_H
#define INSTRUMENT_MEME_H

#include <string>
#include <utility>

#include "Entity.h"

namespace Content {

  class InstrumentMeme : public Entity {
  public:
    InstrumentMeme() = default;
    UUID instrumentId;
    std::string name;
  };

}// namespace Content

#endif//INSTRUMENT_MEME_H
