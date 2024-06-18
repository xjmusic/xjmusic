// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <codecvt>
#include <locale>
#include <regex>

#include "xjmusic/music/Accidental.h"
#include "xjmusic/util/StringUtils.h"

using namespace XJ;


static std::regex accidentalSharpishIn("(M|maj|Major|major|aug)");
static std::regex accidentalFlattishIn("([^a-z]|^)(m|min|Minor|minor|dim)");

Accidental accidentalOf(const std::string &name) {
  std::string normalized = accidentalNormalized(name);
  int numSharps = StringUtils::countMatches('#', normalized);
  int numFlats = StringUtils::countMatches('b', normalized);
  int numSharpish = StringUtils::countMatches(accidentalSharpishIn, normalized);
  int numFlattish = StringUtils::countMatches(accidentalFlattishIn, normalized);

  // sharp/flat has precedent over sharpish/flattish; overall default is sharp
  return (numFlats > numSharps || numFlats == numSharps && numFlattish > numSharpish) ? Flat : Sharp;
}


Accidental accidentalOfBeginning(const std::string &name) {
  std::string normalized = accidentalNormalized(name);
  if (normalized[0] == '#')
    return Accidental::Sharp;
  else if (normalized[0] == 'b')
    return Accidental::Flat;
  else
    return Accidental::Natural;
}


std::string accidentalNormalized(const std::string &name) {
  std::wstring_convert<std::codecvt_utf8<wchar_t>, wchar_t> converter;
  std::wstring wide = converter.from_bytes(name);
  std::replace(wide.begin(), wide.end(), L'♯', L'#');
  std::replace(wide.begin(), wide.end(), L'♭', L'b');
  return converter.to_bytes(wide);
}

