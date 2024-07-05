// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_PROGRAM_SEQUENCE_BINDING_MEME_H
#define XJMUSIC_PROGRAM_SEQUENCE_BINDING_MEME_H

#include <string>
#include <utility>
#include <set>

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

namespace XJ {

  class ProgramSequenceBindingMeme : public ContentEntity {
  public:

    ProgramSequenceBindingMeme() = default;

    UUID programId;
    std::string programSequenceBindingId;
    std::string name;

    /**
     * Get the names of a set of ProgramSequenceBinding Memes
     * @param programSequenceBindingMemes  The set of ProgramSequenceBinding Memes
     * @return       The names
     */
    static std::set<std::string> getNames(const std::set<ProgramSequenceBindingMeme> &programSequenceBindingMemes);

  };

  /**
   * Parse a ProgramSequenceBindingMeme from a JSON object
   * @param json  input
   * @param entity  output
   */
  void from_json(const json &json, ProgramSequenceBindingMeme &entity) {
    EntityUtils::setRequired(json, "id", entity.id);
    EntityUtils::setRequired(json, "programId", entity.programId);
    EntityUtils::setRequired(json, "programSequenceBindingId", entity.programSequenceBindingId);
    EntityUtils::setIfNotNull(json, "name", entity.name);
  }

}// namespace XJ

#endif//XJMUSIC_PROGRAM_SEQUENCE_BINDING_MEME_H
