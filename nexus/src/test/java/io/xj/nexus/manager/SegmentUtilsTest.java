// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.nexus.manager;

import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.*;
import io.xj.nexus.persistence.SegmentUtils;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;
import static io.xj.nexus.HubIntegrationTestingFixtures.buildAccount;
import static io.xj.nexus.HubIntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;
import static org.junit.jupiter.api.Assertions.*;

public class SegmentUtilsTest {
  final Account account = buildAccount("Test");
  final Template template = buildTemplate(account, "Test");
  final Chain chain = buildChain(account, "Test", ChainType.PRODUCTION, ChainState.FABRICATE, template);
  final Segment seg0 = buildSegment(
    chain,
    SegmentType.INITIAL,
    0,
    0,
    SegmentState.CRAFTED,
    "F Major",
    64,
    0.30f,
    120.0f,
    "chains-1-segments-9f7s89d8a7892",
    true);
  final Segment seg1 = buildSegment(
    chain,
    SegmentType.CONTINUE,
    1,
    1,
    SegmentState.CRAFTED,
    "F Major",
    64,
    0.30f,
    120.0f,
    "chains-1-segments-078aw34tiu5hga",
    true);
  final Segment seg2 = buildSegment(
    chain,
    SegmentType.NEXTMAIN,
    2,
    0,
    SegmentState.CRAFTED,
    "F Major",
    64,
    0.30f,
    120.0f,
    "chains-1-segments-jhz5sd4fgi786q",
    true);
  final Segment seg3 = buildSegment(
    chain,
    SegmentType.NEXTMAIN,
    3,
    0,
    SegmentState.CRAFTING,
    "F Major",
    64,
    0.30f,
    120.0f,
    "chains-1-segments-j1hsk3dgu2yu2gyy",
    true);
  Collection<Segment> segments = List.of(seg0, seg1, seg2, seg3);

  @Test
  public void testFindFirstOfType() throws NexusException {
    var ch0 = new SegmentChoice();
    ch0.setDeltaIn(Segment.DELTA_UNLIMITED);
    ch0.setDeltaOut(Segment.DELTA_UNLIMITED);
    ch0.setProgramType(ProgramType.Main);
    var ch1 = new SegmentChoice();
    ch1.setDeltaIn(Segment.DELTA_UNLIMITED);
    ch1.setDeltaOut(Segment.DELTA_UNLIMITED);
    ch1.setProgramType(ProgramType.Macro);
    assertEquals(ch0, SegmentUtils.findFirstOfType(List.of(ch0, ch1), ProgramType.Main));
  }

  @Test
  public void testGetIdentifier() {
    assertEquals("chains-1-segments-9f7s89d8a7892", SegmentUtils.getIdentifier(seg0));
  }

  @Test
  public void testGetLastDubbed() {
    assertEquals(seg2, SegmentUtils.getLastCrafted(segments).orElseThrow());
  }

  @Test
  public void testGetLast() {
    assertEquals(seg3, SegmentUtils.getLast(segments).orElseThrow());
  }

  @Test
  public void testGetDubbed() {
    assertEquals(List.of(seg0, seg1, seg2),
      SegmentUtils.getCrafted(segments));
  }

  @Test
  public void testGetShipKey() {
    assertEquals("chains-1-segments-078aw34tiu5hga.wav", SegmentUtils.getStorageFilename(seg1));
  }

  @Test
  public void testIsSpanning() {
    assertTrue(SegmentUtils.isSpanning(seg1, 32 * MICROS_PER_SECOND, 32 * MICROS_PER_SECOND)); // true if exactly at beginning of segment
    assertFalse(SegmentUtils.isSpanning(seg1, 64 * MICROS_PER_SECOND, 64 * MICROS_PER_SECOND)); // false if exactly at end of segment
    assertFalse(SegmentUtils.isSpanning(seg1, 15 * MICROS_PER_SECOND, 30 * MICROS_PER_SECOND));
    assertTrue(SegmentUtils.isSpanning(seg1, 20 * MICROS_PER_SECOND, 36 * MICROS_PER_SECOND));
    assertTrue(SegmentUtils.isSpanning(seg1, 35 * MICROS_PER_SECOND, 52 * MICROS_PER_SECOND));
    assertTrue(SegmentUtils.isSpanning(seg1, 50 * MICROS_PER_SECOND, 67 * MICROS_PER_SECOND));
    assertFalse(SegmentUtils.isSpanning(seg1, 66 * MICROS_PER_SECOND, 80 * MICROS_PER_SECOND));
  }

  @Test
  public void testIsIntersecting() {
    assertTrue(SegmentUtils.isIntersecting(seg1, 32 * MICROS_PER_SECOND)); // true if exactly at beginning of segment
    assertFalse(SegmentUtils.isIntersecting(seg1, 64 * MICROS_PER_SECOND)); // false if exactly at end of segment
    assertFalse(SegmentUtils.isIntersecting(seg1, 15 * MICROS_PER_SECOND));
    assertTrue(SegmentUtils.isIntersecting(seg1, 20 * MICROS_PER_SECOND));
    assertTrue(SegmentUtils.isIntersecting(seg1, 35 * MICROS_PER_SECOND));
    assertTrue(SegmentUtils.isIntersecting(seg1, 50 * MICROS_PER_SECOND));
    assertFalse(SegmentUtils.isIntersecting(seg1, 66 * MICROS_PER_SECOND));
  }

  Segment createSameSegment(String updatedAt, SegmentState state) {
    final Segment s = buildSegment(
      chain,
      SegmentType.CONTINUE,
      1,
      1,
      state,
      "F Major",
      64,
      0.30f,
      120.0f,
      "chains-1-segments-078aw34tiu5hga",
      true);
    return s.setCreatedAt(updatedAt).setUpdatedAt(updatedAt);
  }
}
