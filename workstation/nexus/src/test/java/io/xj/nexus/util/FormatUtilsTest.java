// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.util;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class FormatUtilsTest {

  @Test
  void formatFractionalSuffix() {
    assertEquals("½", FormatUtils.formatFractionalSuffix(0.5));
    assertEquals("¼", FormatUtils.formatFractionalSuffix(0.25));
    assertEquals("¾", FormatUtils.formatFractionalSuffix(0.75));
    assertEquals("⅓", FormatUtils.formatFractionalSuffix(0.33333334));
    assertEquals("⅔", FormatUtils.formatFractionalSuffix(0.6666667));
    assertEquals("⅕", FormatUtils.formatFractionalSuffix(0.2));
    assertEquals("⅖", FormatUtils.formatFractionalSuffix(0.4));
    assertEquals("⅗", FormatUtils.formatFractionalSuffix(0.6));
    assertEquals("⅘", FormatUtils.formatFractionalSuffix(0.8));
    assertEquals("⅙", FormatUtils.formatFractionalSuffix(0.16666667));
    assertEquals("⅚", FormatUtils.formatFractionalSuffix(0.8333333));
    assertEquals("⅐", FormatUtils.formatFractionalSuffix(0.14285715));
    assertEquals("⅛", FormatUtils.formatFractionalSuffix(0.125));
    assertEquals("⅜", FormatUtils.formatFractionalSuffix(0.375));
    assertEquals(".38", FormatUtils.formatFractionalSuffix(0.38));
    assertEquals("⅝", FormatUtils.formatFractionalSuffix(0.625));
    assertEquals("⅞", FormatUtils.formatFractionalSuffix(0.875));
    assertEquals("⅑", FormatUtils.formatFractionalSuffix(0.11111111));
    assertEquals("⅒", FormatUtils.formatFractionalSuffix(0.1));
    assertEquals("", FormatUtils.formatFractionalSuffix(0.0));
    assertEquals("", FormatUtils.formatFractionalSuffix(1.0));
    assertEquals(".15", FormatUtils.formatFractionalSuffix(0.15));
    assertEquals(".39", FormatUtils.formatFractionalSuffix(0.39));
    assertEquals(".381", FormatUtils.formatFractionalSuffix(0.381239));
  }

  @Test
  void formatDecimalSuffix() {
    assertEquals(".5", FormatUtils.formatDecimalSuffix(0.5));
    assertEquals(".25", FormatUtils.formatDecimalSuffix(0.25));
    assertEquals(".75", FormatUtils.formatDecimalSuffix(0.75));
    assertEquals(".333", FormatUtils.formatDecimalSuffix(0.33333334));
    assertEquals(".667", FormatUtils.formatDecimalSuffix(0.6666667));
    assertEquals(".2", FormatUtils.formatDecimalSuffix(0.2));
    assertEquals(".4", FormatUtils.formatDecimalSuffix(0.4));
    assertEquals(".6", FormatUtils.formatDecimalSuffix(0.6));
    assertEquals(".8", FormatUtils.formatDecimalSuffix(0.8));
    assertEquals(".167", FormatUtils.formatDecimalSuffix(0.16666667));
    assertEquals(".833", FormatUtils.formatDecimalSuffix(0.8333333));
    assertEquals(".143", FormatUtils.formatDecimalSuffix(0.14285715));
    assertEquals(".125", FormatUtils.formatDecimalSuffix(0.125));
    assertEquals(".375", FormatUtils.formatDecimalSuffix(0.375));
    assertEquals(".38", FormatUtils.formatDecimalSuffix(0.38));
    assertEquals(".625", FormatUtils.formatDecimalSuffix(0.625));
    assertEquals(".875", FormatUtils.formatDecimalSuffix(0.875));
    assertEquals(".111", FormatUtils.formatDecimalSuffix(0.11111111));
    assertEquals(".1", FormatUtils.formatDecimalSuffix(0.1));
    assertEquals("", FormatUtils.formatDecimalSuffix(0.0));
    assertEquals("", FormatUtils.formatDecimalSuffix(1.0));
    assertEquals(".15", FormatUtils.formatDecimalSuffix(0.15));
    assertEquals(".39", FormatUtils.formatDecimalSuffix(0.39));
    assertEquals(".381", FormatUtils.formatDecimalSuffix(0.381239));
  }

  @Test
  void formatMinDecimal() {
    assertEquals("1.5", FormatUtils.formatMinDecimal(1.5));
    assertEquals("1.522", FormatUtils.formatMinDecimal(1.522));
    assertEquals("1.522", FormatUtils.formatMinDecimal(1.522346));
  }

  @Test
  void formatTimeFromMicros() {
    assertEquals("3h29m12s", FormatUtils.formatTimeFromMicros(12551221510L));
    assertEquals("2m6s", FormatUtils.formatTimeFromMicros(125512510L));
    assertEquals("2.2s", FormatUtils.formatTimeFromMicros(1251510L));
  }

  @Test
  void iterateNumericalSuffixFromExisting() {
    assertEquals("Thing", FormatUtils.iterateNumericalSuffixFromExisting(Set.of(), "Thing"));
    assertEquals("Thing 2", FormatUtils.iterateNumericalSuffixFromExisting(Set.of("Thing"), "Thing"));
    assertEquals("Thing 2", FormatUtils.iterateNumericalSuffixFromExisting(Set.of("Thing 1"), "Thing"));
    assertEquals("Thing 3", FormatUtils.iterateNumericalSuffixFromExisting(Set.of("Thing 1", "Thing 2"), "Thing"));
  }

  @Test
  void describeCounts() {
    assertEquals("3 apples, 2 bananas, and 1 peach", FormatUtils.describeCounts(Map.of("peach", 1, "apple", 3, "banana", 2)));
    assertEquals("3 apples and 1 peach", FormatUtils.describeCounts(Map.of("peach", 1, "apple", 3, "banana", 0)));
  }

  @Test
  void describeCount() {
    assertNull(FormatUtils.describeCount("banana", 0));
    assertEquals("1 banana", FormatUtils.describeCount("banana", 1));
    assertEquals("2 bananas", FormatUtils.describeCount("banana", 2));
  }
}
