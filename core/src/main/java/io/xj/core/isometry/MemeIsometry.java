// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.isometry;

import io.xj.core.model.meme.Meme;
import io.xj.core.transport.CSV;

import org.jooq.Record;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 Determine the isometry between a source and target group of Memes
 */
public class MemeIsometry {
  private static final String FIELD_NAME = "name";
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
   Instantiate a new MemeIsometry from a group of source Memes

   @param sourceMemes to compare from
   @return MemeIsometry ready for comparison to target Memes
   */
  public static MemeIsometry of(Collection<? extends Meme> sourceMemes) {
    return new MemeIsometry(sourceMemes);
  }

  /**
   Instantiate a new MemeIsometry from a group of source Memes,
   as expressed in a a Result of jOOQ records.

   @param sourceMemeRecords to compare from
   @return MemeIsometry ready for comparison to target Memes
   */
  public static <R extends Record> MemeIsometry of(Iterable<R> sourceMemeRecords) {
    List<Meme> sourceMemes = Lists.newArrayList();

    sourceMemeRecords.forEach(record -> sourceMemes.add(
      new Meme().setName(String.valueOf(record.get(FIELD_NAME)))
    ));

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
      new Meme().setName(record.getName())
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

   @param memesCSV comma-separated values to score against source meme names
   @return score is between 0 (no matches) and 1 (all memes match)
   */
  public double scoreCSV(String memesCSV) {
    Collection<Meme> memes = Lists.newArrayList();
    CSV.split(memesCSV).forEach((name) -> {
      memes.add(new Meme().setName(name)); // can use any meme impl
    });
    return score(memes);
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
