// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/segment/Chain.h"

using namespace XJ;


// Map and reverse-map of Chain::Type enum values to their string representations
static const std::map<Chain::Type, std::string> typeValueNames = {
    {Chain::Preview,    "Preview"},
    {Chain::Production, "Production"},
};
static const std::map<std::string, Chain::Type> typeNameValues = EntityUtils::reverseMap(typeValueNames);


// Map and reverse-map of Chain::State enum values to their string representations
static const std::map<Chain::State, std::string> stateValueNames = {
    {Chain::Draft,     "Draft"},
    {Chain::Ready,     "Ready"},
    {Chain::Fabricate, "Fabricate"},
    {Chain::Failed,    "Failed"},
};
static const std::map<std::string, Chain::State> stateNameValues = EntityUtils::reverseMap(stateValueNames);


Chain::Type Chain::parseType(const std::string &value) {
  if (typeNameValues.count(value) == 0) {
    return Preview;
  }
  return typeNameValues.at(value);
}


Chain::State Chain::parseState(const std::string &value) {
  if (stateNameValues.count(value) == 0) {
    return Draft;
  }
  return stateNameValues.at(value);
}


std::string Chain::toString(const Type &type) {
  return typeValueNames.at(type);
}


std::string Chain::toString(const State &state) {
  return stateValueNames.at(state);
}


bool Chain::equals(const Chain &chain) const {
  return id == chain.id &&
         templateId == chain.templateId &&
         type == chain.type &&
         state == chain.state &&
         shipKey == chain.shipKey &&
         templateConfig == chain.templateConfig &&
         name == chain.name;
}


unsigned long long Chain::hashCode() const {
  return std::hash<std::string>{}(id) ^
         std::hash<std::string>{}(templateId) ^
         std::hash<int>{}(type) ^
         std::hash<int>{}(state) ^
         std::hash<std::string>{}(shipKey) ^
         std::hash<std::string>{}(templateConfig) ^
         std::hash<std::string>{}(name);
}

