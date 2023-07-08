// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.meme;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 Compute normalized string representation of an unordered set of memes
 for the purpose of identifying unique constellations.
 <p>
 for each unique sequence-pattern-meme constellation within the main sequence https://www.pivotaltracker.com/story/show/161736024
 */
public enum MemeConstellation {
  ;
  static final String CONSTELLATION_DELIMITER = "_";

  /**
   @return unique constellation for this set of names
   */
  public static String fromNames(Collection<String> names) {
    Map<String, Boolean> uniqueNames = Maps.newHashMap();
    names.forEach(meme -> uniqueNames.put(meme, true));
    Set<String> pieces = Sets.newTreeSet(uniqueNames.keySet());
    return String.join(CONSTELLATION_DELIMITER, pieces);
  }

  /**
   @return set of names parsed from constellation
   */
  public static Collection<String> toNames(String constellation) {
    return List.of(constellation.split(CONSTELLATION_DELIMITER));
  }
}
