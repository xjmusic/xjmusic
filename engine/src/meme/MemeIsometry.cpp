// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <sstream>
#include <unordered_set>

#include "xjmusic/meme/MemeIsometry.h"

using namespace XJ;

MemeIsometry::MemeIsometry(MemeTaxonomy taxonomy, const std::set<std::string> &sourceMemes)
    : stack(std::move(taxonomy), sourceMemes) {
  for (const std::string &meme: sourceMemes) add(StringUtils::toMeme(meme));
}

MemeIsometry MemeIsometry::of(MemeTaxonomy taxonomy, const std::set<std::string> &sourceMemes) {
  return MemeIsometry(std::move(taxonomy), sourceMemes);
}

MemeIsometry MemeIsometry::of(const std::set<std::string> &sourceMemes) {
  return MemeIsometry(MemeTaxonomy::empty(), sourceMemes);
}

MemeIsometry MemeIsometry::none() {
  return MemeIsometry(MemeTaxonomy(), std::set<std::string>());
}

int MemeIsometry::score(const std::set<std::string> &targets) const {
  if (!isAllowed(targets)) return 0;

  int sum = 0;
  for (const auto &target: targets) {
    std::string targetMeme = StringUtils::toMeme(target);
    for (const auto &source: sources) {
      if (source == targetMeme) {
        sum += 1;
      }
    }
  }

  return sum;
}

void MemeIsometry::add(const std::string &meme) {
  sources.insert(StringUtils::toMeme(meme));
}

void MemeIsometry::add(const ProgramMeme &meme) {
  add(meme.name);
}

void MemeIsometry::add(const ProgramSequenceBindingMeme &meme) {
  add(meme.name);
}

void MemeIsometry::add(const InstrumentMeme &meme) {
  add(meme.name);
}

void MemeIsometry::add(const SegmentMeme &meme) {
  add(meme.name);
}

bool MemeIsometry::isAllowed(const std::set<std::string> &memes) const {
  return stack.isAllowed(memes);
}

std::set<std::string> MemeIsometry::getSources() {
  return sources;
}

std::string MemeIsometry::getConstellation() const {
  return MemeConstellation::fromNames(sources);
}


const std::string MemeIsometry::KEY_NAME = "name";

