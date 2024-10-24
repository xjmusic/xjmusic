// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <set>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "../../_helper/ContentFixtures.h"
#include "../../_helper/SegmentFixtures.h"

#include "xjmusic/craft/Craft.h"
#include "xjmusic/craft/MacroMainCraft.h"
#include "xjmusic/fabricator/SegmentUtils.h"
#include "xjmusic/util/ValueUtils.h"

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

class CraftFoundationContinueTest : public testing::Test {
protected:
    std::unique_ptr<ContentEntityStore> sourceMaterial;
  std::unique_ptr<SegmentEntityStore> store;
  std::unique_ptr<ContentFixtures> fake;
  const Segment *segment4 = nullptr;

  void SetUp() override {
    store = std::make_unique<SegmentEntityStore>();
    sourceMaterial = std::make_unique<ContentEntityStore>();


    // Mock request via HubClientFactory returns fake generated library of model content
    fake = std::make_unique<ContentFixtures>();
    fake->setupFixtureB1(sourceMaterial.get());
    fake->setupFixtureB2(sourceMaterial.get());

    // Chain "Test Print #1" has 5 total segments
    const auto chain1 = store->put(
        SegmentFixtures::buildChain("Test Print #1", Chain::Type::Production, Chain::State::Fabricate, &fake->template1,
                                    ""));
    store->put(SegmentFixtures::buildSegment(
        chain1,
        0,
        Segment::State::Crafted,
        "D major",
        64,
        0.73f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892"));
    store->put(SegmentFixtures::buildSegment(
        chain1,
        1,
        Segment::State::Crafting,
        "Db minor",
        64,
        0.85f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892"));
    // Chain "Test Print #1" has this segment that was just crafted
    const auto segment3 = store->put(SegmentFixtures::buildSegment(
        chain1,
        2,
        Segment::State::Crafted,
        "F Major",
        64,
        0.30f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892.wav"));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment3,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        &fake->program4,
        &fake->program4_sequence1_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment3,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        &fake->program5,
        &fake->program5_sequence0_binding0));

    // Chain "Test Print #1" has a planned segment
    segment4 = store->put(SegmentFixtures::buildSegment(
        chain1,
        3,
        Segment::State::Planned,
        "C",
        4,
        1.0f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892"));
  }
};

TEST_F(CraftFoundationContinueTest, CraftFoundationContinue) {
  const auto retrospective = SegmentRetrospective(store.get(), segment4->id);
  auto fabricator = Fabricator(sourceMaterial.get(), store.get(), &retrospective, segment4->id, std::nullopt);

  MacroMainCraft(&fabricator, std::nullopt, {}).doWork();

  auto result = store->readSegment(segment4->id).value();
  ASSERT_EQ(Segment::Type::Continue, result->type);
  ASSERT_EQ(32 * ValueUtils::MICROS_PER_MINUTE / 140, result->durationMicros);
  ASSERT_EQ(32, result->total);
  ASSERT_NEAR(0.2, result->intensity, 0.001);
  ASSERT_EQ("G -", result->key);
  ASSERT_NEAR(140, result->tempo, 0.001);
  ASSERT_EQ(Segment::Type::Continue, result->type);
  // assert memes
  ASSERT_EQ(
      std::set<std::string>({"OUTLOOK", "TROPICAL", "COZY", "WILD", "PESSIMISM"}),
      SegmentMeme::getNames(store->readAllSegmentMemes(result->id)));
  // assert chords
  ASSERT_EQ(
      std::set<std::string>({"Bb -", "C"}),
      SegmentChord::getNames(store->readAllSegmentChords(result->id)));
  // assert choices
  auto segmentChoices = store->readAllSegmentChoices(result->id);
  // assert macro choice
  auto macroChoice = SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Macro);
  ASSERT_TRUE(macroChoice.has_value());
  ASSERT_EQ(fake->program4_sequence1_binding0.id, macroChoice.value()->programSequenceBindingId);
  ASSERT_EQ(1, fabricator.getSequenceBindingOffsetForChoice(macroChoice.value()));
  // assert main choice
  auto mainChoice = SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Main);
  ASSERT_TRUE(mainChoice.has_value());
  ASSERT_EQ(fake->program5_sequence1_binding0.id,
            mainChoice.value()->programSequenceBindingId);// next main sequence binding in same program as previous sequence
  ASSERT_EQ(1, fabricator.getSequenceBindingOffsetForChoice(mainChoice.value()));
}
