// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_PROGRAM_MEME_H
#define XJMUSIC_PROGRAM_MEME_H

#include <string>
#include <set>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

namespace XJ {

  class ProgramMeme : public ContentEntity {
  public:

    ProgramMeme() = default;

    UUID programId;
    std::string name;

    /**
     * Get the names of a set of Program Memes
     * @param programMemes  The set of Program Memes
     * @return       The names
     */
    static std::set<std::string> getNames(const std::set<const ProgramMeme *> &programMemes);

  };

  /**
   * Parse a ProgramMeme from a JSON object
   * @param json  input
   * @param entity  output
   */
  void from_json(const json &json, ProgramMeme &entity) {
    EntityUtils::setRequired(json, "id", entity.id);
    EntityUtils::setRequired(json, "programId", entity.programId);
    EntityUtils::setIfNotNull(json, "name", entity.name);
  }

}// namespace XJ

#endif//XJMUSIC_PROGRAM_MEME_H
