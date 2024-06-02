// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <sstream>
#include <regex>
#include <unordered_set>

#include "spdlog/spdlog.h"

#include "xjmusic/content/MemeTaxonomy.h"

namespace Content {

  std::string MemeCategory::MEME_SEPARATOR = ",";
  std::string MemeCategory::KEY_NAME = "name";
  std::string MemeCategory::KEY_MEMES = "memes";
  std::string MemeCategory::DEFAULT_CATEGORY_NAME = "CATEGORY";
  std::regex MemeCategory::rgx("^([a-zA-Z ]+)\\[([a-zA-Z, ]+)\\]$");

  static std::string sanitize(std::string &raw) {
    return Util::StringUtils::toUpperCase(Util::StringUtils::toAlphabetical(Util::StringUtils::trim(raw)));
  }

  std::vector<std::string>
  MemeCategory::parseMemeList(std::map<std::string, std::variant<std::string, std::vector<std::string>>> &data) {
    auto it = data.find(KEY_MEMES);
    if (it != data.end() && std::holds_alternative<std::vector<std::string>>(it->second)) {
      std::vector<std::string> rawList = std::get<std::vector<std::string>>(it->second);
      std::vector<std::string> sanitizedList;
      sanitizedList.reserve(rawList.size());
      for (auto &raw: rawList) {
        sanitizedList.push_back(sanitize(raw));
      }
      return sanitizedList;
    }
    return {};
  }

  MemeCategory::MemeCategory(const std::string *raw) {
    if (Util::StringUtils::isNullOrEmpty(raw)) {
      name = DEFAULT_CATEGORY_NAME;
      memes = std::vector<std::string>();
      return;
    }

    std::smatch matcher;
    std::regex_search(*raw, matcher, rgx);

    if (matcher.empty()) {
      name = DEFAULT_CATEGORY_NAME;
      memes = std::vector<std::string>();
      return;
    }

    std::string pfx = matcher[1].str();
    if (pfx.empty()) {
      name = DEFAULT_CATEGORY_NAME;
      memes = std::vector<std::string>();
      return;
    }
    std::string alphabetical = Util::StringUtils::toAlphabetical(pfx);
    name = sanitize(alphabetical);

    std::string body = matcher[2].str();
    if (body.empty())
      memes = std::vector<std::string>();
    else {
      memes.clear();
      std::istringstream iss(body);
      for (std::string s; std::getline(iss, s, ',');)
        memes.push_back(sanitize(s));
    }
  }

  MemeCategory::MemeCategory(
      std::map<std::string, std::variant<std::string, std::vector<std::string>>> &data) {
    auto it = data.find(KEY_NAME);
    name = (it != data.end() && std::holds_alternative<std::string>(it->second)) ? sanitize(
        std::get<std::string>(it->second))
                                                                                 : DEFAULT_CATEGORY_NAME;

    memes = parseMemeList(data);
  }

  std::string MemeCategory::getName() {
    return name;
  }

  std::vector<std::string> MemeCategory::getMemes() {
    return memes;
  }

  bool MemeCategory::isAllowed(std::vector<std::string> &targets) const {
    std::unordered_set<std::string> targetSet(targets.begin(), targets.end());
    int count = 0;
    for (const auto &meme: memes) {
      if (targetSet.find(meme) != targetSet.end()) {
        count++;
      }
    }
    return count <= 1;
  }

  bool MemeCategory::hasMemes() {
    return !memes.empty();
  }

  std::string MemeCategory::toString() const {
    return name + "[" + Util::StringUtils::join(memes, MEME_SEPARATOR) + "]";
  }

  std::map<std::string, std::variant<std::string, std::vector<std::string>>> MemeCategory::toMap() const {
    return std::map<std::string, std::variant<std::string, std::vector<std::string>>>{
        {KEY_NAME,  name},
        {KEY_MEMES, memes}};
  }

  char MemeTaxonomy::CATEGORY_SEPARATOR = ';';

  MemeTaxonomy::MemeTaxonomy(const std::string &raw) : MemeTaxonomy() {
    if (Util::StringUtils::isNullOrEmpty(&raw)) {
      categories = std::vector<MemeCategory>();
      return;
    }

    categories.clear();
    std::istringstream iss(raw);
    for (std::string s; std::getline(iss, s, CATEGORY_SEPARATOR);) {
      categories.emplace_back(&s);
    }
  }

  MemeTaxonomy::MemeTaxonomy(
      std::vector<std::map<std::string, std::variant<std::string, std::vector<std::string>>>> &data) {
    categories.clear();
    categories.reserve(data.size());
    for (auto &d: data) {
      try {
        categories.emplace_back(d);
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
    return Util::StringUtils::join(strings, std::string(1, CATEGORY_SEPARATOR));
  }

  std::vector<std::map<std::string, std::variant<std::string, std::vector<std::string>>>> MemeTaxonomy::toList() {
    std::vector<std::map<std::string, std::variant<std::string, std::vector<std::string>>>> data;
    data.reserve(categories.size());
    for (const auto &category: categories) {
      auto map = category.toMap();
      data.emplace_back(map);
    }
    return data;
  }

  std::vector<MemeCategory> MemeTaxonomy::getCategories() {
    return categories;
  }

  bool MemeTaxonomy::isAllowed(std::vector<std::string> memes) {
    for (const auto &memeCategory: categories) {
      if (!memeCategory.isAllowed(memes)) {
        return false;
      }
    }
    return true;
  }

}// namespace Content