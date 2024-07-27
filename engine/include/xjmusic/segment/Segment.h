// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef SEGMENT_H
#define SEGMENT_H

#include <optional>
#include <string>

#include "xjmusic/util/EntityUtils.h"

namespace XJ {

  class Segment {
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
    Type type{};
    State state{};
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
    long long createdAt{EntityUtils::currentTimeMillis()};
    long long updatedAt{EntityUtils::currentTimeMillis()};

    /**
     * Parse the Segment Type enum value from a string
     * @param value  The string to parse
     * @return      The Segment Type enum value
     */
    static Type parseType(const std::string &value);

    /**
     * Parse the Segment State enum value from a string
     * @param value  The string to parse
     * @return      The Segment State enum value
     */
    static State parseState(const std::string &value);

    /**
     * Convert an Segment Type enum value to a string
     * @param type  The Segment Type enum value
     * @return      The string representation of the Segment Type
     */
    static std::string toString(const Type &type);

    /**
     * Convert an Segment State enum value to a string
     * @param state  The Segment State enum value
     * @return      The string representation of the Segment State
     */
    static std::string toString(const State &state);

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
