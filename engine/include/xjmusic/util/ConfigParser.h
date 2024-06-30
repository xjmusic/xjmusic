// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_CONFIG_PARSER_H
#define XJMUSIC_CONFIG_PARSER_H

#include <regex>
#include <map>
#include <set>
#include <string>
#include <utility>
#include <variant>
#include <vector>

namespace XJ {

  /**
 * Base class for the different types of values in a HOCON configuration
 */
  class ConfigValue {
  public:
    virtual ~ConfigValue() = default;
  };

  /**
 * A single value in a HOCON configuration
 */
  class ConfigSingleValue final : public ConfigValue {
  private:
    std::variant<std::string, int, float, bool> value;

  public:
    ConfigSingleValue() = default;

    /**
   * Construct a ConfigSingleValue from a string, int, float, or bool
   * @param value  from which to construct the ConfigSingleValue
   */
    explicit ConfigSingleValue(const std::variant<std::string, int, float, bool> &value);

    /**
     * Get a string value from a ConfigSingleValue
     * @return           The string value
     */
    [[nodiscard]] std::string getString() const;

    /**
     * Get a float value from a ConfigSingleValue
     * @return           The float value
     */
    [[nodiscard]] float getFloat() const;

    /**
     * Get an integer value from a ConfigSingleValue
     * @return           The integer value
     */
    [[nodiscard]] int getInt() const;

    /**
     * Get a bool value from a ConfigSingleValue
     * @return           The bool value
     */
    bool getBool() const;
  };

  /**
 * An object value in a HOCON configuration
 */
  class ConfigObjectValue final : public ConfigValue {
  private:
    std::map<std::string, std::variant<ConfigSingleValue, std::vector<ConfigSingleValue>>> data;

  public:
    /**
   * Get the size of the object
   * @return  The size of the object
   */
    unsigned long size();

    /**
   * Get a single value from the object
   * @param index  at which to get the value
   * @return       single value
   */
    ConfigSingleValue atSingle(const std::string &index);

    /**
   * Get a list of single values from the object
   * @param index  at which to get the value
   * @return       list of single values
   */
    std::vector<ConfigSingleValue> atList(const std::string &index);

    /**
   * Set a value at a key in the object
   * @param key    at which to set the value
   * @param value  The value to set
   */
    void set(const std::string &key, const std::variant<ConfigSingleValue, std::vector<ConfigSingleValue>> &value);

    /**
   * Get the object as a map of strings to strings
   * @return  The object as a map of strings to strings
   */
    std::map<std::string, std::variant<ConfigSingleValue, std::vector<ConfigSingleValue>>> asMapOfSingleOrList();

    /**
   * Get the object as a map of strings to strings
   * @return  The object as a map of strings to strings
   */
    std::map<std::string, std::variant<std::string, std::vector<std::string>>> asMapOfStringsOrListsOfStrings();
  };

  /**
 * A list value in a HOCON configuration
 */
  class ConfigListValue final : public ConfigValue {
  private:
    std::vector<std::variant<ConfigSingleValue, ConfigObjectValue>> data;

  public:
    /**
   * Get the size of the list
   * @return  The size of the list
   */
    unsigned long size();

    /**
   * Get the list as a vector of strings
   * @return  The list as a vector of strings
   */
    [[nodiscard]] std::vector<std::string> asListOfStrings() const;

    /**
   * Get the list as a vector of strings
   * @return  The list as a vector of strings
   */
    [[nodiscard]] std::set<std::string> asSetOfStrings() const;

    /*
   * Get the list as a vector of maps of strings to strings
   * @return  The list as a vector of maps of strings to strings
   */
    std::vector<std::map<std::string, std::variant<std::string, std::vector<std::string>>>> asListOfMapsOfStrings();

    /**
   * Get a single value from the list
   * @param index  at which to get the value
   * @return       single value
   */
    ConfigSingleValue atSingle(const unsigned long &index);

    /**
   * Get an object value from the list
   * @param index  at which to get the value
   * @return       object value
   */
    ConfigObjectValue atObject(const unsigned long &index);

    /**
   * Add a value to the list
   * @param value  The value to add
   */
    void add(const std::variant<ConfigSingleValue, ConfigObjectValue> &value);
  };

  /**
 * Configuration exception
 */
  class ConfigException final : public std::exception {
  private:
    std::string message;

  public:
    explicit ConfigException(std::string msg) : message(std::move(msg)) {}

    [[nodiscard]] const char *what() const noexcept override {
      return message.c_str();
    }
  };

  /**
 * A lightweight HOCON parser and formatter
 */
  class ConfigParser {
   std::map<std::string, std::variant<ConfigSingleValue, ConfigObjectValue, ConfigListValue>> config;

  public:
    /**
     * Parse a HOCON string into a ConfigMap
     * @param input  The HOCON string to parse
     * @return       The parsed ConfigMap
     */
    explicit ConfigParser(const std::string &input);

    /**
     * Parse a HOCON string into a ConfigMap
     * @param input     The HOCON string to parse
     * @param defaults  The default values to use
     * @return          The parsed ConfigMap
     */
    ConfigParser(const std::string &input, const ConfigParser &defaults);

    /**
     * Get a single value from a ConfigMap
     * @param key        The key of the value to get
     * @return           The single value
     */
    ConfigSingleValue getSingleValue(const std::string &key);

    /**
     * Get a list value from a ConfigMap
     * @param key        The key of the value to get
     * @return           The list value
     */
    ConfigListValue getListValue(const std::string &key);

    /**
     * Get a object value from a ConfigMap
     * @param key        The key of the value to get
     * @return           The object value
     */
    ConfigObjectValue getObjectValue(const std::string &key);

    /**
     * Format a bool value as a string
     * @param value  The bool value
     * @return       The string representation of the bool value
     */
    static std::string format(const bool &value);

    /**
     * Format an integer value as a string
     * @param value  The integer value
     * @return       The string representation of the integer value
     */
    static std::string format(const int &value);

    /**
     * Format a float value as a string
     * @param value  The float value
     * @return       The string representation of the float value
     */
    static std::string format(const float &value);

    /**
     * Format a string value as a (quoted) string
     * @param value  The string value
     * @return       The string representation of the quoted string value
     */
    static std::string format(const std::string &value);

    /**
     * Format a list of strings as a comma-separated list of quoted strings in square brackets
     * @param values
     * @return
     */
    static std::string format(const std::vector<std::string> &values);

    /**
     * Format a list of strings as a comma-separated list of quoted strings in square brackets
     * @param values
     * @return
     */
    static std::string format(const std::set<std::string> &values);
  };

}// namespace XJ

#endif//XJMUSIC_CONFIG_PARSER_H
