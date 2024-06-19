// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef INSTRUMENT_MEME_H
#define INSTRUMENT_MEME_H

#include <string>
#include <utility>
#include <set>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

namespace XJ {

  class InstrumentMeme : public ContentEntity {
  public:

    InstrumentMeme() = default;

    UUID instrumentId;
    std::string name;

    /**
     * Get the names of a set of Instrument Memes
     * @param instrumentMemes  The set of Instrument Memes
     * @return       The names
     */
    static std::set<std::string> getNames(const std::set<const InstrumentMeme *>& instrumentMemes);
    
  };

}// namespace XJ

#endif//INSTRUMENT_MEME_H
