// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include "xjmusic/entities/meme/ParseUnique.h"

using namespace XJ;

ParseUnique::ParseUnique(const std::string &raw) {
  std::smatch matcher;
  isValid = std::regex_search(raw, matcher, rgx);

  if (!isValid) {
    body = "";
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

ParseUnique ParseUnique::fromString(const std::string &raw) {
  return ParseUnique(raw);
}

bool ParseUnique::isViolatedBy(const ParseUnique &target) {
  return isValid && target.isValid && body == target.body;
}

bool ParseUnique::isAllowed(const std::vector<ParseUnique> &memes) {
  return std::all_of(memes.begin(), memes.end(), [this](const ParseUnique &meme) {
    return !isViolatedBy(meme);
  });
}
