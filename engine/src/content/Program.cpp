// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "nlohmann/json.hpp"

#include "xjnexus/content/Program.h"

using json = nlohmann::json;

namespace Content {

  // Map and reverse-map of Program::Type enum values to their string representations
  static const std::map<Program::Type, std::string> typeValueNames = {
      {Program::Macro,  "Macro"},
      {Program::Main,   "Main"},
      {Program::Beat,   "Beat"},
      {Program::Detail, "Detail"},
  };
  static const std::map<std::string, Program::Type> typeNameValues = reverseMap(typeValueNames);

  // Map and reverse-map of Program::State enum values to their string representations
  static const std::map<Program::State, std::string> stateValueNames = {
      {Program::Draft,     "Draft"},
      {Program::Published, "Published"},
  };
  static const std::map<std::string, Program::State> stateNameValues = reverseMap(stateValueNames);

  Program::Type Program::parseType(const std::string &value) {
    if (typeNameValues.count(value) == 0) {
      return Program::Type::Main;
    }
    return typeNameValues.at(value);
  }

  Program::State Program::parseState(const std::string &value) {
    if (stateNameValues.count(value) == 0) {
      return Program::State::Draft;
    }
    return stateNameValues.at(value);
  }

  std::string Program::toString(const Program::Type &type) {
    return typeValueNames.at(type);
  }

  std::string Program::toString(const Program::State &state) {
    return stateValueNames.at(state);
  }

}// namespace Content