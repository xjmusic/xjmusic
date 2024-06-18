// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include "xjmusic/meme/ParseUnique.h"

using namespace XJ;

ParseUnique::ParseUnique(const std::string &raw) {
  std::smatch matcher;
  valid = std::regex_search(raw, matcher, rgx);

  if (!valid) {
    body = "";
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

ParseUnique ParseUnique::fromString(const std::string &raw) {
  return ParseUnique(raw);
}

bool ParseUnique::isViolatedBy(const ParseUnique &target) const {
  return valid && target.valid && body == target.body;
}

bool ParseUnique::isAllowed(const std::vector<ParseUnique> &memes) {
  return std::all_of(memes.begin(), memes.end(), [this](const ParseUnique &meme) {
    return !isViolatedBy(meme);
  });
}

const std::regex ParseUnique::rgx("^\\$(.+)$");
