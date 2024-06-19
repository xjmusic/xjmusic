// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/content/ProgramSequenceBindingMeme.h"

using namespace XJ;

std::set<std::string> ProgramSequenceBindingMeme::getNames(const std::set<ProgramSequenceBindingMeme> &programSequenceBindingMemes) {
  std::set<std::string> names;
  for (const auto &programSequenceBindingMeme: programSequenceBindingMemes) {
    names.insert(programSequenceBindingMeme.name);
  }
  return names;
}

