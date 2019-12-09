// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 [#166132897] Segment model handles all of its own entities
 [#166273140] Segment Child Entities are identified and related by UUID (not id)
 */
public class SegmentTest  {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  Segment segment;

  @Before
  public void setUp() throws Exception {
    segment = new Segment().setId(UUID.randomUUID());
  }

  @Test
  public void validate() throws Exception {
    segment
      .setChainId(UUID.randomUUID())
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(473L)
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_setAsTimestamps() throws Exception {
    segment
      .setChainId(UUID.randomUUID())
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(473L)
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_withNoEndAtTime_isOkay_becausePlannedSegments() throws Exception {
    segment
      .setChainId(UUID.randomUUID())
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setOffset(473L)
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_failsWithoutChainID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Chain ID is required");

    segment
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(473L)
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_failsWithoutBeginAt() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Begin-at is required");

    segment
      .setChainId(UUID.randomUUID())
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(473L)
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_failsWithoutOffset() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Offset is required");

    segment
      .setChainId(UUID.randomUUID())
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setKey("G minor")
      .setTempo(121.0)
      .setState("Crafted")
      .validate();
  }

  @Test
  public void validate_failsWithoutState() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("State is required");

    segment
      .setChainId(UUID.randomUUID())
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(473L)
      .setKey("G minor")
      .setTempo(121.0)
      .validate();
  }

  @Test
  public void validate_failsWithInvalidState() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("'pensive' is not a valid state");

    segment
      .setChainId(UUID.randomUUID())
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(473L)
      .setKey("G minor")
      .setTempo(121.0)
      .setState("pensive")
      .validate();
  }

  @Test
  public void validate_okWithSetEnumState() throws Exception {
    segment
      .setChainId(UUID.randomUUID())
      .setBeginAt("2014-08-12T12:17:02.527142Z")
      .setEndAt("2014-09-12T12:17:34.262679Z")
      .setTotal(64)
      .setDensity(0.754)
      .setOffset(473L)
      .setKey("G minor")
      .setTempo(121.0);

    segment.setStateEnum(SegmentState.Crafting);
    segment.validate();
  }

  @Test
  public void isInitial_trueAtOffsetZero() {
    assertTrue(segment
      .setOffset(0L)
      .isInitial());
  }

  @Test
  public void isInitial_falseAtOffsetOne() {
    assertFalse(segment
      .setOffset(1L)
      .isInitial());
  }

  @Test
  public void isInitial_falseAtOffsetHigh() {
    assertFalse(segment
      .setOffset(901L)
      .isInitial());
  }

  @Test
  public void getPreviousOffset() throws Exception {
    assertEquals(Long.valueOf(234L),
      segment.setOffset(235L).getPreviousOffset());
  }

  @Test
  public void getPreviousOffset_throwsExceptionForInitialSegment() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Cannot get previous id create initial Segment");

    segment.setOffset(0L).getPreviousOffset();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("state", "beginAt", "endAt", "key", "total", "offset", "density", "tempo", "waveformKey", "type"), segment.getResourceAttributeNames());
  }

}
