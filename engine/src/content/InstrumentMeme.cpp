// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/content/InstrumentMeme.h"

using namespace XJ;

std::set<std::string> InstrumentMeme::getNames(const std::set<const InstrumentMeme *> &instrumentMemes) {
  std::set<std::string> names;
  for (const auto &instrumentMeme: instrumentMemes) {
    names.insert(instrumentMeme->name);
  }
  return names;
}

