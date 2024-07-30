// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_INSTRUMENT_H
#define XJMUSIC_INSTRUMENT_H

#include <string>

#include "ContentEntity.h"
#include "InstrumentConfig.h"
#include "xjmusic/util/EntityUtils.h"

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
    InstrumentConfig config{};
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

    /**
     * Convert a set of Instrument Type enum values to a set of strings
     * @param types  The set of Instrument Type enum values
     * @return     The set of strings
     */
    static const std::vector<std::string> &toStrings(std::vector<Type> types);
  };
  /**
   * Parse a Instrument from a JSON object
   * @param json  input
   * @param entity  output
   */
  inline void from_json(const json &json, Instrument &entity) {
    EntityUtils::setRequired(json, "id", entity.id);
    EntityUtils::setRequired(json, "libraryId", entity.libraryId);
    entity.state = Instrument::parseState(json.at("state").get<std::string>());
    entity.type = Instrument::parseType(json.at("type").get<std::string>());
    entity.mode = Instrument::parseMode(json.at("mode").get<std::string>());
    EntityUtils::setIfNotNull(json, "name", entity.name);
    EntityUtils::setIfNotNull(json, "volume", entity.volume);
    EntityUtils::setIfNotNull(json, "isDeleted", entity.isDeleted);
    EntityUtils::setIfNotNull(json, "updatedAt", entity.updatedAt);

    if (json.contains("config") && json.at("config").is_string()) {
      const auto configStr = json.at("config").get<std::string>();
      entity.config = InstrumentConfig(configStr);
    }
  }


}// namespace XJ

#endif//XJMUSIC_INSTRUMENT_H
