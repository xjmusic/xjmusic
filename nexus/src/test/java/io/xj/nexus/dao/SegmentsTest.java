package io.xj.nexus.dao;

import com.google.common.collect.ImmutableList;
import io.xj.api.ProgramType;
import io.xj.api.Segment;
import io.xj.api.SegmentChoice;
import io.xj.api.SegmentState;
import io.xj.api.SegmentType;
import io.xj.nexus.dao.exception.DAOExistenceException;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.UUID;

public class SegmentsTest extends TestCase {
  private final UUID chainId = UUID.randomUUID();
  private final Segment seg0 = new Segment()
    .id(UUID.randomUUID())
    .chainId(chainId)
    .type(SegmentType.INITIAL)
    .offset(0L)
    .delta(0)
    .state(SegmentState.DUBBED)
    .beginAt("2017-02-14T12:02:04.000001Z")
    .endAt("2017-02-14T12:02:36.000001Z")
    .key("F Major")
    .total(64)
    .density(0.30)
    .tempo(120.0)
    .storageKey("chains-1-segments-9f7s89d8a7892")
    .outputEncoder("wav");
  private final Segment seg1 = new Segment()
    .id(UUID.randomUUID())
    .chainId(chainId)
    .type(SegmentType.CONTINUE)
    .offset(1L)
    .delta(1)
    .state(SegmentState.DUBBED)
    .beginAt("2017-02-14T12:02:36.000001Z")
    .endAt("2017-02-14T12:03:08.000001Z")
    .key("F Major")
    .total(64)
    .density(0.30)
    .tempo(120.0)
    .storageKey("chains-1-segments-078aw34tiu5hga")
    .outputEncoder("wav");
  private final Segment seg2 = new Segment()
    .id(UUID.randomUUID())
    .chainId(chainId)
    .type(SegmentType.NEXTMAIN)
    .offset(2L)
    .delta(0)
    .state(SegmentState.DUBBED)
    .beginAt("2017-02-14T12:03:08.000001Z")
    .endAt("2017-02-14T12:03:40.000001Z")
    .key("F Major")
    .total(64)
    .density(0.30)
    .tempo(120.0)
    .storageKey("chains-1-segments-jhz5sd4fgi786q")
    .outputEncoder("wav");
  private final Segment seg3 = new Segment()
    .id(UUID.randomUUID())
    .chainId(chainId)
    .type(SegmentType.NEXTMAIN)
    .offset(3L)
    .delta(0)
    .state(SegmentState.CRAFTING)
    .beginAt("2017-02-14T12:03:40.000001Z")
    .endAt("2017-02-14T12:04:12.000001Z")
    .key("F Major")
    .total(64)
    .density(0.30)
    .tempo(120.0)
    .storageKey("chains-1-segments-j1hsk3dgu2yu2gyy")
    .outputEncoder("wav");
  Collection<Segment> segments = ImmutableList.of(seg0, seg1, seg2, seg3);

  public void setUp() throws Exception {
    super.setUp();
  }

  public void testFindFirstOfType() throws DAOExistenceException {
    var ch0 = new SegmentChoice()
      .deltaIn(Segments.DELTA_UNLIMITED)
      .deltaOut(Segments.DELTA_UNLIMITED)
      .programType(ProgramType.MAIN);
    var ch1 = new SegmentChoice()
      .deltaIn(Segments.DELTA_UNLIMITED)
      .deltaOut(Segments.DELTA_UNLIMITED)
      .programType(ProgramType.MACRO);
    assertEquals(ch0, Segments.findFirstOfType(ImmutableList.of(ch0, ch1), ProgramType.MAIN));
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
}
