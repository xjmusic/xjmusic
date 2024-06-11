// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

#ifndef XJ_MUSIC_ENTITIES_MEME_CONSTELLATION_H
#define XJ_MUSIC_ENTITIES_MEME_CONSTELLATION_H

#include <string>
#include <vector>
#include <unordered_map>
#include <set>
#include <sstream>
#include <algorithm>
#include <ostream>
#include <iterator>

namespace XJ {

  /**
   Compute normalized string representation of an unordered set of memes
   for the purpose of identifying unique constellations.
   <p>
   for each unique sequence-pattern-meme constellation within the main sequence https://github.com/xjmusic/workstation/issues/208
   */
  class MemeConstellation {
  public:
    static const std::string CONSTELLATION_DELIMITER;

    static std::string fromNames(const std::set<std::string> &names);

    static std::vector<std::string> toNames(const std::string& constellation);

  private:
    static std::string join(const std::string &delimiter, const std::set<std::string> &pieces);

    static std::vector<std::string> split(const std::string &str, const std::string &delimiter);
  };

  const std::string MemeConstellation::CONSTELLATION_DELIMITER = "_";
}

#endif//XJ_MUSIC_ENTITIES_MEME_CONSTELLATION_H