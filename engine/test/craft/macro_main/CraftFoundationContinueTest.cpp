// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <set>
#include <vector>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "../../_helper/ContentFixtures.h"
#include "../../_helper/SegmentFixtures.h"
#include "../../_helper/YamlTest.h"

#include "xjmusic/craft/Craft.h"
#include "xjmusic/craft/CraftFactory.h"
#include "xjmusic/fabricator/ChainUtils.h"
#include "xjmusic/fabricator/FabricatorFactory.h"
#include "xjmusic/fabricator/SegmentUtils.h"
#include "xjmusic/util/CsvUtils.h"
#include "xjmusic/util/ValueUtils.h"

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

class CraftFoundationContinueTest : public testing::Test {
protected:
  CraftFactory *craftFactory = nullptr;
  FabricatorFactory *fabricatorFactory = nullptr;
  ContentEntityStore *sourceMaterial = nullptr;
  SegmentEntityStore *store = nullptr;
  ContentFixtures *fake = nullptr;
  Segment *segment4 = nullptr;

  void SetUp() override {
    craftFactory = new CraftFactory();
    store = new SegmentEntityStore();
    sourceMaterial = new ContentEntityStore();
    fabricatorFactory = new FabricatorFactory(store);

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new ContentFixtures();
    fake->setupFixtureB1(sourceMaterial);
    fake->setupFixtureB2(sourceMaterial);

    // Chain "Test Print #1" has 5 total segments
    const auto chain1 = store->put(SegmentFixtures::buildChain("Test Print #1", Chain::Type::Production, Chain::State::Fabricate, &fake->template1, ""));
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

  void TearDown() override {
    delete fake;
    delete sourceMaterial;
    delete store;
    delete fabricatorFactory;
    delete craftFactory;
    delete segment4;
  }
};

TEST_F(CraftFoundationContinueTest, CraftFoundationContinue) {
  auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment4->id, 48000.0f, 2, std::nullopt);

  craftFactory->macroMain(fabricator, std::nullopt, {}).doWork();

  auto result = store->readSegment(segment4->id).value();
  ASSERT_EQ(Segment::Type::Continue, result->type);
  ASSERT_EQ(32 * ValueUtils::MICROS_PER_MINUTE / 140, result->durationMicros);
  ASSERT_EQ(32, result->total);
  ASSERT_NEAR(0.23, result->intensity, 0.001);
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
  ASSERT_EQ(1, fabricator->getSequenceBindingOffsetForChoice(macroChoice.value()));
  // assert main choice
  auto mainChoice = SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Main);
  ASSERT_TRUE(mainChoice.has_value());
  ASSERT_EQ(fake->program5_sequence1_binding0.id, mainChoice.value()->programSequenceBindingId);// next main sequence binding in same program as previous sequence
  ASSERT_EQ(1, fabricator->getSequenceBindingOffsetForChoice(mainChoice.value()));
}
