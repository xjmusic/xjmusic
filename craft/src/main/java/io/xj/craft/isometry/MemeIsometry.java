// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.isometry;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import io.xj.core.model.meme.Meme;
import io.xj.core.model.pattern_meme.PatternMeme;

import java.util.List;
import java.util.Map;

/**
 Determine the isometry between a source and target group of Memes
 */
public class MemeIsometry extends Isometry {

  /**
   Instantiate a new MemeIsometry from a group of source Memes,
   as expressed in a a Result of jOOQ records.

   @param sourceMemes to compare from
   @return MemeIsometry ready for comparison to target Memes
   */
  public static <R extends Meme> MemeIsometry ofMemes(Iterable<R> sourceMemes) {
    MemeIsometry result = new MemeIsometry();
    sourceMemes.forEach(meme ->
      result.addStem(meme.getName()));
    return result;
  }


  /**
   Instantiate a new MemeIsometry from a map of source Memes

   @param stringMemeMap to compare from
   @return MemeIsometry ready for comparison to target Memes
   */
  public static MemeIsometry ofMemes(Map<String, Meme> stringMemeMap) {
    List<Meme> sourceMemes = Lists.newArrayList();

    stringMemeMap.forEach((key, record) -> sourceMemes.add(
      new PatternMeme().setName(record.getName())
    ));

    return ofMemes(sourceMemes);
  }

  /**
   Score a CSV list of memes based on isometry to source memes

   @param targetMemes comma-separated values to score against source meme names
   @return score is between 0 (no matches) and 1 (all memes match)
   */
  public <M extends Meme> double score(Iterable<M> targetMemes) {
    double tally = 0;

    // tally each match of source & target stem
    for (M targetMeme : targetMemes) {

      String targetStem = stem(targetMeme.getName());
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
  public <R extends Meme> void add(R meme) {
    addStem(stem(meme.getName()));
  }
}
