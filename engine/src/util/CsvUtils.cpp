// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include "xjmusic/util/CsvUtils.h"

using namespace XJ;


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


std::string CsvUtils::join(const std::vector<std::string> &parts) {
  return StringUtils::join(parts, ", ");
}


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
