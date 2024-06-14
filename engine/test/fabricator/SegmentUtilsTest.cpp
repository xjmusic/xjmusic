// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <fstream>
#include <gtest/gtest.h>
#include <string>

#include "../_helper/ContentFixtures.h"
#include "../_helper/SegmentFixtures.h"
#include "../_helper/TestHelpers.h"
#include "xjmusic/fabricator/SegmentUtils.h"
#include "Entity.h"

using namespace XJ;

class SegmentUtilsTest : public ::testing::Test {
protected:
  std::vector<Segment> segments;
  Project project;
  Template tmpl;
  Chain chain;
  Segment seg0;
  Segment seg1;
  Segment seg2;
  Segment seg3;

  void SetUp() override {
    project = ContentFixtures::buildProject("Test");
    tmpl = ContentFixtures::buildTemplate(project, "Test");
    chain = SegmentFixtures::buildChain(project, "Test", Chain::Type::Production, Chain::State::Fabricate, tmpl);
    seg0 = SegmentFixtures::buildSegment(
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
    seg1 = SegmentFixtures::buildSegment(
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
    seg2 = SegmentFixtures::buildSegment(
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
    seg3 = SegmentFixtures::buildSegment(
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
    segments = {seg0, seg1, seg2, seg3};
  }

  Segment createSameSegment(long long updatedAt, Segment::State state) {
    Segment s = SegmentFixtures::buildSegment(
        chain,
        Segment::Type::Continue,
        1,
        1,
        state,
        "F Major",
        64,
        0.30f,
        120.0f,
        "chains-1-segments-078aw34tiu5hga",
        true);
    s.createdAt = updatedAt;
    s.updatedAt = updatedAt;
    return s;
  }
};


// Test for findFirstOfType
TEST_F(SegmentUtilsTest, FindFirstOfType) {
  SegmentChoice ch0;
  ch0.id = Entity::randomUUID();
  ch0.deltaIn = SegmentChoice::DELTA_UNLIMITED;
  ch0.deltaOut = SegmentChoice::DELTA_UNLIMITED;
  ch0.programType = Program::Type::Main;

  SegmentChoice ch1;
  ch1.id = Entity::randomUUID();
  ch1.deltaIn = SegmentChoice::DELTA_UNLIMITED;
  ch1.deltaOut = SegmentChoice::DELTA_UNLIMITED;
  ch1.programType = Program::Type::Macro;

  std::vector<SegmentChoice> choices = {ch0, ch1};
  ASSERT_EQ(ch0.id, SegmentUtils::findFirstOfType(choices, Program::Type::Main).value()->id);
}

// Test for getIdentifier
TEST_F(SegmentUtilsTest, GetIdentifier) {
  ASSERT_EQ("chains-1-segments-9f7s89d8a7892", SegmentUtils::getIdentifier(&seg0));
}

// Test for getLastDubbed
TEST_F(SegmentUtilsTest, GetLastDubbed) {
  std::optional<Segment> lastCrafted = SegmentUtils::getLastCrafted(segments);

  ASSERT_TRUE(lastCrafted.has_value());
  ASSERT_EQ(seg2.id, lastCrafted.value().id);
}

// Test for getLast
TEST_F(SegmentUtilsTest, GetLast) {
  std::optional<Segment> last = SegmentUtils::getLast(segments);

  ASSERT_TRUE(last.has_value());
  ASSERT_EQ(seg3.id, last.value().id);
}


// Test for getDubbed
TEST_F(SegmentUtilsTest, GetDubbed) {
  std::vector<Segment *> expected = {&seg0, &seg1, &seg2};

  auto actual = SegmentUtils::getCrafted(segments);

  ASSERT_EQ(expected.size(), actual.size());
  for (int i = 0; i < expected.size(); i++) {
    ASSERT_EQ(expected[i]->id, actual[i].id);
  }
}

// Test for getShipKey
TEST_F(SegmentUtilsTest, GetShipKey) {
  ASSERT_EQ("chains-1-segments-078aw34tiu5hga.wav", SegmentUtils::getStorageFilename(seg1, "wav"));
}

// Inclusive of segment start time; exclusive of segment end time (different from SegmentUtils::isIntersecting)
TEST_F(SegmentUtilsTest, IsSpanning) {
  ASSERT_TRUE(SegmentUtils::isSpanning(seg1, 32 * ValueUtils::MICROS_PER_SECOND,
                                       32 * ValueUtils::MICROS_PER_SECOND)); // true if exactly at beginning of segment
  ASSERT_FALSE(SegmentUtils::isSpanning(seg1, 64 * ValueUtils::MICROS_PER_SECOND,
                                        64 * ValueUtils::MICROS_PER_SECOND)); // false if exactly at end of segment
  ASSERT_FALSE(SegmentUtils::isSpanning(seg1, 15 * ValueUtils::MICROS_PER_SECOND, 30 * ValueUtils::MICROS_PER_SECOND));
  ASSERT_TRUE(SegmentUtils::isSpanning(seg1, 20 * ValueUtils::MICROS_PER_SECOND, 36 * ValueUtils::MICROS_PER_SECOND));
  ASSERT_TRUE(SegmentUtils::isSpanning(seg1, 35 * ValueUtils::MICROS_PER_SECOND, 52 * ValueUtils::MICROS_PER_SECOND));
  ASSERT_TRUE(SegmentUtils::isSpanning(seg1, 50 * ValueUtils::MICROS_PER_SECOND, 67 * ValueUtils::MICROS_PER_SECOND));
  ASSERT_FALSE(SegmentUtils::isSpanning(seg1, 66 * ValueUtils::MICROS_PER_SECOND, 80 * ValueUtils::MICROS_PER_SECOND));
}

// Exclusive of segment start time; inclusive of segment end time (different from SegmentUtils::isSpanning)
TEST_F(SegmentUtilsTest, IsIntersecting) {
  ASSERT_FALSE(SegmentUtils::isIntersecting(seg1, 15 * ValueUtils::MICROS_PER_SECOND, 100L));
  ASSERT_FALSE(SegmentUtils::isIntersecting(seg1, 20 * ValueUtils::MICROS_PER_SECOND, 100L));
  ASSERT_TRUE(SegmentUtils::isIntersecting(seg1, 35 * ValueUtils::MICROS_PER_SECOND, 100L));
  ASSERT_TRUE(SegmentUtils::isIntersecting(seg1, 50 * ValueUtils::MICROS_PER_SECOND, 100L));
  ASSERT_FALSE(SegmentUtils::isIntersecting(seg1, 65 * ValueUtils::MICROS_PER_SECOND, 100L));
  ASSERT_TRUE(SegmentUtils::isIntersecting(seg1, 65 * ValueUtils::MICROS_PER_SECOND, 2000000L)); // expanded threshold
  ASSERT_TRUE(SegmentUtils::isIntersecting(seg1, 32 * ValueUtils::MICROS_PER_SECOND,
                                           0L)); // true if exactly at beginning of segment when threshold is 0
  ASSERT_FALSE(SegmentUtils::isIntersecting(seg1, 64 * ValueUtils::MICROS_PER_SECOND,
                                            0L)); // false if exactly at end of segment when threshold is 0
}

// Test for isSameButUpdated
TEST_F(SegmentUtilsTest, IsSameButUpdated) {
  Segment s1 = createSameSegment(527142, Segment::State::Crafted);
  Segment s1_failed = createSameSegment(527142, Segment::State::Failed);
  Segment s1_updated = createSameSegment(627142, Segment::State::Crafted);
  ASSERT_TRUE(SegmentUtils::isSameButUpdated(s1, s1_updated));
  ASSERT_TRUE(SegmentUtils::isSameButUpdated(s1, s1_failed));
  ASSERT_FALSE(SegmentUtils::isSameButUpdated(s1, s1));
  ASSERT_FALSE(SegmentUtils::isSameButUpdated(s1, seg2));
}

// Test for getDurationMinMicros
TEST_F(SegmentUtilsTest, GetDurationMinMicros) {
  std::vector<Segment> segments = {seg0, seg1, seg2, seg3};
  ASSERT_EQ(32000000L, SegmentUtils::getDurationMinMicros(segments));
}

// Test for getSegmentId
TEST_F(SegmentUtilsTest, GetSegmentId) {
  SegmentChoice segmentChoice;
  segmentChoice.segmentId = seg3.id;
  SegmentChoiceArrangement segmentChoiceArrangement;
  segmentChoiceArrangement.segmentId = seg3.id;
  SegmentChoiceArrangementPick segmentChoiceArrangementPick;
  segmentChoiceArrangementPick.segmentId = seg3.id;
  SegmentChord segmentChord;
  segmentChord.segmentId = seg3.id;
  SegmentChordVoicing segmentChordVoicing;
  segmentChordVoicing.segmentId = seg3.id;
  SegmentMeme segmentMeme;
  segmentMeme.segmentId = seg3.id;
  SegmentMessage segmentMessage;
  segmentMessage.segmentId = seg3.id;
  SegmentMeta segmentMeta;
  segmentMeta.segmentId = seg3.id;

  ASSERT_EQ(seg3.id, SegmentUtils::getSegmentId(seg3));
  ASSERT_EQ(seg3.id, SegmentUtils::getSegmentId(segmentChoice));
  ASSERT_EQ(seg3.id, SegmentUtils::getSegmentId(segmentChoiceArrangement));
  ASSERT_EQ(seg3.id, SegmentUtils::getSegmentId(segmentChoiceArrangementPick));
  ASSERT_EQ(seg3.id, SegmentUtils::getSegmentId(segmentChord));
  ASSERT_EQ(seg3.id, SegmentUtils::getSegmentId(segmentChordVoicing));
  ASSERT_EQ(seg3.id, SegmentUtils::getSegmentId(segmentMeme));
  ASSERT_EQ(seg3.id, SegmentUtils::getSegmentId(segmentMessage));
  ASSERT_EQ(seg3.id, SegmentUtils::getSegmentId(segmentMeta));

  try {
    Program notSegmentObject;
    SegmentUtils::getSegmentId(notSegmentObject); // This line will throw an exception
    FAIL() << "Expected std::invalid_argument";
  }
  catch (std::invalid_argument const &err) {
    ASSERT_TRUE(std::string(err.what()).rfind("Can't get segment id", 0) == 0);
  }
  catch (...) {
    FAIL() << "Expected std::invalid_argument";
  }
}