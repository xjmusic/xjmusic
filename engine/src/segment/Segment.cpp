// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/segment/Segment.h"

using namespace XJ;


// Map and reverse-map of Segment::EType enum values to their string representations
static const std::map<Segment::Type, std::string> typeValueNames = {
    {Segment::Pending,   "Pending"},
    {Segment::Initial,   "Initial"},
    {Segment::Continue,  "Continue"},
    {Segment::NextMain,  "NextMain"},
    {Segment::NextMacro, "NextMacro"},
};
static const std::map<std::string, Segment::Type> typeNameValues = EntityUtils::reverseMap(typeValueNames);


// Map and reverse-map of Segment::State enum values to their string representations
static const std::map<Segment::State, std::string> stateValueNames = {
    {Segment::Planned,  "Planned"},
    {Segment::Crafting, "Crafting"},
    {Segment::Crafted,  "Crafted"},
    {Segment::Failed,   "Failed"},
};
static const std::map<std::string, Segment::State> stateNameValues = EntityUtils::reverseMap(stateValueNames);


Segment::Type Segment::parseType(const std::string &value) {
  if (typeNameValues.count(value) == 0) {
    return Pending;
  }
  return typeNameValues.at(value);
}


Segment::State Segment::parseState(const std::string &value) {
  if (stateNameValues.count(value) == 0) {
    return Planned;
  }
  return stateNameValues.at(value);
}


std::string Segment::toString(const Type &type) {
  return typeValueNames.at(type);
}


std::string Segment::toString(const State &state) {
  return stateValueNames.at(state);
}


bool Segment::equals(const Segment &segment) const {
  return id == segment.id &&
         chainId == segment.chainId &&
         type == segment.type &&
         state == segment.state &&
         beginAtChainMicros == segment.beginAtChainMicros &&
         durationMicros == segment.durationMicros &&
         key == segment.key &&
         total == segment.total &&
         intensity == segment.intensity &&
         tempo == segment.tempo &&
         storageKey == segment.storageKey &&
         waveformPreroll == segment.waveformPreroll &&
         waveformPostroll == segment.waveformPostroll &&
         delta == segment.delta &&
         createdAt == segment.createdAt &&
         updatedAt == segment.updatedAt;
}


unsigned long long Segment::hashCode() const {
  return std::hash<int>{}(id) ^
         std::hash<UUID>{}(chainId) ^
         std::hash<int>{}(type) ^
         std::hash<int>{}(state) ^
         std::hash<long>{}(beginAtChainMicros) ^
         (durationMicros.has_value() ? std::hash<long>{}(durationMicros.value()) : 0) ^
         std::hash<std::string>{}(key) ^
         std::hash<int>{}(total) ^
         std::hash<float>{}(intensity) ^
         std::hash<float>{}(tempo) ^
         std::hash<std::string>{}(storageKey) ^
         std::hash<float>{}(waveformPreroll) ^
         std::hash<float>{}(waveformPostroll) ^
         std::hash<int>{}(delta) ^
         std::hash<long long>{}(createdAt) ^
         std::hash<long long>{}(updatedAt);
}

