// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/fabricator/NameIsometry.h"

/*
TODO remove Java imports
  package io.xj.nexus.fabricator;
  import io.xj.hub.meme.Isometry;
  import me.xdrop.fuzzywuzzy.FuzzySearch;
  import org.apache.commons.codec.language.DoubleMetaphone;
*/

/**
 Determine the isometry between a source and target group of Events
 */
public class NameIsometry extends Isometry {
  static final double SIMILARITY_SCORE_MATCHING_NAME = 3;

  /**
   Instantiate a new NameIsometry of a group of source Events

   @param sourceNames to compare of
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
  public static int similarity(String name1, String name2) {
    DoubleMetaphone dm = new DoubleMetaphone();

    // score includes double-metaphone phonetic fuzzy-match of name
    return (int) (SIMILARITY_SCORE_MATCHING_NAME * FuzzySearch.ratio(
      dm.doubleMetaphone(name1),
      dm.doubleMetaphone(name2)
    ));
  }

  /**
   Add an event for isometry comparison

   @param source to add
   */
  public void add(String source) {
    addPhonetic(source);
  }
}
