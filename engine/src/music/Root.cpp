// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/music/Root.h"

using namespace XJ;


std::regex Root::rgxNote("^([ABCDEFG]).*");
std::regex Root::rgxNoteModified("^([ABCDEFG][#b]).*");


Root::Root(const std::string &name) {
  {
    const std::string normalized = accidentalNormalized(name);

    // as a default, the whole thing is remaining text, and pitch class is None
    pitchClass = PitchClass::Atonal;
    remainingText = normalized;

    evaluate(rgxNote, normalized);
    evaluate(rgxNoteModified, normalized);
  }
}


Root Root::of(const std::string &name) {
  return Root(name);
}


void Root::evaluate(const std::regex &pattern, const std::string &text) {
  std::smatch matcher;
  if (!std::regex_search(text, matcher, pattern))
    return;

  const std::string match = matcher[1];
  if (match.empty())
    return;

  this->pitchClass = pitchClassOf(match);
  this->remainingText = text.substr(match.length());
  this->remainingText.erase(this->remainingText.begin(),
                            std::find_if(this->remainingText.begin(), this->remainingText.end(), [](const unsigned char ch) {
                              return !std::isspace(ch);
                            }));
}
