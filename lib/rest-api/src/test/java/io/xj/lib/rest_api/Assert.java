// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.rest_api;

import com.google.common.collect.Maps;

import java.util.Collection;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
   */
  public static void assertSameItems(Collection<String> expect, Collection<String> actual) {
    assertEquals(String.format("Different number create items! expected=[%s] vs actual=[%s]", String.join(",", expect), String.join(",", actual)), expect.size(), actual.size());

    // prepare a map of expected segment chord, all marked false (not yet found)
    Map<String, Boolean> found = Maps.newHashMap();
    expect.forEach(item -> found.put(item, false));

    // for each found segment chord, assert that we were expecting it, assert that it hasn't been found yet, then mark that it's been found
    actual.forEach(item -> {
      assertTrue(String.format("Not expecting item %s", PayloadKey.toSingleQuoted(item)), found.containsKey(item));
      assertFalse(String.format("Already encountered %s and can't have a duplicate", PayloadKey.toSingleQuoted(item)), found.get(item));
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
      assertTrue(String.format("Not expecting key %s", PayloadKey.toSingleQuoted(key)), found.containsKey(key));
      assertFalse(String.format("Already encountered %s and can't have a duplicate", PayloadKey.toSingleQuoted(key)), found.get(key));
      assertEquals(String.format("Values equal for key %s", PayloadKey.toSingleQuoted(key)), expect.get(key), actual.get(key));
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
