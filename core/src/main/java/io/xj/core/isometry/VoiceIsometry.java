// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.isometry;

import com.google.common.collect.Maps;
import io.xj.core.model.ProgramVoice;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import javax.annotation.Nullable;
import java.util.Map;

/**
 Determine the isometry between a source and target group of Voices
 */
public class VoiceIsometry extends Isometry {
  private final Map<String, ProgramVoice> stemmedSourceVoices;

  /**
   Private constructor

   @param sourceVoices source group of voices
   */
  private VoiceIsometry(Iterable<? extends ProgramVoice> sourceVoices) {
    stemmedSourceVoices = Maps.newHashMap();
    sourceVoices.forEach(voice ->
      stemmedSourceVoices.put(stem(voice.getName()), voice));
  }

  /**
   Instantiate a new VoiceIsometry of a group of source Voices,
   as expressed in a a Result of jOOQ records.

   @param sourceVoices to compare of
   @return VoiceIsometry ready for comparison to target Voices
   */
  public static <R extends ProgramVoice> VoiceIsometry ofVoices(Iterable<R> sourceVoices) {
    return new VoiceIsometry(sourceVoices);
  }


  /**
   Find the closest match of within source voices

   @param needle to search for in source voices
   @return closest match
   */
  @Nullable
  public ProgramVoice find(ProgramVoice needle) {
    ProgramVoice result = null;
    double resultScore = 0;

    String targetStem = stem(needle.getName());
    for (Map.Entry<String, ProgramVoice> stringVoiceEntry : stemmedSourceVoices.entrySet()) {
      double sourceScore = FuzzySearch.ratio(stringVoiceEntry.getKey(), targetStem);
      if (sourceScore > resultScore) {
        result = stringVoiceEntry.getValue();
        resultScore = sourceScore;
      }
    }

    return result;
  }

}
