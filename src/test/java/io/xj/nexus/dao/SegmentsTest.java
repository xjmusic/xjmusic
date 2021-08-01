package io.xj.nexus.dao;

import com.google.common.collect.ImmutableList;
import io.xj.Program;
import io.xj.Segment;
import io.xj.SegmentChoice;
import io.xj.nexus.dao.exception.DAOExistenceException;
import junit.framework.TestCase;

import java.util.Collection;
import java.util.UUID;

public class SegmentsTest extends TestCase {
  private final String chainId = UUID.randomUUID().toString();
  private final Segment seg0 = Segment.newBuilder()
    .setId(UUID.randomUUID().toString())
    .setChainId(chainId)
    .setType(Segment.Type.Initial)
    .setOffset(0L)
    .setDelta(0)
    .setState(Segment.State.Dubbed)
    .setBeginAt("2017-02-14T12:02:04.000001Z")
    .setEndAt("2017-02-14T12:02:36.000001Z")
    .setKey("F Major")
    .setTotal(64)
    .setDensity(0.30)
    .setTempo(120.0)
    .setStorageKey("chains-1-segments-9f7s89d8a7892")
    .setOutputEncoder("wav")
    .build();
  private final Segment seg1 = Segment.newBuilder()
    .setId(UUID.randomUUID().toString())
    .setChainId(chainId)
    .setType(Segment.Type.Continue)
    .setOffset(1L)
    .setDelta(1)
    .setState(Segment.State.Dubbed)
    .setBeginAt("2017-02-14T12:02:36.000001Z")
    .setEndAt("2017-02-14T12:03:08.000001Z")
    .setKey("F Major")
    .setTotal(64)
    .setDensity(0.30)
    .setTempo(120.0)
    .setStorageKey("chains-1-segments-078aw34tiuhga")
    .setOutputEncoder("wav")
    .build();
  private final Segment seg2 = Segment.newBuilder()
    .setId(UUID.randomUUID().toString())
    .setChainId(chainId)
    .setType(Segment.Type.NextMain)
    .setOffset(2L)
    .setDelta(0)
    .setState(Segment.State.Dubbed)
    .setBeginAt("2017-02-14T12:03:08.000001Z")
    .setEndAt("2017-02-14T12:03:40.000001Z")
    .setKey("F Major")
    .setTotal(64)
    .setDensity(0.30)
    .setTempo(120.0)
    .setStorageKey("chains-1-segments-jhz5sd4fgi786q")
    .setOutputEncoder("wav")
    .build();
  private final Segment seg3 = Segment.newBuilder()
    .setId(UUID.randomUUID().toString())
    .setChainId(chainId)
    .setType(Segment.Type.NextMain)
    .setOffset(3L)
    .setDelta(0)
    .setState(Segment.State.Crafting)
    .setBeginAt("2017-02-14T12:03:40.000001Z")
    .setEndAt("2017-02-14T12:04:12.000001Z")
    .setKey("F Major")
    .setTotal(64)
    .setDensity(0.30)
    .setTempo(120.0)
    .setStorageKey("chains-1-segments-j1hsk3dgu2yu2gyy")
    .setOutputEncoder("wav")
    .build();
  Collection<Segment> segments = ImmutableList.of(seg0, seg1, seg2, seg3);

  public void setUp() throws Exception {
    super.setUp();
  }

  public void testFindFirstOfType() throws DAOExistenceException {
    var ch0 = SegmentChoice.newBuilder()
      .setProgramType(Program.Type.Main)
      .build();
    var ch1 = SegmentChoice.newBuilder()
      .setProgramType(Program.Type.Macro)
      .build();
    assertEquals(ch0, Segments.findFirstOfType(ImmutableList.of(ch0, ch1), Program.Type.Main));
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
    assertEquals(ImmutableList.of(seg0,seg1,seg2),
      Segments.getDubbed(segments));
  }

  public void testGetLengthSeconds() {
    assertEquals(32.0, Segments.getLengthSeconds(seg0), 0.1);
  }
}
