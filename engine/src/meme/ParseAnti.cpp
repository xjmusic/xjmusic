// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include <set>
#include "xjmusic/meme/ParseAnti.h"

using namespace XJ;

ParseAnti::ParseAnti(const std::string &raw) {
  std::smatch matcher;
  valid = std::regex_search(raw, matcher, rgx);

  if (!valid) {
    body = raw;
    return;
  }

  body = matcher[1].str();
  if (body.empty()) {
    valid = false;
    return;
  }
}

ParseAnti ParseAnti::fromString(const std::string &raw) {
  return ParseAnti(raw);
}

bool ParseAnti::isViolatedBy(const ParseAnti &target) const {
  return (valid && !target.valid && body == target.body) ||
         (!valid && target.valid && body == target.body);
}

bool ParseAnti::isAllowed(const std::vector<ParseAnti> &memes) const {
  return std::all_of(memes.begin(), memes.end(), [this](const ParseAnti &meme) {
    return !isViolatedBy(meme);
  });
}

const std::regex ParseAnti::rgx("^!(.+)$");
