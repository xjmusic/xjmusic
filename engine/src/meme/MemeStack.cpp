// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include <algorithm>
#include <set>
#include <utility>
#include <vector>
#include <iterator>

#include "xjmusic/meme/MemeStack.h"

using namespace XJ;

/**
 Constructor from taxonomy and memes

 @param from from which to create stack
 */
MemeStack::MemeStack(MemeTaxonomy taxonomy, const std::set<std::string> &from) {
  this->taxonomy = std::move(taxonomy);
  for (const auto &str: from) {
    memes.insert(StringUtils::toMeme(str));
  }
}

MemeStack MemeStack::from(const MemeTaxonomy &taxonomy, const std::set<std::string> &memes) {
  return MemeStack(taxonomy, memes);
}

bool MemeStack::isAllowed(const std::set<std::string> &targets) const {
  return isAllowed(memes, targets);
}

bool MemeStack::isAllowed(const std::set<std::string> &sources, const std::set<std::string> &targets) const {
  // this axiom is applied from source to target
  for (const auto &source: sources) {
    std::vector<ParseAnti> antiTargets;
    std::transform(targets.begin(), targets.end(), std::back_inserter(antiTargets), ParseAnti::fromString);
    if (!ParseAnti::fromString(source).isAllowed(antiTargets)) return false;

    std::vector<ParseNumeric> numericTargets;
    std::transform(targets.begin(), targets.end(), std::back_inserter(numericTargets), ParseNumeric::fromString);
    if (!ParseNumeric::fromString(source).isAllowed(numericTargets)) return false;

    std::vector<ParseUnique> uniqueTargets;
    std::transform(targets.begin(), targets.end(), std::back_inserter(uniqueTargets), ParseUnique::fromString);
    if (!ParseUnique::fromString(source).isAllowed(uniqueTargets)) return false;
  }

  // this axiom is applied from target to source
  for (const auto &target: targets) {
    std::vector<ParseStrong> strongSources;
    std::transform(sources.begin(), sources.end(), std::back_inserter(strongSources), ParseStrong::fromString);
    if (!ParseStrong::fromString(target).isAllowed(strongSources)) return false;
  }

  // meme categories
  std::set<std::string> allMemes;
  allMemes.insert(sources.begin(), sources.end());
  allMemes.insert(targets.begin(), targets.end());
  return taxonomy.isAllowed(allMemes);
}

bool MemeStack::isValid() {
  const std::vector<std::string> targets(memes.begin(), memes.end());

  for (int a = 0; a < targets.size(); a++) {
    std::vector<std::string> subTargets = targets;
    subTargets.erase(subTargets.begin() + a);
    for (int b = 0; b < memes.size(); b++) {
      std::set<std::string> m1(subTargets.begin(), subTargets.end());
      std::set<std::string> m2 = {targets[a]};
      if (!isAllowed(m1, m2))
        return false;
    }
  }

  // meme categories https://github.com/xjmusic/xjmusic/issues/209
  const std::set<std::string> targetSet(memes.begin(), memes.end());
  return taxonomy.isAllowed(targetSet);
}

std::string MemeStack::getConstellation() const {
  return MemeConstellation::fromNames(memes);
}

