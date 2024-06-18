// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef SEGMENT_H
#define SEGMENT_H

#include <string>
#include <optional>
#include <variant>

#include "nlohmann/json.hpp"

#include "xjmusic/util/EntityUtils.h"
#include "SegmentChoice.h"
#include "SegmentChoiceArrangement.h"
#include "SegmentChoiceArrangementPick.h"
#include "SegmentChord.h"
#include "SegmentChordVoicing.h"
#include "SegmentMeme.h"
#include "SegmentMessage.h"
#include "SegmentMeta.h"

using json = nlohmann::json;

namespace XJ {

  class Segment : public EntityUtils {
  public:

    enum Type {
      Pending,
      Initial,
      Continue,
      NextMain,
      NextMacro,
    };

    enum State {
      Planned,
      Crafting,
      Crafted,
      Failed,
    };

    Segment() = default;

    int id{};
    UUID chainId{};
    Segment::Type type{};
    Segment::State state{};
    long beginAtChainMicros{}; // Segment begin-at time in microseconds since beginning of chain
    std::optional<long> durationMicros{}; // @Nullable
    std::string key{};
    int total{};
    float intensity{};
    float tempo{};
    std::string storageKey{};
    float waveformPreroll{};
    float waveformPostroll{};
    int delta{};
    long long createdAt{currentTimeMillis()};
    long long updatedAt{currentTimeMillis()};

    /**
     * Parse the Segment Type enum value from a string
     * @param value  The string to parse
     * @return      The Segment Type enum value
     */
    static Segment::Type parseType(const std::string &value);

    /**
     * Parse the Segment State enum value from a string
     * @param value  The string to parse
     * @return      The Segment State enum value
     */
    static Segment::State parseState(const std::string &value);

    /**
     * Convert an Segment Type enum value to a string
     * @param type  The Segment Type enum value
     * @return      The string representation of the Segment Type
     */
    static std::string toString(const Segment::Type &type);

    /**
     * Convert an Segment State enum value to a string
     * @param state  The Segment State enum value
     * @return      The string representation of the Segment State
     */
    static std::string toString(const Segment::State &state);

    /**
     * Assert equality with another Segment
     * @param segment  The Segment to compare
     * @return       true if equal
     */
    [[nodiscard]] bool equals(const Segment &segment) const;

    /**
     * Determine a unique hash code for the Segment
     * @return       hash code
     */
    [[nodiscard]] unsigned long long hashCode() const;

    /**
     * Compare two Segments
     * @param lhs segment
     * @param rhs segment
     * @return true if lhs < rhs
     */
    friend bool operator<(const Segment &lhs, const Segment &rhs) {
      return lhs.id < rhs.id;
    }

  };

}// namespace XJ

#endif//SEGMENT_H
