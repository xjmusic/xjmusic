// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJMUSIC_ENTITIES_MEME_PARSE_NUMERIC_H
#define XJMUSIC_ENTITIES_MEME_PARSE_NUMERIC_H

#include <regex>
#include <vector>
#include <string>

namespace XJ {

  /**
   Meme Matcher for Numeric Memes
   <p>
   Parse any meme to test if it's valid, and extract its features
   <p>
   Numeric memes with common letters and different integer prefix (e.g. 2STEP vs 4STEP) are known to be exclusive https://github.com/xjmusic/xjmusic/issues/217
   */
  class ParseNumeric {
  public:
    static const std::regex rgx;
    std::string body;
    int prefix;
    bool valid;
    explicit ParseNumeric(const std::string &raw);
    static ParseNumeric fromString(const std::string &raw);
    [[nodiscard]] bool isViolatedBy(const ParseNumeric &target) const;
    bool isAllowed(const std::vector<ParseNumeric> &memes) const;
  };


}

#endif//XJMUSIC_ENTITIES_MEME_PARSE_NUMERIC_H