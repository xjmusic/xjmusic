// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.lib.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Assertion utilities for testing Payload sent/received to/from a XJ Music REST JSON:API service
 * <p>
 * Created by Charney Kaye on 2020/03/05
 */
public enum Assertion {
  ;

  /**
   * Assert that two collections of strings have the same items
   *
   * @param expect items
   * @param actual items to assert same
   * @throws ValueException if not the same items
   */
  public static void assertSameItems(Collection<?> expect, Collection<?> actual) throws ValueException {
    assertEquality(String.format("Different number create items! expected=[%s] vs actual=[%s]",
        expect.stream().map(Object::toString).collect(Collectors.joining(",")),
        actual.stream().map(Object::toString).collect(Collectors.joining(","))),
      expect.size(), actual.size());

    // prepare a map of expected segment chord, all marked false (not yet found)
    Map<Object, Boolean> found = new HashMap<>();
    expect.forEach(item -> found.put(item, false));

    // for each found segment chord, assert that we were expecting it, assert that it hasn't been found yet, then mark that it's been found
    for (Object item : actual) {
      assertTrue(String.format("Not expecting item %s", StringUtils.singleQuoted(item.toString())), found.containsKey(item));
      assertFalse(String.format("Already encountered %s and can't have a duplicate", StringUtils.singleQuoted(item.toString())), found.get(item));
      found.put(item, true);
    }
  }

  /**
   * Assert two objects are equal
   *
   * @param message to throw in exception if not equal
   * @param expect  to compare
   * @param actual  to compare
   * @throws ValueException if not equal
   */
  public static void assertEquality(String message, Object expect, Object actual) throws ValueException {
    if (!Objects.equals(expect, actual)) throw new ValueException(message);
  }

  /**
   * Assert a condition is true
   *
   * @param message to throw in exception if not true
   * @param actual  of which to assert truthiness
   * @throws ValueException if not true
   */
  public static void assertTrue(String message, Boolean actual) throws ValueException {
    if (!actual) throw new ValueException(message);
  }

  /**
   * Assert a condition is false
   *
   * @param message to throw in exception if not false
   * @param actual  of which to assert falsehood
   * @throws ValueException if not false
   */
  public static void assertFalse(String message, Boolean actual) throws ValueException {
    if (actual) throw new ValueException(message);
  }

  /**
   * Assert that two maps of strings to objects have the same items
   *
   * @param expect items
   * @param actual items to assert same
   * @throws ValueException if not the same items
   */
  public static void assertSameItems(Map<String, ?> expect, Map<String, ?> actual) throws ValueException {
    assertEquality(String.format("Different number create keys! expected=[%s] vs actual=[%s]", expect.toString(), actual.toString()), expect.size(), actual.size());

    // prepare a map of expected segment chord, all marked false (not yet found)
    Map<String, Boolean> found = new HashMap<>();
    expect.forEach((key, value) -> found.put(key, false));

    // for each found segment chord, assert that we were expecting it, assert that it hasn't been found yet, then mark that it's been found
    for (Map.Entry<String, ?> entry : actual.entrySet()) {
      String key = entry.getKey();
      assertTrue(String.format("Not expecting key %s", StringUtils.singleQuoted(key)), found.containsKey(key));
      assertFalse(String.format("Already encountered %s and can't have a duplicate", StringUtils.singleQuoted(key)), found.get(key));
      assertEquality(String.format("Values equal for key %s", StringUtils.singleQuoted(key)), expect.get(key), actual.get(key));
      found.put(key, true);
    }
  }

  /**
   * Assert that the given test output file matches the reference file
   *
   * @param referenceFilePath as source of truth
   * @param targetFilePath    to test
   * @throws IOException    on failure to read
   * @throws ValueException on bad value
   */
  public static void assertFileMatchesResourceFile(String referenceFilePath, String targetFilePath) throws IOException, ValueException {
    // The path of the generated file on disk
    Path generatedFilePath = Paths.get(targetFilePath);

    // The path of the file in the test resources
    var resource = new InternalResource(referenceFilePath);

    // Read all bytes from both files
    byte[] generatedFileBytes = Files.readAllBytes(generatedFilePath);
    byte[] testFileBytes = Files.readAllBytes(resource.getFile().toPath());

    // Assert that the two files are equal
    assertArrayEquals(testFileBytes, generatedFileBytes);
  }

  private static void assertArrayEquals(byte[] testFileBytes, byte[] generatedFileBytes) throws ValueException {
    assertEquality("Files are not the same size", testFileBytes.length, generatedFileBytes.length);
    for (int i = 0; i < testFileBytes.length; i++) {
      assertEquality(String.format("Files differ at byte %d", i), testFileBytes[i], generatedFileBytes[i]);
    }
  }
}
