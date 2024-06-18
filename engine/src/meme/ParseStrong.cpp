// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include "xjmusic/meme/ParseStrong.h"

using namespace XJ;

ParseStrong::ParseStrong(const std::string &raw) {
  std::smatch matcher;
  valid = std::regex_search(raw, matcher, rgx);

  if (!valid) {
    body = raw;
    valid = false;
    return;
  }

  body = matcher[1].str();
  if (body.empty()) {
    valid = false;
    return;
  }

  valid = true;
}

ParseStrong ParseStrong::fromString(const std::string &raw) {
  return ParseStrong(raw);
}

bool ParseStrong::isAllowed(const std::vector<ParseStrong> &memes) {
  if (!valid) return true;
  return !std::all_of(memes.begin(), memes.end(), [this](const ParseStrong &meme) {
    return body != meme.body;
  });
}

const std::regex ParseStrong::rgx("^(.+)!$");
