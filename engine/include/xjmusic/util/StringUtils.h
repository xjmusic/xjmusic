// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJMUSIC_STRING_UTILS_H
#define XJMUSIC_STRING_UTILS_H

#include <optional>
#include <regex>
#include <string>
#include <vector>

namespace XJ {
  class StringUtils {
  private:
    static std::regex nonMeme;
    static std::regex nonAlphabetical;
    static std::regex nonAlphanumeric;

  public:
    /**
     * Split a string by a delimiter
     * @param input      The string to split
     * @param delimiter  The delimiter
     * @return           The split string
     */
    static std::vector<std::string> split(const std::string &input, char delimiter);

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
    static std::optional<std::string> match(const std::regex& pattern, const std::string& text);

    /**
     * Count the matches of a regex in a string
     * @param regex        to use
     * @param basicString  to search
     * @return number of matches
     */
    static int countMatches(const std::regex& regex, const std::string &basicString);

    /**
     * Count the matches of a character in a string
     * @param regex        to use
     * @param basicString  to search
     * @return number of matches
     */
    static int countMatches(const char match, const std::string &basicString);
  };


}// namespace XJ

#endif//XJMUSIC_STRING_UTILS_H
