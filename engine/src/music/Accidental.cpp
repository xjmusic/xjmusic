// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <locale>
#include <regex>

#include "xjmusic/music/Accidental.h"
#include "xjmusic/util/StringUtils.h"

using namespace XJ;


static std::regex accidentalSharpishIn("(M|maj|Major|major|aug)");
static std::regex accidentalFlattishIn("([^a-z]|^)(m|min|Minor|minor|dim)");


Accidental XJ::accidentalOf(const std::string &name) {
  const int numSharps = StringUtils::countMatches('#', name);
  const int numFlats = StringUtils::countMatches('b', name);
  const int numSharpish = StringUtils::countMatches(accidentalSharpishIn, name);
  const int numFlattish = StringUtils::countMatches(accidentalFlattishIn, name);

  // sharp/flat has precedent over sharpish/flattish; overall default is sharp
  return numFlats > numSharps || numFlats == numSharps && numFlattish > numSharpish ? Flat : Sharp;
}


Accidental XJ::accidentalOfBeginning(const std::string &name) {
  if (name[0] == '#')
    return Sharp;

  if (name[0] == 'b')
    return Flat;

  return Natural;
}
