// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include <set>
#include "xjmusic/entities/meme/ParseNumeric.h"

using namespace XJ;

ParseNumeric::ParseNumeric(const std::string& raw) {
  std::smatch matcher;
  valid = std::regex_search(raw, matcher, rgx);

  if (!valid) {
    prefix = 0;
    body = "";
    valid = false;
    return;
  }

  std::string pfx = matcher[1].str();
  if (pfx.empty()) {
    prefix = 0;
    body = "";
    valid = false;
    return;
  }
  prefix = std::stoi(pfx);

  body = matcher[2].str();
  if (body.empty()) {
    valid = false;
    return;
  }

  valid = true;
}

ParseNumeric ParseNumeric::fromString(const std::string& raw) {
  return ParseNumeric(raw);
}

bool ParseNumeric::isViolatedBy(const ParseNumeric &target) const {
  return valid && target.valid && body == target.body && prefix != target.prefix;
}

bool ParseNumeric::isAllowed(const std::vector<ParseNumeric> &memes) {
  return std::all_of(memes.begin(), memes.end(), [this](const ParseNumeric &meme) {
    return !isViolatedBy(meme);
  });
}

const std::regex ParseNumeric::rgx("^([0-9]+)(.+)$");
