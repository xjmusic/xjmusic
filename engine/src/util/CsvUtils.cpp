// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include "xjmusic/util/CsvUtils.h"

using namespace XJ;

/**
 * Split a CSV string into a vector of strings
 * @param csv  CSV string
 * @return     vector of strings
 */
std::vector<std::string> CsvUtils::split(const std::string &csv) {
  if (csv.empty()) return {};
  std::vector<std::string> result;
  std::stringstream ss(csv);
  std::string item;
  while (std::getline(ss, item, ',')) {
    item.erase(item.begin(), std::find_if(item.begin(), item.end(), [](unsigned char ch) {
      return !std::isspace(ch);
    }));
    item.erase(std::find_if(item.rbegin(), item.rend(), [](unsigned char ch) {
      return !std::isspace(ch);
    }).base(), item.end());
    result.push_back(item);
  }
  return result;
}

/**
 * Split a CSV string into a vector of proper slugs
 * @param csv  CSV string
 * @return     vector of proper slugs
 */
std::vector<std::string> CsvUtils::splitProperSlug(const std::string &csv) {
  std::vector<std::string> items = split(csv);
  std::vector<std::string> slugs;
  for (const auto &item: items) {
    if (!item.empty()) {
      slugs.push_back(StringUtils::toProperSlug(item));
    }
  }
  return slugs;
}

/**
 * Join a set of items' toPrettyCsv() values properly, e.g. "One, Two, Three, and Four"
 * @param parts 
 * @return 
 */
std::string CsvUtils::join(const std::vector<std::string> &parts) {
  return StringUtils::join(parts, ", ");
}

/**
 Join a set of items properly, e.g. "One, Two, Three, and Four"

 @param ids             to write
 @param beforeFinalItem text after last comma
 @return CSV of ids
 */
std::string CsvUtils::prettyFrom(const std::vector<std::string> &ids, const std::string &beforeFinalItem) {
  if (ids.empty()) {
    return "";
  }
  std::ostringstream result;
  for (size_t i = 0; i < ids.size(); ++i) {
    if (i != 0) {
      result << ", ";
    }
    if (i == ids.size() - 1) {
      result << beforeFinalItem << ' ';
    }
    result << ids[i];
  }
  return result.str();
}

/**
 Get a CSV string of key=value properties

 @param properties key=value
 @return CSV string
 */
std::string CsvUtils::from(const std::map<std::string, std::string> &properties) {
  int i = 0;
  std::string result;
  for (const auto &[key, value]: properties) {
    if (i != 0) {
      result += ", ";
    }
    result += key;
    result += "=";
    result += value;
    i++;
  }
  return result;
}
