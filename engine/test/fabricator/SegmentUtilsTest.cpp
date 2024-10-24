// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <string>

#include "xjmusic/fabricator/SegmentUtils.h"
#include "xjmusic/util/ValueUtils.h"

#include "../_helper/ContentFixtures.h"
#include "../_helper/SegmentFixtures.h"

using namespace XJ;

class SegmentUtilsTest : public testing::Test {
protected:
  std::vector<const Segment *> segments;
  Project project;
  Template tmpl;
  Chain chain;
  Segment seg0;
  Segment seg1;
  Segment seg2;
  Segment seg3;

  void SetUp() override {
    project = ContentFixtures::buildProject("Test");
    tmpl = ContentFixtures::buildTemplate(&project, "Test");
    chain = SegmentFixtures::buildChain("Test", Chain::Type::Production, Chain::State::Fabricate, &tmpl);
    seg0 = SegmentFixtures::buildSegment(
        &chain,
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
        &chain,
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
        &chain,
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
        &chain,
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
    segments = {&seg0, &seg1, &seg2, &seg3};
  }

  Segment createSameSegment(const unsigned long long updatedAt, const Segment::State state) const {
    Segment s = SegmentFixtures::buildSegment(
        &chain,
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
  ch0.id = EntityUtils::computeUniqueId();
  ch0.deltaIn = SegmentChoice::DELTA_UNLIMITED;
  ch0.deltaOut = SegmentChoice::DELTA_UNLIMITED;
  ch0.programType = Program::Type::Main;

  SegmentChoice ch1;
  ch1.id = EntityUtils::computeUniqueId();
  ch1.deltaIn = SegmentChoice::DELTA_UNLIMITED;
  ch1.deltaOut = SegmentChoice::DELTA_UNLIMITED;
  ch1.programType = Program::Type::Macro;

  std::set<const SegmentChoice *> choices = {&ch0, &ch1};
  ASSERT_EQ(ch0.id, SegmentUtils::findFirstOfType(choices, Program::Type::Main).value()->id);
}

// Test for getIdentifier
TEST_F(SegmentUtilsTest, GetIdentifier) {
  ASSERT_EQ("chains-1-segments-9f7s89d8a7892", SegmentUtils::getIdentifier(&seg0));
}

// Test for getLastDubbed
TEST_F(SegmentUtilsTest, GetLastDubbed) {
  const std::optional<const Segment *> lastCrafted = SegmentUtils::getLastCrafted(segments);

  ASSERT_TRUE(lastCrafted.has_value());
  ASSERT_EQ(seg2.id, lastCrafted.value()->id);
}

// Test for getLast
TEST_F(SegmentUtilsTest, GetLast) {
  const std::optional<const Segment *> last = SegmentUtils::getLast(segments);

  ASSERT_TRUE(last.has_value());
  ASSERT_EQ(seg3.id, last.value()->id);
}


// Test for getDubbed
TEST_F(SegmentUtilsTest, GetDubbed) {
  const std::vector expected = {&seg0, &seg1, &seg2};

  const auto actual = SegmentUtils::getCrafted(segments);

  ASSERT_EQ(expected.size(), actual.size());
  for (int i = 0; i < expected.size(); i++) {
    ASSERT_EQ(expected[i]->id, actual[i]->id);
  }
}

// Test for getShipKey
TEST_F(SegmentUtilsTest, GetShipKey) {
  ASSERT_EQ("chains-1-segments-078aw34tiu5hga.wav", SegmentUtils::getStorageFilename(&seg1, "wav"));
}

// Inclusive of segment start time; exclusive of segment end time (different from SegmentUtils::isIntersecting)
TEST_F(SegmentUtilsTest, IsSpanning) {
  ASSERT_TRUE(SegmentUtils::isSpanning(&seg1, 32 * ValueUtils::MICROS_PER_SECOND,
                                       32 * ValueUtils::MICROS_PER_SECOND));// true if exactly at beginning of segment
  ASSERT_FALSE(SegmentUtils::isSpanning(&seg1, 64 * ValueUtils::MICROS_PER_SECOND,
                                        64 * ValueUtils::MICROS_PER_SECOND));// false if exactly at end of segment
  ASSERT_FALSE(SegmentUtils::isSpanning(&seg1, 15 * ValueUtils::MICROS_PER_SECOND, 30 * ValueUtils::MICROS_PER_SECOND));
  ASSERT_TRUE(SegmentUtils::isSpanning(&seg1, 20 * ValueUtils::MICROS_PER_SECOND, 36 * ValueUtils::MICROS_PER_SECOND));
  ASSERT_TRUE(SegmentUtils::isSpanning(&seg1, 35 * ValueUtils::MICROS_PER_SECOND, 52 * ValueUtils::MICROS_PER_SECOND));
  ASSERT_TRUE(SegmentUtils::isSpanning(&seg1, 50 * ValueUtils::MICROS_PER_SECOND, 67 * ValueUtils::MICROS_PER_SECOND));
  ASSERT_FALSE(SegmentUtils::isSpanning(&seg1, 66 * ValueUtils::MICROS_PER_SECOND, 80 * ValueUtils::MICROS_PER_SECOND));
}

// Exclusive of segment start time; inclusive of segment end time (different from SegmentUtils::isSpanning)
TEST_F(SegmentUtilsTest, IsIntersecting) {
  ASSERT_FALSE(SegmentUtils::isIntersecting(&seg1, 15 * ValueUtils::MICROS_PER_SECOND, 100L));
  ASSERT_FALSE(SegmentUtils::isIntersecting(&seg1, 20 * ValueUtils::MICROS_PER_SECOND, 100L));
  ASSERT_TRUE(SegmentUtils::isIntersecting(&seg1, 35 * ValueUtils::MICROS_PER_SECOND, 100L));
  ASSERT_TRUE(SegmentUtils::isIntersecting(&seg1, 50 * ValueUtils::MICROS_PER_SECOND, 100L));
  ASSERT_FALSE(SegmentUtils::isIntersecting(&seg1, 65 * ValueUtils::MICROS_PER_SECOND, 100L));
  ASSERT_TRUE(SegmentUtils::isIntersecting(&seg1, 65 * ValueUtils::MICROS_PER_SECOND, 2000000L));// expanded threshold
  ASSERT_TRUE(SegmentUtils::isIntersecting(&seg1, 32 * ValueUtils::MICROS_PER_SECOND,
                                           0L));// true if exactly at beginning of segment when threshold is 0
  ASSERT_FALSE(SegmentUtils::isIntersecting(&seg1, 64 * ValueUtils::MICROS_PER_SECOND,
                                            0L));// false if exactly at end of segment when threshold is 0
}

// Test for isSameButUpdated
TEST_F(SegmentUtilsTest, IsSameButUpdated) {
  Segment s1 = createSameSegment(527142, Segment::State::Crafted);
  Segment s1_failed = createSameSegment(527142, Segment::State::Failed);
  Segment s1_updated = createSameSegment(627142, Segment::State::Crafted);
  ASSERT_TRUE(SegmentUtils::isSameButUpdated(&s1, &s1_updated));
  ASSERT_TRUE(SegmentUtils::isSameButUpdated(&s1, &s1_failed));
  ASSERT_FALSE(SegmentUtils::isSameButUpdated(&s1, &s1));
  ASSERT_FALSE(SegmentUtils::isSameButUpdated(&s1, &seg2));
}

// Test for getDurationMinMicros
TEST_F(SegmentUtilsTest, GetDurationMinMicros) {
  std::vector segments = {seg0, seg1, seg2, seg3};
  ASSERT_EQ(32000000L, SegmentUtils::getDurationMinMicros(segments));
}
