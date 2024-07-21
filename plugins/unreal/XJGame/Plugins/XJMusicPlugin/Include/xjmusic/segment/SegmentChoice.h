// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef SEGMENT_CHOICE_H
#define SEGMENT_CHOICE_H

#include <string>
#include <utility>

#include "xjmusic/util/EntityUtils.h"
#include "xjmusic/content/Instrument.h"
#include "xjmusic/content/Program.h"
#include "SegmentEntity.h"

namespace XJ {

  class SegmentChoice : public SegmentEntity {
  public:
    SegmentChoice() = default;

    static constexpr int DELTA_UNLIMITED = -1;

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

  };

}// namespace XJ

#endif//SEGMENT_CHOICE_H
