// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include "xjmusic/entities/meme/ParseAnti.h"

using namespace XJ;

ParseAnti::ParseAnti(const std::string &raw) {
  std::smatch matcher;
  isValid = std::regex_search(raw, matcher, rgx);

  if (!isValid) {
    body = raw;
    return;
  }

  body = matcher[1].str();
  if (body.empty()) {
    isValid = false;
    return;
  }
}

ParseAnti ParseAnti::fromString(const std::string &raw) {
  return ParseAnti(raw);
}

bool ParseAnti::isViolatedBy(const ParseAnti &target) {
  return (isValid && !target.isValid && body == target.body) ||
         (!isValid && target.isValid && body == target.body);
}

bool ParseAnti::isAllowed(const std::vector<ParseAnti> &memes) {
  return std::all_of(memes.begin(), memes.end(), [this](const ParseAnti &meme) {
    return !isViolatedBy(meme);
  });
}
