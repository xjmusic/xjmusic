// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_PROGRAM_H
#define XJMUSIC_PROGRAM_H

#include <string>

#include "ContentEntity.h"
#include "ProgramConfig.h"
#include "xjmusic/util/EntityUtils.h"

namespace XJ {

  class Program : public ContentEntity {
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
    State state{};
    Type type{};
    std::string key;
    float tempo{};
    std::string name;
    ProgramConfig config;
    bool isDeleted{};
    long long updatedAt{EntityUtils::currentTimeMillis()};

    /**
     * Parse the Program Type enum value from a string
     * @param value  The string to parse
     * @return      The Program Type enum value
     */
    static Type parseType(const std::string &value);

    /**
     * Parse the Program State enum value from a string
     * @param value  The string to parse
     * @return      The Program State enum value
     */
    static State parseState(const std::string &value);

    /**
     * Convert a Program Type enum value to a string
     * @param type  The Program Type enum value
     * @return      The string representation of the Program Type
     */
    static std::string toString(const Type &type);

    /**
     * Convert a Program State enum value to a string
     * @param state  The Program State enum value
     * @return      The string representation of the Program State
     */
    static std::string toString(const State &state);
  };

  /**
   * Parse a Program from a JSON object
   * @param json  input
   * @param entity  output
   */
  inline void from_json(const json &json, Program &entity) {
    EntityUtils::setRequired(json, "id", entity.id);
    EntityUtils::setRequired(json, "libraryId", entity.libraryId);
    entity.state = Program::parseState(json.at("state").get<std::string>());
    entity.type = Program::parseType(json.at("type").get<std::string>());
    EntityUtils::setIfNotNull(json, "key", entity.key);
    EntityUtils::setIfNotNull(json, "tempo", entity.tempo);
    EntityUtils::setIfNotNull(json, "name", entity.name);
    EntityUtils::setIfNotNull(json, "isDeleted", entity.isDeleted);
    EntityUtils::setIfNotNull(json, "updatedAt", entity.updatedAt);

    if (json.contains("config") && json.at("config").is_string()) {
      const auto configStr = json.at("config").get<std::string>();
      entity.config = ProgramConfig(configStr);
    }
  }

}// namespace XJ

#endif//XJMUSIC_PROGRAM_H
