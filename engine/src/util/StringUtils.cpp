// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <algorithm>
#include <sstream>

#include "xjnexus/util/StringUtils.h"

namespace Util {

  std::regex StringUtils::nonMeme("[^a-zA-Z0-9!$]");
  std::regex StringUtils::nonAlphabetical("[^a-zA-Z]");
  std::regex StringUtils::nonAlphanumeric("[^a-zA-Z0-9.\\-]");// include decimal and sign

  std::vector<std::string> StringUtils::split(const std::string &input, char delimiter) {
    std::vector<std::string> tokens;
    std::string token;
    std::istringstream tokenStream(trim(input));
    while (std::getline(tokenStream, token, delimiter)) {
      tokens.push_back(trim(token));
    }
    return tokens;
  }

  std::string StringUtils::join(const std::vector<std::string> &input, const std::string &delimiter) {
    std::stringstream ss;
    for (size_t i = 0; i < input.size(); ++i) {
      if (i != 0)
        ss << delimiter;
      ss << input[i];
    }
    return ss.str();
  }

  std::string StringUtils::trim(const std::string &str) {
    size_t first = str.find_first_not_of(" \n");
    if (first == std::string::npos)
      return "";
    size_t last = str.find_last_not_of(" \n");
    return str.substr(first, (last - first + 1));
  }

  std::string StringUtils::toMeme(const std::string &raw) {
    std::string result = std::regex_replace(raw, nonMeme, "");
    std::transform(result.begin(), result.end(), result.begin(), ::toupper);// to uppercase
    return result;
  }

  std::string StringUtils::toMeme(const std::string *raw, const std::string &defaultValue) {
    if (isNullOrEmpty(raw))
      return StringUtils::toMeme(defaultValue);

    std::string out = StringUtils::toMeme(*raw);
    if (out.empty())
      return StringUtils::toMeme(defaultValue);

    return out;
  }

  bool StringUtils::isNullOrEmpty(const std::string *raw) {
    return raw == nullptr || raw->empty();
  }

  std::string StringUtils::toAlphabetical(const std::string &raw) {
    std::string result = std::regex_replace(raw, nonAlphabetical, "");
    return result;
  }

  std::string StringUtils::toAlphanumeric(const std::string &raw) {
    std::string result = std::regex_replace(raw, nonAlphanumeric, "");
    return result;
  }

  std::string StringUtils::toUpperCase(const std::string &input) {
    std::string result = input;
    std::transform(result.begin(), result.end(), result.begin(), ::toupper);// to uppercase
    return result;
  }

  std::string StringUtils::toLowerCase(const std::string &input) {
    std::string result = input;
    std::transform(result.begin(), result.end(), result.begin(), ::tolower);// to uppercase
    return result;
  }

  std::string StringUtils::formatFloat(float value) {
    std::string str = std::to_string(value);

    // Remove trailing zeros
    str.erase(str.find_last_not_of('0') + 1, std::string::npos);

    // If the last character is a decimal point, add a zero
    if (str.back() == '.') {
      str.push_back('0');
    }

    return str;
  }

  std::string StringUtils::stripExtraSpaces(const std::string &value) {
    std::string result = value;
    result.erase(std::unique(result.begin(), result.end(), [](char a, char b) { return a == ' ' && b == ' '; }),
                 result.end());
    return trim(result);
  }

  std::optional<std::string> StringUtils::match(const std::regex& pattern, const std::string& text) {
    std::smatch matcher;
    std::regex_search(text, matcher, pattern);

    if (matcher.empty())
      return std::nullopt;

    std::string match = matcher[1].str();
    if (match.empty())
      return std::nullopt;

    return match;
  }

  int StringUtils::countMatches(const std::regex& regex, const std::string &basicString) {
    std::sregex_iterator it(basicString.begin(), basicString.end(), regex);
    std::sregex_iterator itEnd;
    return static_cast<int>(std::distance(it, itEnd));
  }

  int StringUtils::countMatches(const char match, const std::string &basicString) {
    return std::count(basicString.begin(), basicString.end(), match);
  }

}// namespace Util