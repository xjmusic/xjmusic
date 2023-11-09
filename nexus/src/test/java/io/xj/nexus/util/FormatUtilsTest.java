// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.util;

import org.junit.jupiter.api.Test;

class FormatUtilsTest {

  @Test
  void formatFractionalSuffix() {
    assertEquals("½", FormatUtils.formatFractionalSuffix(0.5f));
    assertEquals("¼", FormatUtils.formatFractionalSuffix(0.25f));
    assertEquals("¾", FormatUtils.formatFractionalSuffix(0.75f));
    assertEquals("⅓", FormatUtils.formatFractionalSuffix(0.33333334f));
    assertEquals("⅔", FormatUtils.formatFractionalSuffix(0.6666667f));
    assertEquals("⅕", FormatUtils.formatFractionalSuffix(0.2f));
    assertEquals("⅖", FormatUtils.formatFractionalSuffix(0.4f));
    assertEquals("⅗", FormatUtils.formatFractionalSuffix(0.6f));
    assertEquals("⅘", FormatUtils.formatFractionalSuffix(0.8f));
    assertEquals("⅙", FormatUtils.formatFractionalSuffix(0.16666667f));
    assertEquals("⅚", FormatUtils.formatFractionalSuffix(0.8333333f));
    assertEquals("⅐", FormatUtils.formatFractionalSuffix(0.14285715f));
    assertEquals("⅛", FormatUtils.formatFractionalSuffix(0.125f));
    assertEquals("⅜", FormatUtils.formatFractionalSuffix(0.375f));
    assertEquals("⅜", FormatUtils.formatFractionalSuffix(0.38f));
    assertEquals("⅝", FormatUtils.formatFractionalSuffix(0.625f));
    assertEquals("⅞", FormatUtils.formatFractionalSuffix(0.875f));
    assertEquals("⅑", FormatUtils.formatFractionalSuffix(0.11111111f));
    assertEquals("⅒", FormatUtils.formatFractionalSuffix(0.1f));
    assertEquals("", FormatUtils.formatFractionalSuffix(0.0f));
    assertEquals("", FormatUtils.formatFractionalSuffix(1.0f));
    assertEquals(".15", FormatUtils.formatFractionalSuffix(0.15f));
    assertEquals(".39", FormatUtils.formatFractionalSuffix(0.39f));
    assertEquals(".381", FormatUtils.formatFractionalSuffix(0.381239f));
  }

  @Test
  void formatDecimalSuffix() {
    assertEquals(".5", FormatUtils.formatDecimalSuffix(0.5f));
    assertEquals(".25", FormatUtils.formatDecimalSuffix(0.25f));
    assertEquals(".75", FormatUtils.formatDecimalSuffix(0.75f));
    assertEquals(".333", FormatUtils.formatDecimalSuffix(0.33333334f));
    assertEquals(".667", FormatUtils.formatDecimalSuffix(0.6666667f));
    assertEquals(".2", FormatUtils.formatDecimalSuffix(0.2f));
    assertEquals(".4", FormatUtils.formatDecimalSuffix(0.4f));
    assertEquals(".6", FormatUtils.formatDecimalSuffix(0.6f));
    assertEquals(".8", FormatUtils.formatDecimalSuffix(0.8f));
    assertEquals(".167", FormatUtils.formatDecimalSuffix(0.16666667f));
    assertEquals(".833", FormatUtils.formatDecimalSuffix(0.8333333f));
    assertEquals(".143", FormatUtils.formatDecimalSuffix(0.14285715f));
    assertEquals(".125", FormatUtils.formatDecimalSuffix(0.125f));
    assertEquals(".375", FormatUtils.formatDecimalSuffix(0.375f));
    assertEquals(".38", FormatUtils.formatDecimalSuffix(0.38f));
    assertEquals(".625", FormatUtils.formatDecimalSuffix(0.625f));
    assertEquals(".875", FormatUtils.formatDecimalSuffix(0.875f));
    assertEquals(".111", FormatUtils.formatDecimalSuffix(0.11111111f));
    assertEquals(".1", FormatUtils.formatDecimalSuffix(0.1f));
    assertEquals("", FormatUtils.formatDecimalSuffix(0.0f));
    assertEquals("", FormatUtils.formatDecimalSuffix(1.0f));
    assertEquals(".15", FormatUtils.formatDecimalSuffix(0.15f));
    assertEquals(".39", FormatUtils.formatDecimalSuffix(0.39f));
    assertEquals(".381", FormatUtils.formatDecimalSuffix(0.381239f));
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
}
