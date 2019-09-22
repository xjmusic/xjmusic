//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.isometry;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.apache.commons.codec.language.DoubleMetaphone;

/**
 Determine the isometry between a source and target group of Events
 */
public class NameIsometry extends Isometry {
  private static final double SIMILARITY_SCORE_MATCHING_NAME = 3;

  /**
   Instantiate a new NameIsometry from a group of source Events

   @param sourceNames to compare from
   @return NameIsometry ready for comparison to target Events
   */
  public static NameIsometry ofEvents(Iterable<String> sourceNames) {
    NameIsometry result = new NameIsometry();
    sourceNames.forEach(result::addPhonetic);
    return result;
  }

  /**
   [#252] Similarity between two events implements Double Metaphone phonetic similarity algorithm

   @param name1 to compare
   @param name2 to compare
   @return score
   */
  public static double similarity(String name1, String name2) {
    double score = 0;
    DoubleMetaphone dm = new DoubleMetaphone();

    // score includes double-metaphone phonetic fuzzy-match of name
    score += SIMILARITY_SCORE_MATCHING_NAME * FuzzySearch.ratio(
      dm.doubleMetaphone(name1),
      dm.doubleMetaphone(name2)
    );

    return score;
  }

  /**
   Add an event for isometry comparison

   @param source to add
   */
  public void add(String source) {
    addPhonetic(source);
  }
}
