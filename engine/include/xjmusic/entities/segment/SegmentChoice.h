// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef SEGMENT_CHOICE_H
#define SEGMENT_CHOICE_H

#include <string>
#include <utility>

#include "xjmusic/entities/Entity.h"
#include "xjmusic/entities/content/Instrument.h"
#include "xjmusic/entities/content/Program.h"

namespace XJ {

  class SegmentChoice : public Entity {
  public:
    SegmentChoice() = default;

    static constexpr int DELTA_UNLIMITED = -1;

    UUID id;
    int segmentId{};
    float position{};
    std::string name;
    UUID programId;
    UUID programSequenceId;
    UUID programSequenceBindingId;
    UUID programVoiceId;
    UUID instrumentId;
    int deltaIn{DELTA_UNLIMITED};
    int deltaOut{DELTA_UNLIMITED};
    bool mute{};
    Instrument::Type instrumentType{};
    Instrument::Mode instrumentMode{};
    Program::Type programType{};

    /**
     * Assert equality with another Segment Choice
     * @param segmentChoice  The Segment Choice to compare
     * @return       true if equal
     */
    [[nodiscard]] bool equals(const SegmentChoice &segmentChoice) const;

    /**
     * Determine a unique hash code for the Segment Choice
     * @return       hash code
     */
    [[nodiscard]] unsigned long long hashCode() const;

    /**
     * Compare two Segment Choices
     * @param lhs segment choice
     * @param rhs segment choice
     * @return true if lhs < rhs
     */
    friend bool operator<(const SegmentChoice &lhs, const SegmentChoice &rhs) {
      return lhs.id < rhs.id;
    }

  };

}// namespace XJ

#endif//SEGMENT_CHOICE_H
