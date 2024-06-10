// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include <algorithm>
#include <set>

#include "xjmusic/entities/meme/MemeStack.h"

using namespace XJ;

/**
 Constructor from taxonomy and memes

 @param from from which to create stack
 */
MemeStack::MemeStack(MemeTaxonomy taxonomy, std::vector<std::string> from) {
  this->taxonomy = taxonomy;
  for (const auto& str : from) {
    memes.insert(StringUtils::toMeme(str));
  }
}

