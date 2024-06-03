// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef CHAIN_H
#define CHAIN_H

#include <string>

#include "nlohmann/json.hpp"

#include "Entity.h"

using json = nlohmann::json;

namespace XJ {

  class Chain : public Entity {
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

    Chain() = default;

    UUID libraryId{};
    Chain::State state{};
    Chain::Type type{};
    Chain::Mode mode{};
    std::string name{};
    std::string config{};
    float volume{0};
    bool isDeleted{false};
    long long updatedAt{0};

    /**
     * Parse the Chain Type enum value from a string
     * @param value  The string to parse
     * @return      The Chain Type enum value
     */
    static Chain::Type parseType(const std::string &value);

    /**
     * Parse the Chain Mode enum value from a string
     * @param value  The string to parse
     * @return      The Chain Mode enum value
     */
    static Chain::Mode parseMode(const std::string &value);

    /**
     * Parse the Chain State enum value from a string
     * @param value  The string to parse
     * @return      The Chain State enum value
     */
    static Chain::State parseState(const std::string &value);

    /**
     * Convert an Chain Type enum value to a string
     * @param type  The Chain Type enum value
     * @return      The string representation of the Chain Type
     */
    static std::string toString(const Chain::Type &type);

    /**
     * Convert an Chain Mode enum value to a string
     * @param mode  The Chain Mode enum value
     * @return      The string representation of the Chain Mode
     */
    static std::string toString(const Chain::Mode &mode);

    /**
     * Convert an Chain State enum value to a string
     * @param state  The Chain State enum value
     * @return      The string representation of the Chain State
     */
    static std::string toString(const Chain::State &state);
  };

}// namespace XJ

#endif//CHAIN_H
