// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.isometry;

import io.xj.core.model.voice.Voice;

import com.google.common.collect.Maps;

import me.xdrop.fuzzywuzzy.FuzzySearch;
import org.tartarus.snowball.SnowballStemmer;
import org.tartarus.snowball.ext.englishStemmer;

import javax.annotation.Nullable;
import java.util.Map;

/**
 Determine the isometry between a source and target group of Voices
 */
public class VoiceIsometry {
  private final Map<String, Voice> stemmedSourceVoices;

  /**
   Private constructor

   @param sourceVoices source group of voices
   */
  private VoiceIsometry(Iterable<? extends Voice> sourceVoices) {
    stemmedSourceVoices = Maps.newConcurrentMap();
    sourceVoices.forEach(voice ->
      stemmedSourceVoices.put(stem(voice.getDescription()), voice));
  }

  /**
   Instantiate a new VoiceIsometry from a group of source Voices,
   as expressed in a a Result of jOOQ records.

   @param sourceVoices to compare from
   @return VoiceIsometry ready for comparison to target Voices
   */
  public static <R extends Voice> VoiceIsometry of(Iterable<R> sourceVoices) {
    return new VoiceIsometry(sourceVoices);
  }


  /**
   Find the closest match from within source voices

   @param needle to search for in source voices
   @return closest match
   */
  @Nullable
  public Voice find(Voice needle) {
    Voice result = null;
    double resultScore = 0;

    String targetStem = stem(needle.getDescription());
    for (Map.Entry<String, Voice> stringVoiceEntry : stemmedSourceVoices.entrySet()) {
      double sourceScore = FuzzySearch.ratio(stringVoiceEntry.getKey(), targetStem);
      if (sourceScore > resultScore) {
        result = stringVoiceEntry.getValue();
        resultScore = sourceScore;
      }
    }

    return result;
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
