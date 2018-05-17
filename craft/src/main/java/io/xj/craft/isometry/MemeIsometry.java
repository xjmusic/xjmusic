// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.isometry;

import io.xj.core.model.meme.Meme;
import io.xj.core.model.pattern_meme.PatternMeme;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 Determine the isometry between a source and target group of Memes
 */
public class MemeIsometry {
  private final List<String> sourceStems;

  /**
   Private constructor

   @param sourceMemes source group of memes
   */
  private MemeIsometry(Iterable<? extends Meme> sourceMemes) {
    sourceStems = Lists.newArrayList();
    sourceMemes.forEach(meme ->
      sourceStems.add(stem(meme.getName())));
  }

  /**
   Instantiate a new MemeIsometry from a group of source Memes,
   as expressed in a a Result of jOOQ records.

   @param sourceMemes to compare from
   @return MemeIsometry ready for comparison to target Memes
   */
  public static <R extends Meme> MemeIsometry of(Iterable<R> sourceMemes) {
    return new MemeIsometry(sourceMemes);
  }

  /**
   Instantiate a new MemeIsometry from a map of source Memes

   @param stringMemeMap to compare from
   @return MemeIsometry ready for comparison to target Memes
   */
  public static MemeIsometry of(Map<String, Meme> stringMemeMap) {
    List<Meme> sourceMemes = Lists.newArrayList();

    stringMemeMap.forEach((key, record) -> sourceMemes.add(
      new PatternMeme().setName(record.getName())
    ));

    return new MemeIsometry(sourceMemes);
  }

  /**
   Get the source Memes

   @return source memes
   */
  List<String> getSourceStems() {
    return Collections.unmodifiableList(sourceStems);
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
      for (String sourceStem : sourceStems) {
        if (Objects.equal(sourceStem, targetStem)) {
          tally += 1;
        }
      }
    }
    return tally / sourceStems.size();
  }

  /**
   Snowball stem of a particular word

   @param raw text to get stem of
   @return stem
   */
  private static String stem(String raw) {
    SnowballStemmer stemmer = new englishStemmer(); // this is the only part proprietary to English
    stemmer.setCurrent(raw.toLowerCase().trim());
    stemmer.stem();
    return stemmer.getCurrent();
  }

}
