// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_MEME_H
#define PROGRAM_MEME_H

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

}// namespace XJ

#endif//PROGRAM_MEME_H
