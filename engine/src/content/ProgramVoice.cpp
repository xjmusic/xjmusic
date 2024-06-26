// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <set>
#include "xjmusic/content/ProgramVoice.h"

std::set<std::string> XJ::ProgramVoice::getNames(const std::set<const ProgramVoice *>& voices) {
  std::set<std::string> names;
  for (const ProgramVoice *voice : voices) {
    names.insert(voice->name);
  }
  return names;
}
