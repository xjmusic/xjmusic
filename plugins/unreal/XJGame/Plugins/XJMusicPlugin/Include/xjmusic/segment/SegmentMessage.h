// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef SEGMENT_MESSAGE_H
#define SEGMENT_MESSAGE_H

#include <string>
#include <utility>

#include "xjmusic/util/EntityUtils.h"
#include "SegmentEntity.h"

namespace XJ {

  class SegmentMessage : public SegmentEntity {
  public:

    enum Type {
      Debug,
      Info,
      Warning,
      Error,
    };

    SegmentMessage() = default;

    SegmentMessage::Type type{};
    std::string body;

    /**
     * Parse the Segment Message Type enum value from a string
     * @param value  The string to parse
     * @return      The Segment Message Type enum value
     */
    static SegmentMessage::Type parseType(const std::string &value);

    /**
     * Convert an Segment Message Type enum value to a string
     * @param type  The Segment Message Type enum value
     * @return      The string representation of the Segment Message Type
     */
    static std::string toString(const SegmentMessage::Type &type);

    /**
     * Assert equality with another Segment Message
     * @param segmentMessage  The Segment Message to compare
     * @return       true if equal
     */
    [[nodiscard]] bool equals(const SegmentMessage &segmentMessage) const;

    /**
     * Determine a unique hash code for the Segment Message
     * @return       hash code
     */
    [[nodiscard]] unsigned long long hashCode() const;

  };

}// namespace XJ

#endif//SEGMENT_MESSAGE_H
