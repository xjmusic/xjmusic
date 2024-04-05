// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.util;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.xj.hub.util.ValueUtils.MICROS_PER_MINUTE;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValueUtilsTest {
  @Test
  public void eitherOr_Double() {
    assertEquals(Double.valueOf(5.0), ValueUtils.eitherOr(5.0, null));
    assertEquals(Double.valueOf(5.0), ValueUtils.eitherOr(null, 5.0));
    assertEquals(Double.valueOf(5.0), ValueUtils.eitherOr(5.0, 7.0));
  }

  @Test
  public void eitherOr_String() {
    assertEquals("bing", ValueUtils.eitherOr("bing", null));
    assertEquals("bing", ValueUtils.eitherOr(null, "bing"));
    assertEquals("bing", ValueUtils.eitherOr("bing", "schwang"));
  }

  @Test
  public void dividedBy() {
    assertEquals(Set.of(2, 8, 23, 31, 40), ValueUtils.dividedBy(2.0, Set.of(4, 16, 62, 80, 46)));
    assertEquals(Set.of(1, 6, 18, 24, 32), ValueUtils.dividedBy(2.5, Set.of(4, 16, 62, 80, 46)));
  }

  @Test
  public void ratio() {
    assertEquals(0.0, ValueUtils.ratio(0.0, 5.0), 0.01);
    assertEquals(0.6, ValueUtils.ratio(3.0, 5.0), 0.01);
  }

  @Test
  public void isInteger() {
    assertEquals(false, ValueUtils.isInteger("a"));
    assertEquals(false, ValueUtils.isInteger("125a"));
    assertEquals(true, ValueUtils.isInteger("377"));
    assertEquals(false, ValueUtils.isInteger("237.1"));
    assertEquals(true, ValueUtils.isInteger("100000045"));
    assertEquals(false, ValueUtils.isInteger(" 97"));
    assertEquals(false, ValueUtils.isInteger(" 27773"));
    assertEquals(true, ValueUtils.isInteger("32"));
  }

  @Test
  public void limitDecimalPrecision() {
    assertEquals(1.25, ValueUtils.limitDecimalPrecision(1.2545897987), 0.0000001);
  }

  @Test
  public void formatIso8601UTC() {
    assertEquals("2014-08-12T12:17:02.527142Z",
      ValueUtils.formatIso8601UTC(Instant.parse("2014-08-12T12:17:02.527142Z")));
  }

  @Test
  public void formatRfc1123UTC() {
    assertEquals("Tue, 12 Aug 2014 12:17:02 GMT",
      ValueUtils.formatRfc1123UTC(Instant.parse("2014-08-12T12:17:02.527142Z")));
  }

  @Test
  public void without() {
    assertArrayEquals(new String[]{"A", "B", "C"}, ValueUtils.without("D", new String[]{"A", "B", "C", "D"}));
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
    }, ValueUtils.without(vD, new UUID[]{
      vA,
      vB,
      vC,
      vD
    }));
  }

  @Test
  public void isSet() {
    assertTrue(ValueUtils.isSet(true));
    assertTrue(ValueUtils.isSet(1));
    assertTrue(ValueUtils.isSet("yes"));
    assertTrue(ValueUtils.isSet(7.2));
    assertTrue(ValueUtils.isSet(0));
    assertFalse(ValueUtils.isSet(""));
    assertFalse(ValueUtils.isSet(null));
  }

  @Test
  public void isUnset() {
    assertTrue(ValueUtils.isUnset(""));
    assertTrue(ValueUtils.isUnset(null));
    assertFalse(ValueUtils.isUnset(true));
    assertFalse(ValueUtils.isUnset(1));
    assertFalse(ValueUtils.isUnset("yes"));
    assertFalse(ValueUtils.isUnset(7.2));
    assertFalse(ValueUtils.isUnset(0));
  }

  @Test
  public void uuidOrNull() {
    assertEquals(UUID.fromString("4797a799-827f-4543-9e0e-65a6cb6b382f"), ValueUtils.uuidOrNull("4797a799-827f-4543-9e0e-65a6cb6b382f"));
    assertNull(ValueUtils.uuidOrNull("4797a799-827f-4543-9e0e"));
    assertNull(ValueUtils.uuidOrNull("yodel"));
    assertNull(ValueUtils.uuidOrNull("x"));
    assertNull(ValueUtils.uuidOrNull(""));
    assertNull(ValueUtils.uuidOrNull(null));
  }

  @Test
  public void toEpochMicros() {
    assertEquals(Long.valueOf(1407845823054142L), ValueUtils.toEpochMicros(Instant.parse("2014-08-12T12:17:02.527142Z")));
  }

  @Test
  public void k() {
    assertEquals("128k", ValueUtils.k(128000));
  }

  @Test
  public void randomFrom() {
    var input = List.of("A", "B", "C");
    assertTrue(input.contains(ValueUtils.randomFrom(input)));
  }

  @Test
  public void randomFrom_multiple() {
    var input = List.of("A", "B", "C", "D", "E", "F", "G", "H", "I", "J");
    assertTrue(input.containsAll(ValueUtils.randomFrom(input, 4)));
  }

  @Test
  public void randomFrom_notEnough() {
    var input = List.of("A", "B", "C");

    var result = ValueUtils.randomFrom(input, 5);

    assertTrue(input.containsAll(result));
    assertEquals(3, input.size());
  }

  @Test
  public void randomFrom_noSource() {
    assertEquals(List.of(), ValueUtils.randomFrom(List.of(), 5));
  }

  @Test
  public void randomFrom_targetZero() {
    var input = List.of("A", "B", "C");
    assertEquals(List.of(), ValueUtils.randomFrom(input, 0));
  }

  @Test
  public void randomFrom_zeroFromNoSource() {
    assertEquals(List.of(), ValueUtils.randomFrom(List.of(), 0));
  }


  @Test
  public void gcd() {
    assertEquals(4, ValueUtils.gcd(4, 12));
    assertEquals(3, ValueUtils.gcd(9, 12));
  }

  @Test
  public void factors() {
    assertArrayEquals(new int[]{2, 3, 4}, ValueUtils.factors(12, new int[]{2, 3, 4, 5, 7}));
    assertArrayEquals(new int[]{2, 3, 4, 5}, ValueUtils.factors(60, new int[]{2, 3, 4, 5, 7}));
    assertArrayEquals(new int[]{2, 3, 5, 7}, ValueUtils.factors(210, new int[]{2, 3, 4, 5, 7}));
  }

  @Test
  public void div() {
    assertEquals(2, ValueUtils.subDiv(2, 3));
    assertEquals(4, ValueUtils.subDiv(4, 3));
    assertEquals(4, ValueUtils.subDiv(12, 3));
    assertEquals(3, ValueUtils.subDiv(12, 4));
    assertEquals(4, ValueUtils.subDiv(16, 4));
    assertEquals(4, ValueUtils.subDiv(24, 3));
    assertEquals(3, ValueUtils.subDiv(24, 4));
    assertEquals(4, ValueUtils.subDiv(48, 3));
    assertEquals(3, ValueUtils.subDiv(48, 4));
    assertEquals(4, ValueUtils.subDiv(64, 4));
  }

  @Test
  public void multipleFloor() {
    assertEquals(0, ValueUtils.multipleFloor(12, 11));
    assertEquals(12, ValueUtils.multipleFloor(12, 20));
    assertEquals(36, ValueUtils.multipleFloor(12, 38));
    assertEquals(0, ValueUtils.multipleFloor(16, 11));
    assertEquals(16, ValueUtils.multipleFloor(16, 20));
    assertEquals(32, ValueUtils.multipleFloor(16, 34));
  }

  @Test
  public void interpolate() {
    assertEquals(10, ValueUtils.interpolate(10, 20, 0, 1.0), 0.000001);
    assertEquals(20, ValueUtils.interpolate(10, 20, 1, 1.0), 0.000001);
    assertEquals(15, ValueUtils.interpolate(10, 20, 1, 0.5), 0.000001);
  }

  @Test
  public void enforceMaxStereo() {
    assertThrows(ValueException.class, () -> ValueUtils.enforceMaxStereo(3));
  }

  @Test
  public void withIdsRemoved() {
    assertEquals(2, ValueUtils.withIdsRemoved(List.of(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()), 2).size());
  }

  @Test
  public void emptyZero() {
    assertEquals("12", ValueUtils.emptyZero(12));
    assertEquals("-12", ValueUtils.emptyZero(-12));
    assertEquals("", ValueUtils.emptyZero(0));
  }

  @Test
  public void last() {
    var input = List.of("One", "Two", "Three");

    assertEquals(List.of(), ValueUtils.last(-1, input));
    assertEquals(List.of(), ValueUtils.last(0, input));
    assertEquals(List.of("Three"), ValueUtils.last(1, input));
    assertEquals(List.of("Two", "Three"), ValueUtils.last(2, input));
    assertEquals(List.of("One", "Two", "Three"), ValueUtils.last(3, input));
    assertEquals(List.of("One", "Two", "Three"), ValueUtils.last(4, input));
  }

  @Test
  public void constants() {
    assertEquals(24 * 60 * 60, ValueUtils.SECONDS_PER_DAY);
  }

  @Test
  public void requireMinimum() throws ValueException {
    ValueUtils.requireMinimum(2.0, 3.0, "Test"); // OK
    assertThrows(ValueException.class, () -> ValueUtils.requireMinimum(3.0, 2.0, "Test"));
    ValueUtils.requireMinimum(2L, 3L, "Test"); // OK
    assertThrows(ValueException.class, () -> ValueUtils.requireMinimum(3L, 2L, "Test"));
  }

  @Test
  public void getKeyOfHighestValue() {
    var uuidA = UUID.randomUUID();
    var uuidB = UUID.randomUUID();
    var uuidC = UUID.randomUUID();
    assertEquals(uuidB, ValueUtils.getKeyOfHighestValue(Map.of(uuidA, 1, uuidB, 4, uuidC, 3)).orElseThrow());
  }

  @Test
  public void roundToNearest() {
    assertEquals(255, ValueUtils.roundToNearest(5, 254));
    assertEquals(250, ValueUtils.roundToNearest(10, 254));
    assertEquals(260, ValueUtils.roundToNearest(10, 256));
    assertEquals(300, ValueUtils.roundToNearest(100, 254));
  }

  @Test
  public void microsPerMinutes() {
    assertEquals(60000000, MICROS_PER_MINUTE);
  }
}
