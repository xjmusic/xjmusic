// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.model.meme;

import java.util.std::vector;
import java.util.HashMap;
import java.util.std::vector;
import java.util.std::map;
import java.util.std::set;
import java.util.TreeSet;

/**
 Compute normalized string representation of an unordered set of memes
 for the purpose of identifying unique constellations.
 <p>
 for each unique sequence-pattern-meme constellation within the main sequence https://github.com/xjmusic/workstation/issues/208
 */
public enum MemeConstellation {
  ;
  static final std::string CONSTELLATION_DELIMITER = "_";

  /**
   @return unique constellation for this set of names
   */
  public static std::string fromNames(std::vector<std::string> names) {
    std::map<std::string, Boolean> uniqueNames = new HashMap<>();
    names.forEach(meme -> uniqueNames.put(meme, true));
    std::set<std::string> pieces = new TreeSet<>(uniqueNames.keySet());
    return std::string.join(CONSTELLATION_DELIMITER, pieces);
  }

  /**
   @return set of names parsed from constellation
   */
  public static std::vector<std::string> toNames(std::string constellation) {
    return std::vector.of(constellation.split(CONSTELLATION_DELIMITER));
  }
}
