// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

package io.xj.engine.fabricator;

import io.xj.hub.enums.ProgramType;
import io.xj.hub.pojos.Project;
import io.xj.hub.pojos.Template;
import io.xj.engine.FabricationException;
import io.xj.engine.model.Chain;
import io.xj.engine.model.ChainState;
import io.xj.engine.model.ChainType;
import io.xj.engine.model.Segment;
import io.xj.engine.model.SegmentChoice;
import io.xj.engine.model.SegmentChoiceArrangement;
import io.xj.engine.model.SegmentChoiceArrangementPick;
import io.xj.engine.model.SegmentChord;
import io.xj.engine.model.SegmentChordVoicing;
import io.xj.engine.model.SegmentMeme;
import io.xj.engine.model.SegmentMessage;
import io.xj.engine.model.SegmentMeta;
import io.xj.engine.model.SegmentState;
import io.xj.engine.model.SegmentType;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static io.xj.hub.util.ValueUtils.MICROS_PER_SECOND;
import static io.xj.engine.NexusHubIntegrationTestingFixtures.buildProject;
import static io.xj.engine.NexusHubIntegrationTestingFixtures.buildTemplate;
import static io.xj.engine.NexusIntegrationTestingFixtures.buildChain;
import static io.xj.engine.NexusIntegrationTestingFixtures.buildSegment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SegmentUtilsTest {
  final Project project = buildProject("Test");
  final Template template = buildTemplate(project, "Test");
  final Chain chain = buildChain(project, "Test", ChainType.PRODUCTION, ChainState.FABRICATE, template);
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
    SegmentType.NEXT_MAIN,
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
    SegmentType.NEXT_MAIN,
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
  public void testFindFirstOfType() throws FabricationException {
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

  // Inclusive of segment start time; exclusive of segment end time (different from SegmentUtils.isIntersecting)
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

  // Exclusive of segment start time; inclusive of segment end time (different from SegmentUtils.isSpanning)
  @Test
  public void testIsIntersecting() {
    assertFalse(SegmentUtils.isIntersecting(seg1, 15 * MICROS_PER_SECOND, 100L));
    assertFalse(SegmentUtils.isIntersecting(seg1, 20 * MICROS_PER_SECOND, 100L));
    assertTrue(SegmentUtils.isIntersecting(seg1, 35 * MICROS_PER_SECOND, 100L));
    assertTrue(SegmentUtils.isIntersecting(seg1, 50 * MICROS_PER_SECOND, 100L));
    assertFalse(SegmentUtils.isIntersecting(seg1, 65 * MICROS_PER_SECOND, 100L));
    assertTrue(SegmentUtils.isIntersecting(seg1, 65 * MICROS_PER_SECOND, 2000000L)); // expanded threshold
    assertTrue(SegmentUtils.isIntersecting(seg1, 32 * MICROS_PER_SECOND, 0L)); // true if exactly at beginning of segment when threshold is 0
    assertFalse(SegmentUtils.isIntersecting(seg1, 64 * MICROS_PER_SECOND, 0L)); // false if exactly at end of segment when threshold is 0
  }

  @Test
  void isSameButUpdated() {
    var s1 = createSameSegment("2014-08-12T12:17:02.527142Z", SegmentState.CRAFTED);
    var s1_failed = createSameSegment("2014-08-12T12:17:02.527142Z", SegmentState.FAILED);
    var s1_updated = createSameSegment("2014-09-09T09:09:09.999999Z", SegmentState.CRAFTED);
    assertTrue(SegmentUtils.isSameButUpdated(s1, s1_updated));
    assertTrue(SegmentUtils.isSameButUpdated(s1, s1_failed));
    assertFalse(SegmentUtils.isSameButUpdated(s1, s1));
    assertFalse(SegmentUtils.isSameButUpdated(s1, seg2));
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

  @Test
  void testGetDurationMinMicros() {
    assertEquals(32000000L, SegmentUtils.getDurationMinMicros(List.of(seg0, seg1, seg2, seg3)));
  }

  @Test
  void getSegmentId() {
    SegmentChoice segmentChoice = new SegmentChoice();
    segmentChoice.setSegmentId(seg3.getId());
    SegmentChoiceArrangement segmentChoiceArrangement = new SegmentChoiceArrangement();
    segmentChoiceArrangement.setSegmentId(seg3.getId());
    SegmentChoiceArrangementPick segmentChoiceArrangementPick = new SegmentChoiceArrangementPick();
    segmentChoiceArrangementPick.setSegmentId(seg3.getId());
    SegmentChord segmentChord = new SegmentChord();
    segmentChord.setSegmentId(seg3.getId());
    SegmentChordVoicing segmentChordVoicing = new SegmentChordVoicing();
    segmentChordVoicing.setSegmentId(seg3.getId());
    SegmentMeme segmentMeme = new SegmentMeme();
    segmentMeme.setSegmentId(seg3.getId());
    SegmentMessage segmentMessage = new SegmentMessage();
    segmentMessage.setSegmentId(seg3.getId());
    SegmentMeta segmentMeta = new SegmentMeta();
    segmentMeta.setSegmentId(seg3.getId());

    assertEquals(seg3.getId(), SegmentUtils.getSegmentId(seg3));
    assertEquals(seg3.getId(), SegmentUtils.getSegmentId(segmentChoice));
    assertEquals(seg3.getId(), SegmentUtils.getSegmentId(segmentChoiceArrangement));
    assertEquals(seg3.getId(), SegmentUtils.getSegmentId(segmentChoiceArrangementPick));
    assertEquals(seg3.getId(), SegmentUtils.getSegmentId(segmentChord));
    assertEquals(seg3.getId(), SegmentUtils.getSegmentId(segmentChordVoicing));
    assertEquals(seg3.getId(), SegmentUtils.getSegmentId(segmentMeme));
    assertEquals(seg3.getId(), SegmentUtils.getSegmentId(segmentMessage));
    assertEquals(seg3.getId(), SegmentUtils.getSegmentId(segmentMeta));
    assertThrows(IllegalArgumentException.class, () -> SegmentUtils.getSegmentId(new Object()));
  }
}
