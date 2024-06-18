// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/util/StringUtils.h"
#include "xjmusic/music/Octave.h"

using namespace XJ;


/**
 * Regular expression to in the octave of a note
 */
static std::regex octaveRgx("(-*[0-9]+)$");


int octaveOf(const std::string &text) {
  std::smatch matcher;
  std::string prepared = std::regex_replace(StringUtils::stripExtraSpaces(text), std::regex("--"), "-");
  if (std::regex_search(prepared, matcher, octaveRgx)) {
    return std::stoi(matcher[1].str());
  } else {
    return 0;
  }
}
