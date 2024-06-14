// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJ_MUSIC_ENTITIES_MEME_PARSE_STRONG_H
#define XJ_MUSIC_ENTITIES_MEME_PARSE_STRONG_H

#include <regex>
#include <vector>
#include <string>
#include <set>

namespace XJ {

  /**
   Meme Matcher for Strong-Memes
   <p>
   Parse any meme to test if it's valid, and extract its features
   <p>
   Strong-meme like LEMONS! should always favor LEMONS https://github.com/xjmusic/xjmusic/issues/218
   */
  class ParseStrong {
  public:
    static const std::regex rgx;
    std::string body;
    bool valid;
    explicit ParseStrong(const std::string &raw);
    static ParseStrong fromString(const std::string &raw);
    bool isAllowed(const std::vector<ParseStrong> &memes);
  };

}

#endif//XJ_MUSIC_ENTITIES_MEME_PARSE_STRONG_H