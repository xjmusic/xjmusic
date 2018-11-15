// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.isometry;

import com.google.common.collect.Maps;
import io.xj.core.model.voice.Voice;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import javax.annotation.Nullable;
import java.util.Map;

/**
 Determine the isometry between a source and target group of Voices
 */
public class VoiceIsometry extends Isometry {
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
  public static <R extends Voice> VoiceIsometry ofVoices(Iterable<R> sourceVoices) {
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

}
