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

    UUID id;
    int segmentId;
    float position;
    std::string name;
    UUID programId;
    UUID programSequenceId;
    UUID programSequenceBindingId;
    UUID programVoiceId;
    UUID instrumentId;
    int deltaIn;
    int deltaOut;
    bool mute;
    Instrument::Type instrumentType;
    Instrument::Mode instrumentMode;
    Program::Type programType;

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
