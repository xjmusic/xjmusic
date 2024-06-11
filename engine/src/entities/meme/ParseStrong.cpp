// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include "xjmusic/entities/meme/ParseStrong.h"

using namespace XJ;

ParseStrong::ParseStrong(const std::string &raw) {
  std::smatch matcher;
  isValid = std::regex_search(raw, matcher, rgx);

  if (!isValid) {
    body = raw;
    isValid = false;
    return;
  }

  body = matcher[1].str();
  if (body.empty()) {
    isValid = false;
    return;
  }

  isValid = true;
}

ParseStrong ParseStrong::fromString(const std::string &raw) {
  return ParseStrong(raw);
}

bool ParseStrong::isAllowed(const std::vector<ParseStrong> &memes) {
  if (!isValid) return true;
  return !std::all_of(memes.begin(), memes.end(), [this](const ParseStrong &meme) {
    return body != meme.body;
  });
}
