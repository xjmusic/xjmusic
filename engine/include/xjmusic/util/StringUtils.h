// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_STRING_UTILS_H
#define XJMUSIC_STRING_UTILS_H

#include <optional>
#include <regex>
#include <set>
#include <string>
#include <vector>

namespace XJ {
  class StringUtils {
  private:
    static std::regex leadingScores;
    static std::regex nonAlphabetical;
    static std::regex nonAlphanumeric;
    static std::regex nonEvent;
    static std::regex nonMeme;
    static std::regex nonScored;
    static std::regex nonSlug;
    static std::regex spaces;
    static std::regex tailingScores;
    static std::regex underscores;

  public:
    /**
     * Split a string by a delimiter
     * @param input      The string to split
     * @param delimiter  The delimiter
     * @return           The split string
     */
    static std::vector<std::string> split(const std::string &input, const char delimiter);

    /**
     * Join a vector of strings with a delimiter
     * @param input      The vector of strings
     * @param delimiter  The delimiter
     * @return           The joined string
     */
    static std::string join(const std::vector<std::string> &input, const std::string &delimiter);

    /**
     * Function to trim whitespace from the start and end of a string
     * @param str  The string to trim
     * @return     The trimmed string
     */
    static std::string trim(const std::string &str);

    /**
     * Conform to Upper-slug including some special characters, e.g. "BUN!"
     * @param   raw input
     * @return  purified
     */
    static std::string toMeme(const std::string &raw);

    /**
     * Conform to Upper-slug (e.g. "BUN"), else default value
     * @param raw           input
     * @param defaultValue  if no input
     * @return              purified
     */
    static std::string toMeme(const std::string *raw, const std::string &defaultValue);

    /**
     Conform to Upper-slug non-numeric and strip special characters, e.g. "BUN"

     @param raw input
     @return purified
     */
    static std::string toEvent(const std::string &raw);

    /**
     * Check if a string is null or empty
     * @param raw  The string to check
     * @return     True if the string is null or empty
     */
    static bool isNullOrEmpty(const std::string *raw);

    /**
     * Remove all non-alphabetical characters from a string
     * @param raw  The string to clean
     * @return     The cleaned string
     */
    static std::string toAlphabetical(const std::string &raw);

    /**
     * Remove all non-alphanumeric characters from a string
     * @param raw  The string to clean
     * @return     The cleaned string
     */
    static std::string toAlphanumeric(const std::string &raw);

    /**
     * Convert a string to uppercase
     * @param input  The string to convert
     * @return     The uppercase string
     */
    static std::string toUpperCase(const std::string &input);

    /**
     * Convert a string to lowercase
     * @param input  The string to convert
     * @return             The lowercase string
     */
    static std::string toLowerCase(const std::string &input);

    /**
     * Format a float as a string with the minimum required number of digits after the floating point (minimum of 1 digit)
     * @param value  The float to format
     * @return       The formatted string
     */
    static std::string formatFloat(float value);

    /**
     * Replace more than one space with one space, and strip leading and trailing spaces
     * @param value to strip
     * @return stripped value
     */
    static std::string stripExtraSpaces(const std::string &value);

    /**
     * First group matching pattern in text, else null
     *
     * @param pattern to use
     * @param text    to search
     * @return match if found
     */
    static std::optional<std::string> match(const std::regex &pattern, const std::string &text);

    /**
     * Count the matches of a regex in a string
     * @param regex        to use
     * @param basicString  to search
     * @return number of matches
     */
    static int countMatches(const std::regex &regex, const std::string &basicString);

    /**
     * Count the matches of a character in a string
     * @param regex        to use
     * @param basicString  to search
     * @return number of matches
     */
    static int countMatches(const char regex, const std::string &basicString);

    /**
     * Convert a string to a ship key
     * @param name  The name to convert
     * @return    The ship key
     */
    static std::string toShipKey(const std::string &name);

    /**
     * Conform to Lower-scored (e.g. "buns_and_jams")
     * @param raw input
     * @return purified
     */
    static std::string toLowerScored(const std::string &raw);

    /**
     * Conform to Upper-scored (e.g. "BUNS_AND_JAMS")
     * @param raw input
     * @return purified
     */
    static std::string toUpperScored(const std::string &raw);

    /**
     * Conform to toScored (e.g. "mush_bun")
     * @param raw input
     * @return purified
     */
    static std::string toScored(const std::string &raw);

    /**
     Conform to Proper (e.g. "Jam")

     @param raw input
     @return purified
     */
    static std::string toProper(std::string raw);

    /**
     Conform to Proper-slug (e.g. "Jam")

     @param raw input
     @return purified
     */
    static std::string toProperSlug(const std::string &raw);

    /**
     Conform to Slug (e.g. "jim")

     @param raw input
     @return purified
     */
    static std::string toSlug(std::string raw);

    /**
     Conform to Lowercase slug (e.g. "mush")

     @param raw input
     @return purified
     */
    static std::string toLowerSlug(const std::string &raw);

    /**
     Conform to Uppercase slug (e.g. "MUSH")

     @param raw input
     @return purified
     */
    static std::string toUpperSlug(const std::string &raw);

    /**
    * Sort a set of strings into an ordered vector
    * @param items  set of strings
    * @return     vector of strings
    */
    static std::vector<std::string> sort(const std::set<std::string> &items);

    /**
     Pad the value with zeros to the given number of digits

     @param value  to pad
     @param digits total after padding
     @return padded value
     */
    static std::string zeroPadded(unsigned long long value, int digits) {
      std::string result = std::to_string(value);
      while (result.length() < digits) {
        result.insert(result.begin(), '0');
      }
      return result;
    }

  };


}// namespace XJ

#endif//XJMUSIC_STRING_UTILS_H
