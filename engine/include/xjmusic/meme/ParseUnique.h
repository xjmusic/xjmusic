// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJMUSIC_ENTITIES_MEME_PARSE_UNIQUE_H
#define XJMUSIC_ENTITIES_MEME_PARSE_UNIQUE_H

#include <regex>
#include <string>
#include <vector>


namespace XJ {

  /**
   Meme Matcher for Unique-Memes
   <p>
   Parse any meme to test if it's valid, and extract its features
   <p>
   Artist can add `$MEME` so only one is chosen https://github.com/xjmusic/xjmusic/issues/219
   */
  class ParseUnique {
  public:
    static const std::regex rgx;
    std::string body;
    bool valid;
    explicit ParseUnique(const std::string &raw);
    static ParseUnique fromString(const std::string &raw);
    [[nodiscard]] bool isViolatedBy(const ParseUnique &target) const;
    bool isAllowed(const std::vector<ParseUnique> &memes) const;
  };

}//namespace XJ

#endif//XJMUSIC_ENTITIES_MEME_PARSE_UNIQUE_H