// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJMUSIC_ENTITIES_MEME_PARSE_UNIQUE_H
#define XJMUSIC_ENTITIES_MEME_PARSE_UNIQUE_H

#include <regex>
#include <vector>
#include <string>


namespace XJ {

  /**
   Meme Matcher for Unique-Memes
   <p>
   Parse any meme to test if it's valid, and extract its features
   <p>
   Artist can add `$MEME` so only one is chosen https://github.com/xjmusic/workstation/issues/219
   */
  class ParseUnique {
    static const std::regex rgx;
    std::string body;
    bool isValid;

  public:
    explicit ParseUnique(const std::string &raw);

    static ParseUnique fromString(const std::string &raw);

    bool isViolatedBy(const ParseUnique &target);

    bool isAllowed(const std::vector<ParseUnique> &memes);
  };

  const std::regex ParseUnique::rgx("^\\$(.+)$");

}//namespace XJ

#endif//XJMUSIC_ENTITIES_MEME_PARSE_UNIQUE_H