// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <nlohmann/json.hpp>

#include "xjmusic/content/Program.h"

using json = nlohmann::json;

using namespace XJ;


// Map and reverse-map of Program::Type enum values to their string representations
static const std::map<Program::Type, std::string> typeValueNames = {
    {Program::Macro,  "Macro"},
    {Program::Main,   "Main"},
    {Program::Beat,   "Beat"},
    {Program::Detail, "Detail"},
};
static const std::map<std::string, Program::Type> typeNameValues = EntityUtils::reverseMap(typeValueNames);


// Map and reverse-map of Program::State enum values to their string representations
static const std::map<Program::State, std::string> stateValueNames = {
    {Program::Draft,     "Draft"},
    {Program::Published, "Published"},
};
static const std::map<std::string, Program::State> stateNameValues = EntityUtils::reverseMap(stateValueNames);


Program::Type Program::parseType(const std::string &value) {
  if (typeNameValues.count(value) == 0) {
    return Main;
  }
  return typeNameValues.at(value);
}


Program::State Program::parseState(const std::string &value) {
  if (stateNameValues.count(value) == 0) {
    return Draft;
  }
  return stateNameValues.at(value);
}


std::string Program::toString(const Type &type) {
  return typeValueNames.at(type);
}


std::string Program::toString(const State &state) {
  return stateValueNames.at(state);
}
