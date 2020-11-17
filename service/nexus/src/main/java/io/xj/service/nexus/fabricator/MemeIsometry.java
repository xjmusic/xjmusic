// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.common.base.Objects;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;

import java.util.Collection;

/**
 Determine the isometry between a source and target group of Memes
 */
public class MemeIsometry extends Isometry {
  private static final String KEY_NAME = "name";

  /**
   Instantiate a new MemeIsometry of a group of source Memes,
   as expressed in a a Result of jOOQ records.

   @param sourceMemes to compare of
   @return MemeIsometry ready for comparison to target Memes
   */
  public static MemeIsometry ofMemes(Collection<String> sourceMemes) throws EntityException {
    MemeIsometry result = new MemeIsometry();
    for (String meme : sourceMemes)
      result.addStem(meme);
    return result;
  }

  /**
   Instantiate a new MemeIsometry representing having no memes

   @return an empty MemeIsometry
   */
  public static MemeIsometry none() {
    return new MemeIsometry();
  }

  /**
   Score a CSV list of memes based on isometry to source memes

   @param targetMemes comma-separated values to score against source meme names
   @return score is between 0 (no matches) and 1 (all memes match)
   */
  public double score(Iterable<String> targetMemes) throws EntityException {
    double tally = 0;

    // tally each match of source & target stem
    for (String meme : targetMemes) {
      String targetStem = stem(meme);
      for (String sourceStem : getSources()) {
        if (Objects.equal(sourceStem, targetStem)) {
          tally += 1;
        }
      }
    }
    return tally / getSources().size();
  }

  /**
   Add a meme for isometry comparison
   */
  public <R> void add(R meme) throws EntityException {
    addStem(stem(String.valueOf(Entities.get(meme, KEY_NAME)
      .orElseThrow(() -> new EntityException("has no name attribute")))));
  }
}
