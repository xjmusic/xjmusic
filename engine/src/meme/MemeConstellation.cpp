// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include "xjmusic/meme/MemeConstellation.h"

using namespace XJ;

std::string MemeConstellation::fromNames(const std::set<std::string> &names) {
  std::set<std::string> uniqueNames;
  for (const auto &meme: names) {
    uniqueNames.emplace(meme);
  }
  return join(CONSTELLATION_DELIMITER, uniqueNames);
}

std::set<std::string> MemeConstellation::toNames(const std::string &constellation)  {
  return split(constellation, CONSTELLATION_DELIMITER);
}

std::string MemeConstellation::join(const std::string &delimiter, const std::set<std::string> &pieces) {
  std::ostringstream joined;
  std::copy(pieces.begin(), pieces.end(), std::ostream_iterator<std::string>(joined, delimiter.c_str()));
  const std::string result = joined.str();
  return result.substr(0, result.size() - delimiter.size()); // remove the last delimiter
}

std::set<std::string> MemeConstellation::split(const std::string &str, const std::string &delimiter)  {
  std::set<std::string> tokens;
  size_t start = 0, end;
  while ((end = str.find(delimiter, start)) != std::string::npos) {
    tokens.insert(str.substr(start, end - start));
    start = end + delimiter.size();
  }
  tokens.insert(str.substr(start));
  return tokens;
}

const std::string MemeConstellation::CONSTELLATION_DELIMITER = "_";
