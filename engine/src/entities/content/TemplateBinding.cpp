// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "nlohmann/json.hpp"

#include "xjmusic/entities/content/TemplateBinding.h"

using json = nlohmann::json;

namespace XJ {

  // Map and reverse-map of TemplateBinding::Type enum values to their string representations
  static const std::map<TemplateBinding::Type, std::string> typeValueNames = {
      {TemplateBinding::Library,    "Library"},
      {TemplateBinding::Program,    "Program"},
      {TemplateBinding::Instrument, "Instrument"},
  };
  static const std::map<std::string, TemplateBinding::Type> typeNameValues = Entity::reverseMap(typeValueNames);

  TemplateBinding::Type TemplateBinding::parseType(const std::string &value) {
    if (typeNameValues.count(value) == 0) {
      return TemplateBinding::Type::Library;
    }
    return typeNameValues.at(value);
  }

  std::string TemplateBinding::toString(const TemplateBinding::Type &type) {
    return typeValueNames.at(type);
  }

}// namespace XJ