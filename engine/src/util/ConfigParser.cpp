// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <map>
#include <sstream>
#include <string>
#include <variant>
#include <vector>
#include <cmath>

#include "xjmusic/util/ConfigParser.h"
#include "xjmusic/util/StringUtils.h"

namespace XJ {

  /**
   * Parse a float value
   * @param s      The string to parse
   * @param value  The float value
   * @return       True if the string was successfully parsed, false otherwise
   */
  bool parseFloat(const std::string &s, float &value) {
    try {
      value = std::stof(s);
      return true;
    } catch (std::exception &) {
      return false;
    }
  }

  /**
   * Parse a string value (removing quotes)
   * @param s  The string to parse
   * @return   The parsed string
   */
  std::string parseString(const std::string &s) {
    if (s.front() == '"' && s.back() == '"')
      return s.substr(1, s.size() - 2);
    return s;
  }

  /**
   * Parse a single value, either an int, float, bool, or string
   * @param value  The value string to parse
   * @return       The parsed value
   */
  ConfigSingleValue parseSingleValue(const std::string &value) {
    float floatValue;
    if (parseFloat(value, floatValue)) {
      // if the original string contains a "." then it's a float, otherwise it's an in
      if (value.find('.') != std::string::npos) {
        return ConfigSingleValue(floatValue);
      } else {
        return ConfigSingleValue(static_cast<int>(std::round(floatValue)));
      }
    } else if (value == "true" || value == "True" || value == "TRUE") {
      return ConfigSingleValue(true);
    } else if (value == "false" || value == "False" || value == "FALSE") {
      return ConfigSingleValue(false);
    } else {
      return ConfigSingleValue(parseString(value));
    }
  }

  /**
   * Parse an array value
   * @param s  The string to parse
   * @return   The parsed array
   */
  std::vector<ConfigSingleValue> parseSimpleListValue(const std::string &s) {
    std::vector<ConfigSingleValue> list;
    std::istringstream iss(s);
    std::string value;
    while (getline(iss, value, ',')) {
      value = StringUtils::trim(value);
      list.emplace_back(parseSingleValue(value));
    }
    return list;
  }

  /**
   * Parse a map value
   * @param input  The string to parse
   * @return       The parsed map
   */
  ConfigObjectValue parseObjectValue(const std::string &input) {
    ConfigObjectValue obj;

    // Read one piece at a time from the input string, and add it to the members vector.
    // If the next piece does not have an "=" sign, then it is a piece of the previous piece
    std::istringstream iss(StringUtils::trim(input));
    std::vector<std::string> members;
    std::string piece;
    std::string key;
    int inObject = 0;// depth inside an object
    int inList = 0;  // depth inside a list
    while (std::getline(iss, piece)) {
      piece = StringUtils::trim(piece);
      if (0 == inObject && 0 == inList && piece.find('=') != std::string::npos) {
        members.push_back(piece);
      } else if (!members.empty()) {
        members.back() += "\n" + piece;
      }

      if (piece.find('{') != std::string::npos) inObject++;
      if (piece.find('}') != std::string::npos) inObject--;
      if (piece.find('[') != std::string::npos) inList++;
      if (piece.find(']') != std::string::npos) inList--;
    }

    // Parse each piece of the members vector
    std::string value;
    for (const auto &member: members) {
      auto pos = member.find('=');
      if (pos != std::string::npos) {
        key = StringUtils::trim(member.substr(0, pos));
        value = StringUtils::trim(member.substr(pos + 1));
        if (value.front() == '[' && value.back() == ']') {// Array
          obj.set(key, parseSimpleListValue(value.substr(1, value.size() - 2)));
        } else {
          obj.set(key, parseSingleValue(value));
        }
      }
    }
    return obj;
  }

