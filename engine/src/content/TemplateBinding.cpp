// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <nlohmann/json.hpp>

#include "xjmusic/content/TemplateBinding.h"
#include "xjmusic/util/CsvUtils.h"

using json = nlohmann::json;

using namespace XJ;


// Map and reverse-map of TemplateBinding::EType enum values to their string representations
static const std::map<TemplateBinding::Type, std::string> typeValueNames = {
    {TemplateBinding::Library,    "Library"},
    {TemplateBinding::Program,    "Program"},
    {TemplateBinding::Instrument, "Instrument"},
};
static const std::map<std::string, TemplateBinding::Type> typeNameValues = EntityUtils::reverseMap(typeValueNames);


std::string TemplateBinding::toString() const {
  return toString(type) + "[" + targetId + "]";
}


TemplateBinding::Type TemplateBinding::parseType(const std::string &value) {
  if (typeNameValues.count(value) == 0) {
    return Library;
  }
  return typeNameValues.at(value);
}


std::string TemplateBinding::toString(const Type &type) {
  return typeValueNames.at(type);
}


std::string TemplateBinding::toPrettyCsv(const std::set<const TemplateBinding *> &templateBindings) {
  std::vector<std::string> parts;
  parts.reserve(templateBindings.size());
  for (const auto &templateBinding: templateBindings) {
    parts.emplace_back(templateBinding->toString());
  }
  return CsvUtils::join(parts);
}

