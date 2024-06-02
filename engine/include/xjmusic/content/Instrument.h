// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef INSTRUMENT_H
#define INSTRUMENT_H

#include <string>

#include "nlohmann/json.hpp"

#include "Entity.h"

using json = nlohmann::json;

namespace Content {

  class Instrument : public Entity {
  public:

    enum Type {
      Drum,
      Bass,
      Pad,
      Sticky,
      Stripe,
      Stab,
      Hook,
      Percussion,
      Transition,
      Background
    };

    enum Mode {
      Event,
      Chord,
      Loop
    };

    enum State {
      Draft,
      Published
    };

    Instrument() = default;

    UUID libraryId{};
    Instrument::State state{};
    Instrument::Type type{};
    Instrument::Mode mode{};
    std::string name{};
    std::string config{};
    float volume{0};
    bool isDeleted{false};
    long long updatedAt{0};

    /**
     * Parse the Instrument Type enum value from a string
     * @param value  The string to parse
     * @return      The Instrument Type enum value
     */
    static Instrument::Type parseType(const std::string &value);

    /**
     * Parse the Instrument Mode enum value from a string
     * @param value  The string to parse
     * @return      The Instrument Mode enum value
     */
    static Instrument::Mode parseMode(const std::string &value);

    /**
     * Parse the Instrument State enum value from a string
     * @param value  The string to parse
     * @return      The Instrument State enum value
     */
    static Instrument::State parseState(const std::string &value);

    /**
     * Convert an Instrument Type enum value to a string
     * @param type  The Instrument Type enum value
     * @return      The string representation of the Instrument Type
     */
    static std::string toString(const Instrument::Type &type);

    /**
     * Convert an Instrument Mode enum value to a string
     * @param mode  The Instrument Mode enum value
     * @return      The string representation of the Instrument Mode
     */
    static std::string toString(const Instrument::Mode &mode);

    /**
     * Convert an Instrument State enum value to a string
     * @param state  The Instrument State enum value
     * @return      The string representation of the Instrument State
     */
    static std::string toString(const Instrument::State &state);
  };

}// namespace Content

#endif//INSTRUMENT_H