  /**
   * Parse an array value
   * @param s  The string to parse
   * @return   The parsed array
   */
  ConfigListValue parseListValue(const std::string &s) {
    ConfigListValue list;
    std::istringstream iss(StringUtils::trim(s));

    std::vector<std::string> members;
    std::string piece;
    int inObject = 0;// depth inside an object
    int inList = 0;  // depth inside a list
    while (std::getline(iss, piece, ',')) {
      piece = StringUtils::trim(piece);
      if (0 == inObject && 0 == inList) {
        members.push_back(piece);
      } else if (!members.empty()) {
        members.back() += "," + piece;
      }

      if (piece.find('{') != std::string::npos) inObject++;
      if (piece.find('}') != std::string::npos) inObject--;
      if (piece.find('[') != std::string::npos) inList++;
      if (piece.find(']') != std::string::npos) inList--;
    }

    for (const auto &member: members) {
      if (member.front() == '{' && member.back() == '}') {// Map
        list.add(parseObjectValue(member.substr(1, member.size() - 2)));
      } else {
        list.add(parseSingleValue(member));
      }
    }

    return list;
  }

  /**
   * Parse a member of the config map from the value string
   * @param value  The value string to parse
   * @return       The parsed member
   */
  std::variant<ConfigSingleValue, ConfigObjectValue, ConfigListValue>
  parseValue(const std::basic_string<char, std::char_traits<char>, std::allocator<char>> &value) {
    if (value.front() == '[' && value.back() == ']')
      return parseListValue(value.substr(1, value.size() - 2));

    if (value.front() == '{' && value.back() == '}')
      return parseObjectValue(value.substr(1, value.size() - 2));

    return parseSingleValue(value);
  }

  ConfigParser::ConfigParser(const std::string &input) {
    // Read one piece at a time from the input string, and add it to the members vector.
    // If the next piece does not have an "=" sign, then it is a piece of the previous piece
    std::istringstream iss(StringUtils::trim(input));
    std::vector<std::string> members;
    std::string piece;
    std::string key;
    int inObject = 0;// depth inside an object
    int inList = 0;  // depth inside a list
    while (std::getline(iss, piece)) {
      piece = StringUtils::trim(piece);
      if (0 == inObject && 0 == inList && piece.find('=') != std::string::npos) {
        members.push_back(piece);
      } else if (!members.empty()) {
        members.back() += "\n" + piece;
      }

      if (piece.find('{') != std::string::npos) inObject++;
      if (piece.find('}') != std::string::npos) inObject--;
      if (piece.find('[') != std::string::npos) inList++;
      if (piece.find(']') != std::string::npos) inList--;
    }

    // Parse each piece of the members vector
    std::string value;
    for (const auto &member: members) {
      auto pos = member.find('=');
      if (pos != std::string::npos) {
        key = StringUtils::trim(member.substr(0, pos));
        value = StringUtils::trim(member.substr(pos + 1));
        config[key] = parseValue(value);
      }
    }
  }

  ConfigParser::ConfigParser(const std::string &input, const ConfigParser &defaults) : ConfigParser(input) {
    for (const auto &pair: defaults.config) {
      if (config.find(pair.first) == config.end()) {
        config[pair.first] = pair.second;
      }
    }
  }

  std::string ConfigSingleValue::getString() const {
    if (std::holds_alternative<std::string>(value)) {
      return std::get<std::string>(value);
    }
    throw ConfigException("Value is not a string");
  }

  float ConfigSingleValue::getFloat() const {
    if (std::holds_alternative<float>(value)) {
      return std::get<float>(value);
    }
    throw ConfigException("Value is not a float");
  }

  int ConfigSingleValue::getInt() const {
    if (std::holds_alternative<int>(value)) {
      return std::get<int>(value);
    }
    throw ConfigException("Value is not an integer");
  }

  bool ConfigSingleValue::getBool() {
    if (std::holds_alternative<bool>(value)) {
      return std::get<bool>(value);
    }
    throw ConfigException("Value is not a bool");
  }

  ConfigSingleValue::ConfigSingleValue(const std::variant<std::string, int, float, bool> &value) : value(value) {}

  ConfigSingleValue ConfigParser::getSingleValue(const std::string &key) {
    if (config.find(key) == config.end()) {
      throw ConfigException("Key not found");
    }
    if (!std::holds_alternative<ConfigSingleValue>(config.at(key))) {
      throw ConfigException("Value is not a single value");
    }
    return std::get<ConfigSingleValue>(config.at(key));
  }

  ConfigListValue ConfigParser::getListValue(const std::string &key) {
    if (config.find(key) == config.end()) {
      throw ConfigException("Key not found");
    }
    if (!std::holds_alternative<ConfigListValue>(config.at(key))) {
      throw ConfigException("Value is not a list value");
    }
    return std::get<ConfigListValue>(config.at(key));
  }

