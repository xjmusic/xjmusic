// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef PROGRAM_H
#define PROGRAM_H

#include <string>

#include "Entity.h"

namespace Content {

  class Program : public Entity {
  public:
    enum Type {
      Macro,
      Main,
      Beat,
      Detail
    };

    enum State {
      Draft,
      Published
    };

    Program() = default;
    UUID libraryId;
    Program::State state{};
    Program::Type type{};
    std::string key;
    float tempo{};
    std::string name;
    std::string config;
    bool isDeleted{};
    long long updatedAt{};

    /**
     * Parse the Program Type enum value from a string
     * @param value  The string to parse
     * @return      The Program Type enum value
     */
    static Program::Type parseType(const std::string &value);

    /**
     * Parse the Program State enum value from a string
     * @param value  The string to parse
     * @return      The Program State enum value
     */
    static Program::State parseState(const std::string &value);

    /**
     * Convert a Program Type enum value to a string
     * @param type  The Program Type enum value
     * @return      The string representation of the Program Type
     */
    static std::string toString(const Program::Type &type);

    /**
     * Convert a Program State enum value to a string
     * @param state  The Program State enum value
     * @return      The string representation of the Program State
     */
    static std::string toString(const Program::State &state);
  };

}// namespace Content

#endif//PROGRAM_H
