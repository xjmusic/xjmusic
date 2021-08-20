// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.util;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.Assert.*;

public class ValueTest {
  @Test
  public void eitherOr_Double() {
    assertEquals(Double.valueOf(5.0), Value.eitherOr(5.0, null));
    assertEquals(Double.valueOf(5.0), Value.eitherOr(null, 5.0));
    assertEquals(Double.valueOf(5.0), Value.eitherOr(5.0, 7.0));
  }

  @Test
  public void eitherOr_String() {
    assertEquals("bing", Value.eitherOr("bing", null));
    assertEquals("bing", Value.eitherOr(null, "bing"));
    assertEquals("bing", Value.eitherOr("bing", "schwang"));
  }

  @Test
  public void dividedBy() {
    assertEquals(ImmutableSet.of(2, 8, 23, 31, 40), Value.dividedBy(2.0, ImmutableSet.of(4, 16, 62, 80, 46)));
    assertEquals(ImmutableSet.of(1, 6, 18, 24, 32), Value.dividedBy(2.5, ImmutableSet.of(4, 16, 62, 80, 46)));
  }

  @Test
  public void ratio() {
    assertEquals(0.0, Value.ratio(0.0, 5.0), 0.01);
    assertEquals(0.6, Value.ratio(3.0, 5.0), 0.01);
  }

  @Test
  public void isInteger() {
    assertEquals(false, Value.isInteger("a"));
    assertEquals(false, Value.isInteger("125a"));
    assertEquals(true, Value.isInteger("377"));
    assertEquals(false, Value.isInteger("237.1"));
    assertEquals(true, Value.isInteger("100000045"));
    assertEquals(false, Value.isInteger(" 97"));
    assertEquals(false, Value.isInteger(" 27773"));
    assertEquals(true, Value.isInteger("32"));
  }

  @Test
  public void limitDecimalPrecision() {
    assertEquals(1.25, Value.limitDecimalPrecision(1.2545897987), 0.0000001);
  }

  @Test
  public void formatIso8601UTC() {
    assertEquals("2014-08-12T12:17:02.527142Z",
      Value.formatIso8601UTC(Instant.parse("2014-08-12T12:17:02.527142Z")));
  }

  @Test
  public void without() {
    assertArrayEquals(new String[]{"A", "B", "C"}, Value.without("D", new String[]{"A", "B", "C", "D"}));
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
    }, Value.without(vD, new UUID[]{
      vA,
      vB,
      vC,
      vD
    }));
  }

  @Test
  public void isSet() {
    assertTrue(Value.isSet(true));
    assertTrue(Value.isSet(1));
    assertTrue(Value.isSet("yes"));
    assertTrue(Value.isSet(7.2));
    assertTrue(Value.isSet(0));
    assertFalse(Value.isSet(""));
    assertFalse(Value.isSet(null));
  }

  @Test
  public void isUnset() {
    assertTrue(Value.isUnset(""));
    assertTrue(Value.isUnset(null));
    assertFalse(Value.isUnset(true));
    assertFalse(Value.isUnset(1));
    assertFalse(Value.isUnset("yes"));
    assertFalse(Value.isUnset(7.2));
    assertFalse(Value.isUnset(0));
  }

  @Test
  public void stringOrEmpty() {
    assertEquals("", Value.stringOrEmpty(null));
    assertEquals("4797a799-827f-4543-9e0e-65a6cb6b382f",
      Value.stringOrEmpty(UUID.fromString("4797a799-827f-4543-9e0e-65a6cb6b382f")));
  }

}
