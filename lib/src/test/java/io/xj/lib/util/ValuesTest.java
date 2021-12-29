// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.util;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class ValuesTest {
  @Test
  public void eitherOr_Double() {
    assertEquals(Double.valueOf(5.0), Values.eitherOr(5.0, null));
    assertEquals(Double.valueOf(5.0), Values.eitherOr(null, 5.0));
    assertEquals(Double.valueOf(5.0), Values.eitherOr(5.0, 7.0));
  }

  @Test
  public void eitherOr_String() {
    assertEquals("bing", Values.eitherOr("bing", null));
    assertEquals("bing", Values.eitherOr(null, "bing"));
    assertEquals("bing", Values.eitherOr("bing", "schwang"));
  }

  @Test
  public void dividedBy() {
    assertEquals(ImmutableSet.of(2, 8, 23, 31, 40), Values.dividedBy(2.0, ImmutableSet.of(4, 16, 62, 80, 46)));
    assertEquals(ImmutableSet.of(1, 6, 18, 24, 32), Values.dividedBy(2.5, ImmutableSet.of(4, 16, 62, 80, 46)));
  }

  @Test
  public void ratio() {
    assertEquals(0.0, Values.ratio(0.0, 5.0), 0.01);
    assertEquals(0.6, Values.ratio(3.0, 5.0), 0.01);
  }

  @Test
  public void isInteger() {
    assertEquals(false, Values.isInteger("a"));
    assertEquals(false, Values.isInteger("125a"));
    assertEquals(true, Values.isInteger("377"));
    assertEquals(false, Values.isInteger("237.1"));
    assertEquals(true, Values.isInteger("100000045"));
    assertEquals(false, Values.isInteger(" 97"));
    assertEquals(false, Values.isInteger(" 27773"));
    assertEquals(true, Values.isInteger("32"));
  }

  @Test
  public void limitDecimalPrecision() {
    assertEquals(1.25, Values.limitDecimalPrecision(1.2545897987), 0.0000001);
  }

  @Test
  public void formatIso8601UTC() {
    assertEquals("2014-08-12T12:17:02.527142Z",
      Values.formatIso8601UTC(Instant.parse("2014-08-12T12:17:02.527142Z")));
  }

  @Test
  public void without() {
    assertArrayEquals(new String[]{"A", "B", "C"}, Values.without("D", new String[]{"A", "B", "C", "D"}));
  }

  @Test
  public void without_fromObjects() {
    UUID vA = UUID.randomUUID();
    UUID vB = UUID.randomUUID();
    UUID vC = UUID.randomUUID();
    UUID vD = UUID.randomUUID();
    assertArrayEquals(new UUID[]{
      vA,
      vB,
      vC
    }, Values.without(vD, new UUID[]{
      vA,
      vB,
      vC,
      vD
    }));
  }

  @Test
  public void isSet() {
    assertTrue(Values.isSet(true));
    assertTrue(Values.isSet(1));
    assertTrue(Values.isSet("yes"));
    assertTrue(Values.isSet(7.2));
    assertTrue(Values.isSet(0));
    assertFalse(Values.isSet(""));
    assertFalse(Values.isSet(null));
  }

  @Test
  public void isUnset() {
    assertTrue(Values.isUnset(""));
    assertTrue(Values.isUnset(null));
    assertFalse(Values.isUnset(true));
    assertFalse(Values.isUnset(1));
    assertFalse(Values.isUnset("yes"));
    assertFalse(Values.isUnset(7.2));
    assertFalse(Values.isUnset(0));
  }

  @Test
  public void stringOrEmpty() {
    assertEquals("", Values.stringOrEmpty(null));
    assertEquals("4797a799-827f-4543-9e0e-65a6cb6b382f",
      Values.stringOrEmpty(UUID.fromString("4797a799-827f-4543-9e0e-65a6cb6b382f")));
  }

  @Test
  public void uuidOrNull() {
    assertEquals(UUID.fromString("4797a799-827f-4543-9e0e-65a6cb6b382f"), Values.uuidOrNull("4797a799-827f-4543-9e0e-65a6cb6b382f"));
    assertNull(Values.uuidOrNull("4797a799-827f-4543-9e0e"));
    assertNull(Values.uuidOrNull("yodel"));
    assertNull(Values.uuidOrNull("x"));
    assertNull(Values.uuidOrNull(""));
    assertNull(Values.uuidOrNull(null));
  }

  @Test
  public void toEpochMicros() {
    assertEquals(1407845823054142L, Values.toEpochMicros(Instant.parse("2014-08-12T12:17:02.527142Z")));
  }

  @Test
  public void k() {
    assertEquals("128k", Values.k(128000));
  }

  @Test
  public void randomFrom() {
    var input = List.of("A", "B", "C");
    assertTrue(input.contains(Values.randomFrom(input)));
  }

  @Test
  public void randomFrom_multiple() {
    var input = List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J");
    assertTrue(input.containsAll(Values.randomFrom(input, 4)));
  }

  @Test
  public void randomFrom_notEnough() {
    var input = List.of("A", "B", "C");

    var result = Values.randomFrom(input, 5);

    assertTrue(input.containsAll(result));
    assertEquals(3, input.size());
  }

  @Test
  public void randomFrom_noSource() {
    assertEquals(List.of(), Values.randomFrom(List.of(), 5));
  }

  @Test
  public void randomFrom_targetZero() {
    var input = List.of("A", "B", "C");
    assertEquals(List.of(), Values.randomFrom(input, 0));
  }

  @Test
  public void randomFrom_zeroFromNoSource() {
    assertEquals(List.of(), Values.randomFrom(List.of(), 0));
  }


  @Test
  public void gcd() {
    assertEquals(4, Values.gcd(4, 12));
    assertEquals(3, Values.gcd(9, 12));
  }

  @Test
  public void factors() {
    assertArrayEquals(new int[]{2, 3, 4}, Values.factors(12, new int[]{2, 3, 4, 5, 7}));
    assertArrayEquals(new int[]{2, 3, 4, 5}, Values.factors(60, new int[]{2, 3, 4, 5, 7}));
    assertArrayEquals(new int[]{2, 3, 5, 7}, Values.factors(210, new int[]{2, 3, 4, 5, 7}));
  }

  @Test
  public void div() {
    assertEquals(2, Values.subDiv(2, 3));
    assertEquals(4, Values.subDiv(4, 3));
    assertEquals(4, Values.subDiv(12, 3));
    assertEquals(3, Values.subDiv(12, 4));
    assertEquals(4, Values.subDiv(16, 4));
    assertEquals(4, Values.subDiv(24, 3));
    assertEquals(3, Values.subDiv(24, 4));
    assertEquals(4, Values.subDiv(48, 3));
    assertEquals(3, Values.subDiv(48, 4));
    assertEquals(4, Values.subDiv(64, 4));
  }

  @Test
  public void multipleFloor() {
    assertEquals(0, Values.multipleFloor(12, 11));
    assertEquals(12, Values.multipleFloor(12, 20));
    assertEquals(36, Values.multipleFloor(12, 38));
    assertEquals(0, Values.multipleFloor(16, 11));
    assertEquals(16, Values.multipleFloor(16, 20));
    assertEquals(32, Values.multipleFloor(16, 34));
  }

  @Test
  public void interpolate() {
    assertEquals(10, Values.interpolate(10, 20, 0, 1.0), 0.000001);
    assertEquals(20, Values.interpolate(10, 20, 1, 1.0), 0.000001);
    assertEquals(15, Values.interpolate(10, 20, 1, 0.5), 0.000001);
  }

  @Test
  public void enforceMaxStereo() {
    assertThrows(ValueException.class, () -> Values.enforceMaxStereo(3));
  }
}
