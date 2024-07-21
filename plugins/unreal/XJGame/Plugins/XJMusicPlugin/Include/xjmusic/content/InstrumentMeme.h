// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_INSTRUMENT_MEME_H
#define XJMUSIC_INSTRUMENT_MEME_H

#include <string>
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

  /**
   * Parse a InstrumentMeme from a JSON object
   * @param json  input
   * @param entity  output
   */
  inline void from_json(const json &json, InstrumentMeme &entity) {
    EntityUtils::setRequired(json, "id", entity.id);
    EntityUtils::setRequired(json, "instrumentId", entity.instrumentId);
    EntityUtils::setIfNotNull(json, "name", entity.name);
  }

}// namespace XJ

#endif//XJMUSIC_INSTRUMENT_MEME_H
