// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.manager;

import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.nexus.NexusException;
import io.xj.nexus.model.Chain;
import io.xj.nexus.model.ChainState;
import io.xj.nexus.model.ChainType;
import io.xj.nexus.model.Segment;
import io.xj.nexus.model.SegmentChoice;
import io.xj.nexus.model.SegmentState;
import io.xj.nexus.model.SegmentType;
import io.xj.nexus.persistence.Segments;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.List;

import static io.xj.test_fixtures.HubIntegrationTestingFixtures.buildAccount;
import static io.xj.test_fixtures.HubIntegrationTestingFixtures.buildTemplate;
import static io.xj.lib.util.ValueUtils.MICROS_PER_SECOND;
import static io.xj.test_fixtures.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.test_fixtures.NexusIntegrationTestingFixtures.buildSegment;

public class SegmentsTest extends TestCase {
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

  public void setUp() throws Exception {
    super.setUp();
  }

  public void testFindFirstOfType() throws NexusException {
    var ch0 = new SegmentChoice();
    ch0.setDeltaIn(Segments.DELTA_UNLIMITED);
    ch0.setDeltaOut(Segments.DELTA_UNLIMITED);
    ch0.setProgramType(ProgramType.Main);
    var ch1 = new SegmentChoice();
    ch1.setDeltaIn(Segments.DELTA_UNLIMITED);
    ch1.setDeltaOut(Segments.DELTA_UNLIMITED);
    ch1.setProgramType(ProgramType.Macro);
    assertEquals(ch0, Segments.findFirstOfType(List.of(ch0, ch1), ProgramType.Main));
  }

  public void testGetIdentifier() {
    assertEquals("chains-1-segments-9f7s89d8a7892", Segments.getIdentifier(seg0));
  }

  public void testGetLastDubbed() {
    assertEquals(seg2, Segments.getLastCrafted(segments).orElseThrow());
  }

  public void testGetLast() {
    assertEquals(seg3, Segments.getLast(segments).orElseThrow());
  }

  public void testGetDubbed() {
    assertEquals(List.of(seg0, seg1, seg2),
      Segments.getCrafted(segments));
  }

  public void testGetShipKey() {
    assertEquals("chains-1-segments-078aw34tiu5hga.wav", Segments.getStorageFilename(seg1));
  }

  public void testIsSpanning() {
    assertTrue(Segments.isSpanning(seg1, 32 * MICROS_PER_SECOND, 32 * MICROS_PER_SECOND)); // true if exactly at beginning of segment
    assertFalse(Segments.isSpanning(seg1, 64 * MICROS_PER_SECOND, 64 * MICROS_PER_SECOND)); // false if exactly at end of segment
    assertFalse(Segments.isSpanning(seg1, 15 * MICROS_PER_SECOND, 30 * MICROS_PER_SECOND));
    assertTrue(Segments.isSpanning(seg1, 20 * MICROS_PER_SECOND, 36 * MICROS_PER_SECOND));
    assertTrue(Segments.isSpanning(seg1, 35 * MICROS_PER_SECOND, 52 * MICROS_PER_SECOND));
    assertTrue(Segments.isSpanning(seg1, 50 * MICROS_PER_SECOND, 67 * MICROS_PER_SECOND));
    assertFalse(Segments.isSpanning(seg1, 66 * MICROS_PER_SECOND, 80 * MICROS_PER_SECOND));
  }
}
