// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.common.base.Objects;
import io.xj.lib.entity.Entities;
import io.xj.lib.entity.EntityException;
import io.xj.lib.util.Text;

import java.util.Collection;

/**
 Determine the isometry between a source and target group of Memes
 */
public class MemeIsometry extends Isometry {
  private static final String KEY_NAME = "name";
  private static final String NOT_PREFIX = "!";
  private static final int WEIGHT_MATCH = 1;
  private static final int WEIGHT_ANTIMATCH = 10;

  /**
   Instantiate a new MemeIsometry of a group of source Memes,
   as expressed in a a Result of jOOQ records.

   @param sourceMemes to compare of
   @return MemeIsometry ready for comparison to target Memes
   */
  public static MemeIsometry ofMemes(Collection<String> sourceMemes) {
    MemeIsometry result = new MemeIsometry();
    for (String meme : sourceMemes)
      result.add(Text.toMeme(meme));
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

   @param targets comma-separated values to score against source meme names
   @return score is between 0 (no matches) and 1 (all memes match)
   */
  public double score(Collection<String> targets) {
    return targets.stream()
      .map(Text::toMeme)
      .flatMap(target -> sources.stream().map(source -> score(source, target)))
      .reduce(0, Integer::sum) / (double) sources.size();
  }

  /**
   Score tallies a match (memes are equal) or anti-match (one meme is !not the other)

   @param source meme
   @param target meme
   @return score
   */
  private int score(String source, String target) {
    if (Objects.equal(source, target)) return WEIGHT_MATCH;
    if (NOT_PREFIX.equals(source.substring(0, 1)) && source.substring(1).equals(target))
      return -WEIGHT_ANTIMATCH;
    if (NOT_PREFIX.equals(target.substring(0, 1)) && target.substring(1).equals(source))
      return -WEIGHT_ANTIMATCH;
    return 0;
  }

  /**
   Add a meme for isometry comparison
   */
  public <R> void add(R meme) {
    try {
      Entities.get(meme, KEY_NAME)
        .ifPresent(name -> add(Text.toMeme(String.valueOf(name))));
    } catch (EntityException ignored) {
    }
  }
}
