// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJMUSIC_ENTITIES_MEME_PARSE_ANTI_H
#define XJMUSIC_ENTITIES_MEME_PARSE_ANTI_H

#include <regex>
#include <vector>
#include <string>

namespace XJ {

  /**
   Meme Matcher for Anti-Memes
   <p>
   Parse any meme to test if it's valid, and extract its features
   <p>
   Artist can add !MEME values into Programs https://github.com/xjmusic/workstation/issues/214
   */
  class ParseAnti {
    static const std::regex rgx;
    std::string body;
    bool isValid;

  public:
    explicit ParseAnti(const std::string& raw);

    static ParseAnti fromString(const std::string& raw);

    bool isViolatedBy(const ParseAnti &target);

    bool isAllowed(const std::vector<ParseAnti> &memes);
  };

  const std::regex ParseAnti::rgx("^!(.+)$");
}

#endif//XJMUSIC_ENTITIES_MEME_PARSE_ANTI_H