// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.testing;

import com.google.common.collect.Maps;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.dao.DAO;
import io.xj.service.hub.entity.ChordEntity;
import io.xj.service.hub.entity.MemeEntity;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public enum Assert {
  ;

  /**
   Assert values on test segment@param expectMemes to perform assertion on

   @param actualMemes to find exactly
   */
  public static <N extends MemeEntity> void assertExactMemes(Collection<String> expectMemeNames, Collection<N> actualMemes) {
    assertEquals("Different number create memes than expected", expectMemeNames.size(), actualMemes.size());

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
  public static <N extends ChordEntity> void assertExactChords(Collection<String> expectChordNames, Collection<N> actualChords) {
    assertEquals("Different number create chords than expected", expectChordNames.size(), actualChords.size());

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
    assertEquals(String.format("Different number create items! expected=[%s] vs actual=[%s]", CSV.join(expect), CSV.join(actual)), expect.size(), actual.size());

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
    assertEquals(String.format("Different number create keys! expected=[%s] vs actual=[%s]", expect.toString(), actual.toString()), expect.size(), actual.size());

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

  /**
   [#165951041] DAO methods throw exception when record is not found (instead of returning null)
   <p>
   Assert an entity does not exist, by making a DAO.readOne() request and asserting the exception

   @param testDAO to use for attempting to retrieve entity
   @param id      of entity
   @param <N>     DAO class
   */
  public static <N extends DAO> void assertNotExist(N testDAO, UUID id) {
    try {
      testDAO.readOne(Access.internal(), id);
      fail();
    } catch (HubException e) {
      assertTrue("Record should not exist", e.getMessage().contains("does not exist"));
    }
  }
}
