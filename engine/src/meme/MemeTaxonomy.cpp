// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <sstream>
#include <regex>
#include <set>

#include <spdlog/spdlog.h>

#include "xjmusic/meme/MemeTaxonomy.h"

using namespace XJ;


std::string MemeCategory::MEME_SEPARATOR = ",";
std::string MemeCategory::KEY_NAME = "name";
std::string MemeCategory::KEY_MEMES = "memes";
std::string MemeCategory::DEFAULT_CATEGORY_NAME = "CATEGORY";
std::regex MemeCategory::rgx("^([a-zA-Z ]+)\\[([a-zA-Z, ]+)\\]$");


static std::string sanitize(const std::string &raw) {
  return StringUtils::toUpperCase(StringUtils::toAlphabetical(StringUtils::trim(raw)));
}


std::set<std::string>
MemeCategory::parseMemeList(const MapStringToOneOrManyString &data) {
  const auto it = data.find(KEY_MEMES);
  if (it != data.end() && std::holds_alternative<std::set<std::string>>(it->second)) {
    const std::set<std::string> rawList = std::get<std::set<std::string>>(it->second);
    std::set<std::string> sanitizedList;
    for (const auto &raw: rawList) {
      sanitizedList.insert(sanitize(raw));
    }
    return sanitizedList;
  }
  return {};
}


MemeCategory::MemeCategory(const std::string *raw) {
  if (StringUtils::isNullOrEmpty(raw)) {
    name = DEFAULT_CATEGORY_NAME;
    memes = std::set<std::string>();
    return;
  }

  std::smatch matcher;
  std::regex_search(*raw, matcher, rgx);

  if (matcher.empty()) {
    name = DEFAULT_CATEGORY_NAME;
    memes = std::set<std::string>();
    return;
  }

  std::string pfx = matcher[1].str();
  if (pfx.empty()) {
    name = DEFAULT_CATEGORY_NAME;
    memes = std::set<std::string>();
    return;
  }
  std::string alphabetical = StringUtils::toAlphabetical(pfx);
  name = sanitize(alphabetical);

  std::string body = matcher[2].str();
  if (body.empty())
    memes = std::set<std::string>();
  else {
    memes.clear();
    std::istringstream iss(body);
    for (std::string s; std::getline(iss, s, ',');)
      memes.insert(sanitize(s));
  }
}


MemeCategory::MemeCategory(const MapStringToOneOrManyString &data) {
  const auto it = data.find(KEY_NAME);
  name = (it != data.end() && std::holds_alternative<std::string>(it->second))
         ? sanitize(std::get<std::string>(it->second))
         : DEFAULT_CATEGORY_NAME;

  memes = parseMemeList(data);
}


std::string MemeCategory::getName() const {
  return name;
}


std::set<std::string> MemeCategory::getMemes() {
  return memes;
}


bool MemeCategory::isAllowed(std::set<std::string> &targets) const {
  int count = 0;
  for (const auto &meme: memes) {
    if (targets.find(meme) != targets.end()) {
      count++;
    }
  }
  return count <= 1;
}


bool MemeCategory::hasMemes() const {
  return !memes.empty();
}


std::string MemeCategory::toString() const {
  std::vector<std::string> sortedMemes(memes.begin(), memes.end());
  std::sort(sortedMemes.begin(), sortedMemes.end());
  return name + "[" + StringUtils::join(sortedMemes, MEME_SEPARATOR) + "]";
}


MapStringToOneOrManyString MemeCategory::toMap() const {
  return MapStringToOneOrManyString{
      {KEY_NAME,  name},
      {KEY_MEMES, memes}};
}


char MemeTaxonomy::CATEGORY_SEPARATOR = ';';


MemeTaxonomy::MemeTaxonomy(const std::string &raw) : MemeTaxonomy() {
  if (StringUtils::isNullOrEmpty(&raw)) {
    return;
  }
  std::istringstream iss(raw);
  for (std::string s; std::getline(iss, s, CATEGORY_SEPARATOR);) {
    categories.insert(MemeCategory(&s));
  }
}


MemeTaxonomy::MemeTaxonomy(const std::set<MapStringToOneOrManyString> &data) {
  categories.clear();
  for (auto &d: data) {
    try {
      categories.insert(MemeCategory(d));
    } catch (...) {
      spdlog::error("Failed to add map data!");
    }
  }
}


std::string MemeTaxonomy::toString() const {
  std::vector<std::string> strings;
  strings.reserve(categories.size());
  for (const auto &category: categories) {
    strings.push_back(category.toString());
  }
  std::sort(strings.begin(), strings.end());
  return StringUtils::join(strings, std::string(1, CATEGORY_SEPARATOR));
}


std::set<MapStringToOneOrManyString> MemeTaxonomy::toList() const {
  std::set<MapStringToOneOrManyString> data;
  for (const auto &category: categories) {
    auto map = category.toMap();
    data.insert(map);
  }
  return data;
}


std::set<MemeCategory> MemeTaxonomy::getCategories() {
  return categories;
}


bool MemeTaxonomy::isAllowed(std::set<std::string> memes) const {
  for (const auto &memeCategory: categories) {
    if (!memeCategory.isAllowed(memes)) {
      return false;
    }
  }
  return true;
}


MemeTaxonomy MemeTaxonomy::empty() {
  return {};
}


MemeTaxonomy MemeTaxonomy::fromSet(std::set<MapStringToOneOrManyString> &data) {
  return MemeTaxonomy(data);
}


MemeTaxonomy MemeTaxonomy::fromString(const std::string &raw) {
  return MemeTaxonomy(raw);
}


MemeTaxonomy MemeTaxonomy::fromList(
    const std::vector<std::map<std::string, std::variant<std::string, std::vector<std::string>>>> &list) {
  std::set<MapStringToOneOrManyString> set;
  for (const auto &map: list) {
    MapStringToOneOrManyString m;
    for (const auto &[key, value]: map) {
      if (std::holds_alternative<std::vector<std::string>>(value)) {
        auto v = std::get<std::vector<std::string>>(value);
        m[key] = std::set<std::string>(v.begin(), v.end());
      } else {
        m[key] = std::get<std::string>(value);
      }
    }
    set.insert(m);
  }
  return MemeTaxonomy(set);
}