  ConfigObjectValue ConfigParser::getObjectValue(const std::string &key) {
    if (config.find(key) == config.end()) {
      throw ConfigException("Key not found");
    }
    if (!std::holds_alternative<ConfigObjectValue>(config.at(key))) {
      throw ConfigException("Value is not an object value");
    }
    return std::get<ConfigObjectValue>(config.at(key));
  }

  std::string ConfigParser::format(const bool &value) {
    return value ? "true" : "false";
  }

  std::string ConfigParser::format(const int &value) {
    return std::to_string(value);
  }

  std::string ConfigParser::format(const float &value) {
    return StringUtils::formatFloat(value);
  }

  std::string ConfigParser::format(const std::string &value) {
    return "\"" + value + "\"";
  }

  std::string ConfigParser::format(const std::vector<std::string> &values) {
    std::vector<std::string> quotedValues;
    quotedValues.reserve(values.size());
    for (const auto &value: values) {
      quotedValues.push_back("\"" + value + "\"");
    }
    return "[" + StringUtils::join(quotedValues, ",") + "]";
  }

  unsigned long ConfigObjectValue::size() {
    return data.size();
  }

  ConfigSingleValue ConfigObjectValue::atSingle(const std::string &index) {
    return std::get<ConfigSingleValue>(data.at(index));
  }

  std::vector<ConfigSingleValue> ConfigObjectValue::atList(const std::string &index) {
    return std::get<std::vector<ConfigSingleValue>>(data.at(index));
  }

  void ConfigObjectValue::set(const std::string &key,
                              const std::variant<ConfigSingleValue, std::vector<ConfigSingleValue>> &value) {
    data[key] = value;
  }

  std::map<std::string, std::variant<ConfigSingleValue, std::vector<ConfigSingleValue>>>
  ConfigObjectValue::asMapOfSingleOrList() {
    return data;
  }

  std::map<std::string, std::variant<std::string, std::vector<std::string>>>
  ConfigObjectValue::asMapOfStringsOrListsOfStrings() {
    std::map<std::string, std::variant<std::string, std::vector<std::string>>> map;
    for (const auto &pair: data) {
      if (std::holds_alternative<ConfigSingleValue>(pair.second)) {
        map[pair.first] = std::get<ConfigSingleValue>(pair.second).getString();
      } else if (std::holds_alternative<std::vector<ConfigSingleValue>>(pair.second)) {
        std::vector<std::string> values;
        for (const auto &value: std::get<std::vector<ConfigSingleValue>>(pair.second)) {
          values.push_back(value.getString());
        }
        map[pair.first] = values;
      }
    }
    return map;
  }

  unsigned long ConfigListValue::size() {
    return data.size();
  }

  ConfigSingleValue ConfigListValue::atSingle(const unsigned long &index) {
    return std::get<ConfigSingleValue>(data.at(index));
  }

  ConfigObjectValue ConfigListValue::atObject(const unsigned long &index) {
    return std::get<ConfigObjectValue>(data.at(index));
  }

  void ConfigListValue::add(const std::variant<ConfigSingleValue, ConfigObjectValue> &value) {
    data.emplace_back(value);
  }

  std::vector<std::string> ConfigListValue::asListOfStrings() const {
    std::vector<std::string> values;
    for (const auto &value: data) {
      if (std::holds_alternative<ConfigSingleValue>(value)) {
        ConfigSingleValue single = std::get<ConfigSingleValue>(value);
        values.push_back(single.getString());
      }
    }
    return values;
  }

  std::vector<std::map<std::string, std::variant<std::string, std::vector<std::string>>>>
  ConfigListValue::asListOfMapsOfStrings() {
    std::vector<std::map<std::string, std::variant<std::string, std::vector<std::string>>>> maps;
    for (const auto &value: data) {
      if (std::holds_alternative<ConfigObjectValue>(value)) {
        ConfigObjectValue object = std::get<ConfigObjectValue>(value);
        maps.emplace_back(object.asMapOfStringsOrListsOfStrings());
      }
    }
    return maps;
  }

}// namespace XJ