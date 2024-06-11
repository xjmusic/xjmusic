// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include "xjmusic/entities/meme/MemeConstellation.h"

using namespace XJ;

std::string MemeConstellation::fromNames(const std::set<std::string> &names) {
  std::unordered_map<std::string, bool> uniqueNames;
  for (const auto &meme: names) {
    uniqueNames[meme] = true;
  }
  std::set<std::string> pieces;
  for (const auto &pair: uniqueNames) {
    pieces.insert(pair.first);
  }
  return join(CONSTELLATION_DELIMITER, pieces);
}

std::vector<std::string> MemeConstellation::toNames(const std::string &constellation)  {
  return split(constellation, CONSTELLATION_DELIMITER);
}

std::string MemeConstellation::join(const std::string &delimiter, const std::set<std::string> &pieces) {
  std::ostringstream joined;
  std::copy(pieces.begin(), pieces.end(), std::ostream_iterator<std::string>(joined, delimiter.c_str()));
  std::string result = joined.str();
  return result.substr(0, result.size() - delimiter.size()); // remove the last delimiter
}

std::vector<std::string> MemeConstellation::split(const std::string &str, const std::string &delimiter)  {
  std::vector<std::string> tokens;
  size_t start = 0, end;
  while ((end = str.find(delimiter, start)) != std::string::npos) {
    tokens.push_back(str.substr(start, end - start));
    start = end + delimiter.size();
  }
  tokens.push_back(str.substr(start));
  return tokens;
}
