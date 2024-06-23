// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/content/ProgramMeme.h"

using namespace XJ;

std::set<std::string> ProgramMeme::getNames(const std::set<const ProgramMeme *> &programMemes) {
  std::set<std::string> names;
  for (const auto &programMeme: programMemes) {
    names.insert(programMeme->name);
  }
  return names;
}
