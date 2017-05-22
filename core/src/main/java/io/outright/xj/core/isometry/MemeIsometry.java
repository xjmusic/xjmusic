// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.isometry;

import io.outright.xj.core.model.MemeEntity;
import io.outright.xj.core.model.link_meme.LinkMeme;
import io.outright.xj.core.transport.CSV;

import org.jooq.Record;
import org.jooq.Result;

import com.google.common.collect.Lists;

import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import java.util.List;

/**
 Determine the isometry between a source and target group of Memes
 */
public class MemeIsometry {
  private static final String FIELD_NAME = "name";
  private static final double SCORE_EACH_MATCHED_MEME = 0.25;
  private final List<String> sourceStems;

  /**
   Private constructor

   @param sourceMemes source group of memes
   */
  private MemeIsometry(List<? extends MemeEntity> sourceMemes) {
    sourceStems = Lists.newArrayList();
    sourceMemes.forEach(meme ->
      sourceStems.add(stem(meme.getName())));
  }

  /**
   Instantiate a new MemeIsometry from a group of source Memes

   @param sourceMemes to compare from
   @return MemeIsometry ready for comparison to target Memes
   */
  public static MemeIsometry of(List<? extends MemeEntity> sourceMemes) {
    return new MemeIsometry(sourceMemes);
  }

  /**
   Instantiate a new MemeIsometry from a group of source Memes,
   as expressed in a a Result of jOOQ records.

   @param sourceMemeRecords to compare from
   @return MemeIsometry ready for comparison to target Memes
   */
  public static <R extends Record> MemeIsometry of(Result<R> sourceMemeRecords) {
    List<MemeEntity> sourceMemes = Lists.newArrayList();

    // use LinkMeme as a generic meme-- we could use any extender of MemeEntity
    sourceMemeRecords.forEach(record -> sourceMemes.add(
      new LinkMeme().setName(String.valueOf(record.get(FIELD_NAME)))
    ));

    return new MemeIsometry(sourceMemes);
  }

  /**
   Get the source Memes

   @return source memes
   */
  List<String> getSourceStems() {
    return sourceStems;
  }

  /**
   Score a CSV list of memes based on isometry to source memes

   @param memesCSV comma-separated values to score against source meme names
   @return score
   */
  public double scoreCSV(String memesCSV) {
    double score = 0;
    List<String> targetMemes = CSV.split(memesCSV);

    // tally each match of source & target stem
    for (String targetMeme : targetMemes) {

      String targetStem = stem(targetMeme);
      for (String sourceStem : sourceStems) {
        if (sourceStem.equals(targetStem))
          score+= SCORE_EACH_MATCHED_MEME;
      }
    }
    return score;
  }

  /**
   Snowball stem of a particular word

   @param raw text to get stem of
   @return stem
   */
  private String stem(String raw) {
    SnowballStemmer stemmer = new englishStemmer();
    stemmer.setCurrent(raw.toLowerCase().trim());
    stemmer.stem();
    return stemmer.getCurrent();
  }
}
