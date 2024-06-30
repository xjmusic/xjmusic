// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/content/Instrument.h"

using namespace XJ;


// Map and reverse-map of Instrument::Type enum values to their string representations
static const std::map<Instrument::Type, std::string> typeValueNames = {
    {Instrument::Drum,       "Drum"},
    {Instrument::Bass,       "Bass"},
    {Instrument::Pad,        "Pad"},
    {Instrument::Sticky,     "Sticky"},
    {Instrument::Stripe,     "Stripe"},
    {Instrument::Stab,       "Stab"},
    {Instrument::Hook,       "Hook"},
    {Instrument::Percussion, "Percussion"},
    {Instrument::Transition, "Transition"},
    {Instrument::Background, "Background"},
};
static const std::map<std::string, Instrument::Type> typeNameValues = EntityUtils::reverseMap(typeValueNames);


// Map and reverse-map of Instrument::Mode enum values to their string representations
static const std::map<Instrument::Mode, std::string> modeValueNames = {
    {Instrument::Event, "Event"},
    {Instrument::Chord, "Chord"},
    {Instrument::Loop,  "Loop"},
};
static const std::map<std::string, Instrument::Mode> modeNameValues = EntityUtils::reverseMap(modeValueNames);


// Map and reverse-map of Instrument::State enum values to their string representations
static const std::map<Instrument::State, std::string> stateValueNames = {
    {Instrument::Draft,     "Draft"},
    {Instrument::Published, "Published"},
};
static const std::map<std::string, Instrument::State> stateNameValues = EntityUtils::reverseMap(stateValueNames);


Instrument::Type Instrument::parseType(const std::string &value) {
  if (typeNameValues.count(value) == 0) {
    return Drum;
  }
  return typeNameValues.at(value);
}


Instrument::Mode Instrument::parseMode(const std::string &value) {
  if (modeNameValues.count(value) == 0) {
    return Event;
  }
  return modeNameValues.at(value);
}


Instrument::State Instrument::parseState(const std::string &value) {
  if (stateNameValues.count(value) == 0) {
    return Draft;
  }
  return stateNameValues.at(value);
}


std::string Instrument::toString(const Type &type) {
  return typeValueNames.at(type);
}


std::string Instrument::toString(const Mode &mode) {
  return modeValueNames.at(mode);
}


std::string Instrument::toString(const State &state) {
  return stateValueNames.at(state);
}

const std::vector<std::string> &Instrument::toStrings(std::vector<Type> types) {
  static std::vector<std::string> typeStrings;
  typeStrings.clear();
  for (const auto &type : types) {
    typeStrings.push_back(toString(type));
  }
  return typeStrings;
}
