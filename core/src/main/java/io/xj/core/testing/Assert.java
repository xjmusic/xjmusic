//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.testing;

import com.google.common.collect.Maps;
import io.xj.core.model.entity.Chord;
import io.xj.core.model.entity.Meme;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public enum Assert {
  ;

  /**
   Assert values on test segment@param expectMemes to perform assertion on

   @param actualMemes to find exactly
   */
  public static <N extends Meme> void assertExactMemes(Collection<String> expectMemeNames, Collection<N> actualMemes) {
    assertEquals("Different number of memes than expected", expectMemeNames.size(), actualMemes.size());

    // prepare a map of expected segment meme, all marked false (not yet found)
    Map<String, Boolean> memesFound = Maps.newHashMap();
    for (String expectMemeName : expectMemeNames) {
      memesFound.put(expectMemeName, false);
    }

    // for each found segment meme, assert that we were expecting it, assert that it hasn't been found yet, then mark that it's been found
    actualMemes.forEach(actualMeme -> {
      String actualMemeName = actualMeme.getName();
      assertTrue(String.format("Not expecting meme %s", Text.toSingleQuoted(actualMemeName)), memesFound.containsKey(actualMemeName));
      assertFalse(String.format("Already encountered %s and can't have a duplicate", Text.toSingleQuoted(actualMemeName)), memesFound.get(actualMemeName));
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
    Map<String, Boolean> chordsFound = Maps.newHashMap();
    for (String expectChordName : expectChordNames) {
      chordsFound.put(expectChordName, false);
    }

    // for each found segment chord, assert that we were expecting it, assert that it hasn't been found yet, then mark that it's been found
    actualChords.forEach(actualChord -> {
      String actualChordName = actualChord.getName();
      assertTrue(String.format("Not expecting chord %s", Text.toSingleQuoted(actualChordName)), chordsFound.containsKey(actualChordName));
      assertFalse(String.format("Already encountered %s and can't have a duplicate", Text.toSingleQuoted(actualChordName)), chordsFound.get(actualChordName));
      chordsFound.put(actualChordName, true);
    });
  }

  /**
   Assert that two collections of strings have the same items

   @param expect items
   @param actual items to assert same
   */
  public static void assertSameItems(Collection<String> expect, Collection<String> actual) {
    assertEquals(String.format("Different number of items! expected=[%s] vs actual=[%s]", CSV.join(expect), CSV.join(actual)), expect.size(), actual.size());

    // prepare a map of expected segment chord, all marked false (not yet found)
    Map<String, Boolean> found = Maps.newHashMap();
    expect.forEach(item -> found.put(item, false));

    // for each found segment chord, assert that we were expecting it, assert that it hasn't been found yet, then mark that it's been found
    actual.forEach(item -> {
      assertTrue(String.format("Not expecting item %s", Text.toSingleQuoted(item)), found.containsKey(item));
      assertFalse(String.format("Already encountered %s and can't have a duplicate", Text.toSingleQuoted(item)), found.get(item));
      found.put(item, true);
    });
  }

  /**
   Assert that two maps of strings to objects have the same items

   @param expect items
   @param actual items to assert same
   */
  public static void assertSameItems(Map<String, ?> expect, Map<String, ?> actual) {
    assertEquals(String.format("Different number of keys! expected=[%s] vs actual=[%s]", expect.toString(), actual.toString()), expect.size(), actual.size());

    // prepare a map of expected segment chord, all marked false (not yet found)
    Map<String, Boolean> found = Maps.newHashMap();
    expect.forEach((key, value) -> found.put(key, false));

    // for each found segment chord, assert that we were expecting it, assert that it hasn't been found yet, then mark that it's been found
    actual.forEach((key, value) -> {
      assertTrue(String.format("Not expecting key %s", Text.toSingleQuoted(key)), found.containsKey(key));
      assertFalse(String.format("Already encountered %s and can't have a duplicate", Text.toSingleQuoted(key)), found.get(key));
      assertEquals(String.format("Values equal for key %s", Text.toSingleQuoted(key)), expect.get(key), actual.get(key));
      found.put(key, true);
    });
  }

  /**
   Assert string contains expected string

   @param actual         string to search
   @param expectContains string to assert contained
   */
  public static void assertContains(String expectContains, String actual) {
    assertTrue(String.format("'%s' contains '%s'", actual, expectContains), actual.equals(expectContains) || actual.contains(expectContains));
  }

}
