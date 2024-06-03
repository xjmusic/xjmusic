// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef INSTRUMENT_MEME_H
#define INSTRUMENT_MEME_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"

namespace XJ {

  class InstrumentMeme : public Entity {
  public:

    InstrumentMeme() = default;

    UUID id;
    UUID instrumentId;
    std::string name;
  };

}// namespace XJ

#endif//INSTRUMENT_MEME_H
