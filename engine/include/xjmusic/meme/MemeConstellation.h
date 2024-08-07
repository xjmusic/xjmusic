// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#ifndef XJ_MUSIC_ENTITIES_MEME_CONSTELLATION_H
#define XJ_MUSIC_ENTITIES_MEME_CONSTELLATION_H

#include <algorithm>
#include <iterator>
#include <map>
#include <ostream>
#include <set>
#include <sstream>
#include <string>
#include <unordered_map>
#include <variant>
#include <vector>

namespace XJ {

  /**
   Compute normalized string representation of an unordered set of memes
   for the purpose of identifying unique constellations.
   <p>
   for each unique sequence-pattern-meme constellation within the main sequence https://github.com/xjmusic/xjmusic/issues/208
   */
  class MemeConstellation {
  public:
    static const std::string CONSTELLATION_DELIMITER;

    static std::string fromNames(const std::set<std::string> &names);

    static std::set<std::string> toNames(const std::string& constellation);

  private:
    static std::string join(const std::string &delimiter, const std::set<std::string> &pieces);

    static std::set<std::string> split(const std::string &str, const std::string &delimiter);
  };
}

#endif//XJ_MUSIC_ENTITIES_MEME_CONSTELLATION_H