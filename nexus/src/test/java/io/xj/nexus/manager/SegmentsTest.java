// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus.manager;

import com.google.common.collect.ImmutableList;
import io.xj.nexus.model.*;
import io.xj.hub.enums.ProgramType;
import io.xj.hub.tables.pojos.Account;
import io.xj.hub.tables.pojos.Template;
import io.xj.nexus.NexusException;
import io.xj.nexus.persistence.Segments;
import junit.framework.TestCase;

import java.time.Instant;
import java.util.Collection;

import static io.xj.hub.IntegrationTestingFixtures.buildAccount;
import static io.xj.hub.IntegrationTestingFixtures.buildTemplate;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.nexus.NexusIntegrationTestingFixtures.buildSegment;

public class SegmentsTest extends TestCase {
  private final Account account = buildAccount("Test");
  private final Template template = buildTemplate(account, "Test");
  private final Chain chain = buildChain(account, "Test", ChainType.PRODUCTION, ChainState.FABRICATE, template, Instant.parse("2017-02-14T12:02:04.000001Z"));
  private final Segment seg0 = buildSegment(
    chain,
    SegmentType.INITIAL,
    0,
    0,
    SegmentState.DUBBED,
    Instant.parse("2017-02-14T12:02:04.000001Z"),
    Instant.parse("2017-02-14T12:02:36.000001Z"),
    "F Major",
    64,
    0.30f,
    120.0f,
    "chains-1-segments-9f7s89d8a7892",
    "wav");
  private final Segment seg1 = buildSegment(
    chain,
    SegmentType.CONTINUE,
    1,
    1,
    SegmentState.DUBBED,
    Instant.parse("2017-02-14T12:02:36.000001Z"),
    Instant.parse("2017-02-14T12:03:08.000001Z"),
    "F Major",
    64,
    0.30f,
    120.0f,
    "chains-1-segments-078aw34tiu5hga",
    "wav");
  private final Segment seg2 = buildSegment(
    chain,
    SegmentType.NEXTMAIN,
    2,
    0,
    SegmentState.DUBBED,
    Instant.parse("2017-02-14T12:03:08.000001Z"),
    Instant.parse("2017-02-14T12:03:40.000001Z"),
    "F Major",
    64,
    0.30f,
    120.0f,
    "chains-1-segments-jhz5sd4fgi786q",
    "wav");
  private final Segment seg3 = buildSegment(
    chain,
    SegmentType.NEXTMAIN,
    3,
    0,
    SegmentState.CRAFTING,
    Instant.parse("2017-02-14T12:03:40.000001Z"),
    Instant.parse("2017-02-14T12:04:12.000001Z"),
    "F Major",
    64,
    0.30f,
    120.0f,
    "chains-1-segments-j1hsk3dgu2yu2gyy",
    "wav");
  Collection<Segment> segments = ImmutableList.of(seg0, seg1, seg2, seg3);

  public void setUp() throws Exception {
    super.setUp();
  }

  public void testFindFirstOfType() throws NexusException {
    var ch0 = new SegmentChoice();
    ch0.setDeltaIn(Segments.DELTA_UNLIMITED);
    ch0.setDeltaOut(Segments.DELTA_UNLIMITED);
    ch0.setProgramType(ProgramType.Main.toString());
    var ch1 = new SegmentChoice();
    ch1.setDeltaIn(Segments.DELTA_UNLIMITED);
    ch1.setDeltaOut(Segments.DELTA_UNLIMITED);
    ch1.setProgramType(ProgramType.Macro.toString());
    assertEquals(ch0, Segments.findFirstOfType(ImmutableList.of(ch0, ch1), ProgramType.Main));
  }

  public void testGetIdentifier() {
    assertEquals("chains-1-segments-9f7s89d8a7892", Segments.getIdentifier(seg0));
  }

  public void testGetLastDubbed() {
    assertEquals(seg2, Segments.getLastDubbed(segments).orElseThrow());
  }

  public void testGetLast() {
    assertEquals(seg3, Segments.getLast(segments).orElseThrow());
  }

  public void testGetDubbed() {
    assertEquals(ImmutableList.of(seg0, seg1, seg2),
      Segments.getDubbed(segments));
  }

  public void testGetLengthSeconds() {
    assertEquals(32.0, Segments.getLengthSeconds(seg0), 0.1);
  }

  public void testGetShipKey() {
    assertEquals("chains-1-segments-078aw34tiu5hga.wav", Segments.getStorageFilename(seg1));
  }
}
