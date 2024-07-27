// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/segment/SegmentMessage.h"

using namespace XJ;


// Map and reverse-map of SegmentMessage::EType enum values to their string representations
static const std::map<SegmentMessage::Type, std::string> typeValueNames = {
    {SegmentMessage::Debug,   "Debug"},
    {SegmentMessage::Info,    "Info"},
    {SegmentMessage::Warning, "Warning"},
    {SegmentMessage::Error,   "Error"},
};
static const std::map<std::string, SegmentMessage::Type> typeNameValues = EntityUtils::reverseMap(typeValueNames);


SegmentMessage::Type SegmentMessage::parseType(const std::string &value) {
  if (typeNameValues.count(value) == 0) {
    return Debug;
  }
  return typeNameValues.at(value);
}


std::string SegmentMessage::toString(const Type &type) {
  return typeValueNames.at(type);
}


bool SegmentMessage::equals(const SegmentMessage &segmentMessage) const {
  return id == segmentMessage.id &&
         segmentId == segmentMessage.segmentId &&
         type == segmentMessage.type &&
         body == segmentMessage.body;
}


unsigned long long SegmentMessage::hashCode() const {
  return std::hash<std::string>{}(id) ^
         std::hash<int>{}(segmentId) ^
         std::hash<std::string>{}(body) ^
         std::hash<int>{}(type);
}
