// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.util;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 Assertion utilities for testing Payload sent/received to/from a XJ Music REST JSON:API service
 <p>
 Created by Charney Kaye on 2020/03/05
 */
public enum Assert {
  ;


  /**
   Assert that two collections of strings have the same items

   @param expect items
   @param actual items to assert same
   @throws ValueException if not the same items
   */
  public static void assertSameItems(Collection<String> expect, Collection<String> actual) throws ValueException {
    assertEquals(String.format("Different number create items! expected=[%s] vs actual=[%s]", String.join(",", expect), String.join(",", actual)), expect.size(), actual.size());

    // prepare a map of expected segment chord, all marked false (not yet found)
    Map<String, Boolean> found = Maps.newHashMap();
    expect.forEach(item -> found.put(item, false));

    // for each found segment chord, assert that we were expecting it, assert that it hasn't been found yet, then mark that it's been found
    for (String item : actual) {
      assertTrue(String.format("Not expecting item %s", Text.toSingleQuoted(item)), found.containsKey(item));
      assertFalse(String.format("Already encountered %s and can't have a duplicate", Text.toSingleQuoted(item)), found.get(item));
      found.put(item, true);
    }
  }

  /**
   Assert two objects are equal

   @param message to throw in exception if not equal
   @param expect  to compare
   @param actual  to compare
   @throws ValueException if not equal
   */
  public static void assertEquals(String message, Object expect, Object actual) throws ValueException {
    if (!Objects.equals(expect, actual)) throw new ValueException(message);
  }

  /**
   Assert a condition is true

   @param message to throw in exception if not true
   @param actual  of which to assert truthiness
   @throws ValueException if not true
   */
  public static void assertTrue(String message, Boolean actual) throws ValueException {
    if (!actual) throw new ValueException(message);
  }

  /**
   Assert a condition is false

   @param message to throw in exception if not false
   @param actual  of which to assert falsehood
   @throws ValueException if not false
   */
  public static void assertFalse(String message, Boolean actual) throws ValueException {
    if (actual) throw new ValueException(message);
  }

  /**
   Assert that two maps of strings to objects have the same items

   @param expect items
   @param actual items to assert same
   @throws ValueException if not the same items
   */
  public static void assertSameItems(Map<String, ?> expect, Map<String, ?> actual) throws ValueException {
    assertEquals(String.format("Different number create keys! expected=[%s] vs actual=[%s]", expect.toString(), actual.toString()), expect.size(), actual.size());

    // prepare a map of expected segment chord, all marked false (not yet found)
    Map<String, Boolean> found = Maps.newHashMap();
    expect.forEach((key, value) -> found.put(key, false));

    // for each found segment chord, assert that we were expecting it, assert that it hasn't been found yet, then mark that it's been found
    for (Map.Entry<String, ?> entry : actual.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      assertTrue(String.format("Not expecting key %s", Text.toSingleQuoted(key)), found.containsKey(key));
      assertFalse(String.format("Already encountered %s and can't have a duplicate", Text.toSingleQuoted(key)), found.get(key));
      assertEquals(String.format("Values equal for key %s", Text.toSingleQuoted(key)), expect.get(key), actual.get(key));
      found.put(key, true);
    }
  }

  /**
   Assert string contains expected string

   @param actual         string to search
   @param expectContains string to assert contained
   */
  public static void assertContains(String expectContains, String actual) throws ValueException {
    assertTrue(String.format("'%s' contains '%s'", actual, expectContains), actual.equals(expectContains) || actual.contains(expectContains));
  }

}
