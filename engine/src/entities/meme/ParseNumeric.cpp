// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include "xjmusic/entities/meme/ParseNumeric.h"

using namespace XJ;

ParseNumeric::ParseNumeric(const std::string& raw) {
  std::smatch matcher;
  isValid = std::regex_search(raw, matcher, rgx);

  if (!isValid) {
    prefix = 0;
    body = "";
    isValid = false;
    return;
  }

  std::string pfx = matcher[1].str();
  if (pfx.empty()) {
    prefix = 0;
    body = "";
    isValid = false;
    return;
  }
  prefix = std::stoi(pfx);

  body = matcher[2].str();
  if (body.empty()) {
    isValid = false;
    return;
  }

  isValid = true;
}

ParseNumeric ParseNumeric::fromString(const std::string& raw) {
  return ParseNumeric(raw);
}

bool ParseNumeric::isViolatedBy(const ParseNumeric &target) {
  return isValid && target.isValid && body == target.body && prefix != target.prefix;
}

bool ParseNumeric::isAllowed(const std::vector<ParseNumeric> &memes) {
  return std::all_of(memes.begin(), memes.end(), [this](const ParseNumeric &meme) {
    return !isViolatedBy(meme);
  });
}