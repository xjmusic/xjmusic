// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <fstream>
#include <gtest/gtest.h>
#include <string>

#include "xjmusic/entities/content/ContentStore.h"

#include "../_helper/ContentTestHelper.h"
#include "xjmusic/entities/segment/Segment.h"
#include "xjmusic/entities/segment/Chain.h"

static std::string CONTENT_STORE_TEST_JSON_PATH = "_data/content_store_test.json";

using namespace XJ;

class SegmentUtilsTest : public ::testing::Test {
protected:
  // This function runs before each test
  void SetUp() override {
    Project project = FabricationContentOneFixtures::buildProject("Test");
    Template tmpl = FabricationContentOneFixtures::buildTemplate(project, "Test");
    Chain chain = buildChain(project, "Test", Chain::Type::Production, Chain::State::Fabricate, tmpl);
    Segment seg0 = FabricationContentTwoFixtures::buildSegment(
        chain,
        Segment::Type::Initial,
        0,
        0,
        Segment::State::Crafted,
        "F Major",
        64,
        0.30f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892",
        true);
    Segment seg1 = FabricationContentTwoFixtures::buildSegment(
        chain,
        Segment::Type::Continue,
        1,
        1,
        Segment::State::Crafted,
        "F Major",
        64,
        0.30f,
        120.0f,
        "chains-1-segments-078aw34tiu5hga",
        true);
    Segment seg2 = FabricationContentTwoFixtures::buildSegment(
        chain,
        Segment::Type::NextMain,
        2,
        0,
        Segment::State::Crafted,
        "F Major",
        64,
        0.30f,
        120.0f,
        "chains-1-segments-jhz5sd4fgi786q",
        true);
    Segment seg3 = FabricationContentTwoFixtures::buildSegment(
        chain,
        Segment::Type::NextMain,
        3,
        0,
        Segment::State::Crafting,
        "F Major",
        64,
        0.30f,
        120.0f,
        "chains-1-segments-j1hsk3dgu2yu2gyy",
        true);
    std::vector<Segment> segments = {seg0, seg1, seg2, seg3};
  }

  // You can also define a TearDown() function that runs after each test
  void TearDown() override {
    // Cleanup code here...
  }
};

TEST(SegmentUtilsTest, FromJsonFile) {
  // Load the JSON file
  std::ifstream file(CONTENT_STORE_TEST_JSON_PATH);
  ASSERT_TRUE(file.is_open());

  // Deserialize a content store from a JSON file stream
  ContentStore subject = ContentStore::fromJson(file);

  // Assert the correct count of entities in the content store
  ASSERT_EQ(2, subject.getInstruments().size());
  ASSERT_EQ(2, subject.getInstrumentAudios().size());
  ASSERT_EQ(2, subject.getInstrumentMemes().size());
  ASSERT_EQ(1, subject.getLibraries().size());
  ASSERT_EQ(2, subject.getPrograms().size());
  ASSERT_EQ(2, subject.getProgramMemes().size());
  ASSERT_EQ(2, subject.getProgramSequences().size());
  ASSERT_EQ(2, subject.getProgramSequenceBindings().size());
  ASSERT_EQ(2, subject.getProgramSequenceBindingMemes().size());
  ASSERT_EQ(2, subject.getProgramSequenceChords().size());
  ASSERT_EQ(2, subject.getProgramSequenceChordVoicings().size());
  ASSERT_EQ(2, subject.getProgramSequencePatterns().size());
  ASSERT_EQ(2, subject.getProgramSequencePatternEvents().size());
  ASSERT_EQ(2, subject.getProgramVoices().size());
  ASSERT_EQ(2, subject.getProgramVoiceTracks().size());
  ASSERT_EQ(1, subject.getProjects().size());
  ASSERT_EQ(2, subject.getTemplates().size());
  ASSERT_EQ(3, subject.getTemplateBindings().size());
}


/*
public class SegmentUtilsTest {

  @Test
  public void testFindFirstOfType() throws FabricationException {
    var ch0 = new SegmentChoice();
    ch0.setDeltaIn(Segment.DELTA_UNLIMITED);
    ch0.setDeltaOut(Segment.DELTA_UNLIMITED);
    ch0.setProgramType(Program::Type.Main);
    var ch1 = new SegmentChoice();
    ch1.setDeltaIn(Segment.DELTA_UNLIMITED);
    ch1.setDeltaOut(Segment.DELTA_UNLIMITED);
    ch1.setProgramType(Program::Type.Macro);
    assertEquals(ch0, SegmentUtils.findFirstOfType(List.of(ch0, ch1), Program::Type.Main));
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
    var s1 = createSameSegment("2014-08-12T12:17:02.527142Z", Segment::State.CRAFTED);
    var s1_failed = createSameSegment("2014-08-12T12:17:02.527142Z", Segment::State.FAILED);
    var s1_updated = createSameSegment("2014-09-09T09:09:09.999999Z", Segment::State.CRAFTED);
    assertTrue(SegmentUtils.isSameButUpdated(s1, s1_updated));
    assertTrue(SegmentUtils.isSameButUpdated(s1, s1_failed));
    assertFalse(SegmentUtils.isSameButUpdated(s1, s1));
    assertFalse(SegmentUtils.isSameButUpdated(s1, seg2));
  }

  Segment createSameSegment(String updatedAt, Segment::State state) {
    final Segment s = FabricationContentTwoFixtures.buildSegment(
      chain,
      Segment::Type.CONTINUE,
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
  void segmentId {
    SegmentChoice segmentChoice = new SegmentChoice();
    segmentChoice.setSegmentId(seg3.id);
    SegmentChoiceArrangement segmentChoiceArrangement = new SegmentChoiceArrangement();
    segmentChoiceArrangement.setSegmentId(seg3.id);
    SegmentChoiceArrangementPick segmentChoiceArrangementPick = new SegmentChoiceArrangementPick();
    segmentChoiceArrangementPick.setSegmentId(seg3.id);
    SegmentChord segmentChord = new SegmentChord();
    segmentChord.setSegmentId(seg3.id);
    SegmentChordVoicing segmentChordVoicing = new SegmentChordVoicing();
    segmentChordVoicing.setSegmentId(seg3.id);
    SegmentMeme segmentMeme = new SegmentMeme();
    segmentMeme.setSegmentId(seg3.id);
    SegmentMessage segmentMessage = new SegmentMessage();
    segmentMessage.setSegmentId(seg3.id);
    SegmentMeta segmentMeta = new SegmentMeta();
    segmentMeta.setSegmentId(seg3.id);

    assertEquals(seg3.id, SegmentUtils.getSegmentId(seg3));
    assertEquals(seg3.id, SegmentUtils.getSegmentId(segmentChoice));
    assertEquals(seg3.id, SegmentUtils.getSegmentId(segmentChoiceArrangement));
    assertEquals(seg3.id, SegmentUtils.getSegmentId(segmentChoiceArrangementPick));
    assertEquals(seg3.id, SegmentUtils.getSegmentId(segmentChord));
    assertEquals(seg3.id, SegmentUtils.getSegmentId(segmentChordVoicing));
    assertEquals(seg3.id, SegmentUtils.getSegmentId(segmentMeme));
    
    assertEquals(seg3.id, SegmentUtils.getSegmentId(segmentMessage));
    assertEquals(seg3.id, SegmentUtils.getSegmentId(segmentMeta));
    assertThrows(IllegalArgumentException.class, () -> SegmentUtils.getSegmentId(new Object()));
  }
}
*/
