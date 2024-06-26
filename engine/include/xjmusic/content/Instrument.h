// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef INSTRUMENT_H
#define INSTRUMENT_H

#include <string>

#include "nlohmann/json.hpp"

#include "xjmusic/util/EntityUtils.h"
#include "ContentEntity.h"

using json = nlohmann::json;

namespace XJ {

  class Instrument : public ContentEntity {
  public:

    enum Type {
      Background,
      Bass,
      Drum,
      Hook,
      Pad,
      Percussion,
      Stab,
      Sticky,
      Stripe,
      Transition
    };

    enum Mode {
      Chord,
      Event,
      Loop
    };

    enum State {
      Draft,
      Published
    };

    Instrument() = default;

    UUID libraryId{};
    State state{};
    Type type{};
    Mode mode{};
    std::string name{};
    std::string config{};
    float volume{0};
    bool isDeleted{false};
    long long updatedAt{EntityUtils::currentTimeMillis()};

    /**
     * Parse the Instrument Type enum value from a string
     * @param value  The string to parse
     * @return      The Instrument Type enum value
     */
    static Type parseType(const std::string &value);

    /**
     * Parse the Instrument Mode enum value from a string
     * @param value  The string to parse
     * @return      The Instrument Mode enum value
     */
    static Mode parseMode(const std::string &value);

    /**
     * Parse the Instrument State enum value from a string
     * @param value  The string to parse
     * @return      The Instrument State enum value
     */
    static State parseState(const std::string &value);

    /**
     * Convert an Instrument Type enum value to a string
     * @param type  The Instrument Type enum value
     * @return      The string representation of the Instrument Type
     */
    static std::string toString(const Type &type);

    /**
     * Convert an Instrument Mode enum value to a string
     * @param mode  The Instrument Mode enum value
     * @return      The string representation of the Instrument Mode
     */
    static std::string toString(const Mode &mode);

    /**
     * Convert an Instrument State enum value to a string
     * @param state  The Instrument State enum value
     * @return      The string representation of the Instrument State
     */
    static std::string toString(const State &state);

    static const std::vector<std::string> &toStrings(std::vector<Type> types);
  };

}// namespace XJ

#endif//INSTRUMENT_H
