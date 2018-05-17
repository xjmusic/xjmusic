// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.segment;

import io.xj.core.exception.BusinessException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.sql.Timestamp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SegmentTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Segment()
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAt("2014-08-12 12:17:02.527142")
      .setEndAt("2014-09-12 12:17:34.262679")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473L))
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_setAsTimestamps() throws Exception {
    new Segment()
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAtTimestamp(Timestamp.valueOf("2014-08-12 12:17:02.527142"))
      .setEndAtTimestamp(Timestamp.valueOf("2014-09-12 12:17:34.262679"))
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473L))
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_withMinimalAttributes() throws Exception {
    new Segment()
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAt("2014-08-12 12:17:02.527142")
      .setOffset(BigInteger.valueOf(473L))
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_failsWithoutChainID() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Chain ID is required");

    new Segment()
      .setBeginAt("2014-08-12 12:17:02.527142")
      .setEndAt("2014-09-12 12:17:34.262679")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473L))
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_failsWithoutBeginAt() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Begin-at is required");

    new Segment()
      .setChainId(BigInteger.valueOf(180923L))
      .setEndAt("2014-09-12 12:17:34.262679")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473L))
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_failsWithoutOffset() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Offset is required");

    new Segment()
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAt("2014-08-12 12:17:02.527142")
      .setEndAt("2014-09-12 12:17:34.262679")
      .setTotal(64)
      .setDensity(0.754)
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_failsWithoutState() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("State is required");

    Segment segment = new Segment()
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAt("2014-08-12 12:17:02.527142")
      .setEndAt("2014-09-12 12:17:34.262679")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473L))
      .setKey("G minor")
      .setTempo(121.0);
    segment.validate();
  }

  @Test
  public void validate_failsWithInvalidState() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("'pensive' is not a valid state");

    new Segment()
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAt("2014-08-12 12:17:02.527142")
      .setEndAt("2014-09-12 12:17:34.262679")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473L))
      .setKey("G minor")
      .setTempo(121.0)
      .setState("pensive")
      .validate();
  }

  @Test
  public void validate_okWithSetEnumState() throws Exception {
    Segment segment = new Segment()
      .setChainId(BigInteger.valueOf(180923L))
      .setBeginAt("2014-08-12 12:17:02.527142")
      .setEndAt("2014-09-12 12:17:34.262679")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(BigInteger.valueOf(473L))
      .setKey("G minor")
      .setTempo(121.0);

    segment.setStateEnum(SegmentState.Crafting);
    segment.validate();
  }

  @Test
  public void isInitial() throws Exception {
    assertTrue(new Segment()
      .setOffset(BigInteger.valueOf(0L))
      .isInitial());
    assertFalse(new Segment()
      .setOffset(BigInteger.valueOf(1L))
      .isInitial());
    assertFalse(new Segment()
      .setOffset(BigInteger.valueOf(981L))
      .isInitial());
  }

  @Test
  public void getPreviousOffset() throws Exception {
    assertEquals(BigInteger.valueOf(0L),
      new Segment().setOffset(BigInteger.valueOf(1L)).getPreviousOffset());
    assertEquals(BigInteger.valueOf(234L),
      new Segment().setOffset(BigInteger.valueOf(235L)).getPreviousOffset());
  }

  @Test
  public void getPreviousOffset_throwsExceptionForInitialSegment() throws Exception {
    failure.expect(BusinessException.class);
    failure.expectMessage("Cannot get previous id of initial Segment");

    new Segment().setOffset(BigInteger.valueOf(0L)).getPreviousOffset();
  }

}
