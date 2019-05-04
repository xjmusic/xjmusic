//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core;

import com.google.common.collect.Maps;
import io.xj.core.model.chord.Chord;
import io.xj.core.model.meme.Meme;
import io.xj.core.util.Text;

import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Assert {
  /**
   Assert values on test segment@param expectMemes to perform assertion on

   @param actualMemes to find exactly
   */
  public static <N extends Meme> void assertExactMemes(Collection<String> expectMemeNames, Collection<N> actualMemes) {
    assertEquals("Different number of memes than expected", expectMemeNames.size(), actualMemes.size());

    // prepare a map of expected segment meme, all marked false (not yet found)
    Map<String, Boolean> memesFound = Maps.newConcurrentMap();
    for (String expectMemeName : expectMemeNames) {
      memesFound.put(expectMemeName, false);
    }

    // for each found segment meme, assert that we were expecting it, assert that it hasn't been found yet, then mark that it's been found
    actualMemes.forEach(actualMeme -> {
      String actualMemeName = actualMeme.getName();
      assertTrue(String.format("Not expecting meme %s", Text.singleQuoted(actualMemeName)), memesFound.containsKey(actualMemeName));
      assertFalse(String.format("Already encountered %s and can't have a duplicate", Text.singleQuoted(actualMemeName)), memesFound.get(actualMemeName));
      memesFound.put(actualMemeName, true);
    });
  }

  /**
   Assert values on test segment@param expectChordNames to perform assertion on

   @param actualChords to find exactly
   */
  public static <N extends Chord> void assertExactChords(Collection<String> expectChordNames, Collection<N> actualChords) {
    assertEquals("Different number of chords than expected", expectChordNames.size(), actualChords.size());

    // prepare a map of expected segment chord, all marked false (not yet found)
    Map<String, Boolean> chordsFound = Maps.newConcurrentMap();
    for (String expectChordName : expectChordNames) {
      chordsFound.put(expectChordName, false);
    }

    // for each found segment chord, assert that we were expecting it, assert that it hasn't been found yet, then mark that it's been found
    actualChords.forEach(actualChord -> {
      String actualChordName = actualChord.getName();
      assertTrue(String.format("Not expecting chord %s", Text.singleQuoted(actualChordName)), chordsFound.containsKey(actualChordName));
      assertFalse(String.format("Already encountered %s and can't have a duplicate", Text.singleQuoted(actualChordName)), chordsFound.get(actualChordName));
      chordsFound.put(actualChordName, true);
    });
  }

}
