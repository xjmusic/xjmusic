// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <set>
#include <vector>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "../../_helper/ContentFixtures.h"
#include "../../_helper/SegmentFixtures.h"
#include "../../_helper/YamlTest.h"

#include "xjmusic/craft/BeatCraft.h"
#include "xjmusic/craft/Craft.h"
#include "xjmusic/fabricator/ChainUtils.h"
#include "xjmusic/fabricator/SegmentUtils.h"
#include "xjmusic/util/CsvUtils.h"
#include "xjmusic/util/ValueUtils.h"

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

class CraftBeatNextMainTest : public testing::Test {
protected:
    std::unique_ptr<ContentEntityStore> sourceMaterial;
  std::unique_ptr<SegmentEntityStore> store;
  std::unique_ptr<ContentFixtures> fake;
  Chain *chain1 = nullptr;
  const Segment *segment4 = nullptr;

  void SetUp() override {
    store = std::make_unique<SegmentEntityStore>();


    // Mock request via HubClientFactory returns fake generated library of model content
    fake = std::make_unique<ContentFixtures>();
    sourceMaterial = std::make_unique<ContentEntityStore>();
    fake->setupFixtureB1(sourceMaterial.get());
    fake->setupFixtureB2(sourceMaterial.get());
    fake->setupFixtureB3(sourceMaterial.get());

    // Chain "Test Print #1" has 5 total segments
    chain1 = store->put(
        SegmentFixtures::buildChain("Test Print #1", Chain::Type::Production, Chain::State::Fabricate, &fake->template1,
                                    ""));
    store->put(SegmentFixtures::buildSegment(
        chain1,
        Segment::Type::Initial,
        0,
        0,
        Segment::State::Crafted,
        "D major",
        64,
        0.73f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892",
        true));
    store->put(SegmentFixtures::buildSegment(
        chain1,
        1,
        Segment::State::Crafting,
        "Db minor",
        64,
        0.85f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892.wav"));
  }


  /**
   Insert fixture segments 3 and 4, including the beat choice for segment 3 only if specified

   @param excludeBeatChoiceForSegment3 if desired for the purpose of this test
   */
  void insertSegments3and4(const bool excludeBeatChoiceForSegment3) {
    // segment just crafted
    // Testing entities for reference
    const auto segment3 = store->put(SegmentFixtures::buildSegment(
        chain1,
        Segment::Type::Continue,
        2,
        2,
        Segment::State::Crafted,
        "F Major",
        64,
        0.30f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892.wav", true));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment3,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        &fake->program4,
        &fake->program4_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment3,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        &fake->program15,
        &fake->program15_sequence1_binding0));
    if (!excludeBeatChoiceForSegment3)
      store->put(SegmentFixtures::buildSegmentChoice(
          segment3,
          SegmentChoice::DELTA_UNLIMITED,
          SegmentChoice::DELTA_UNLIMITED,
          &fake->program35));

    // segment crafting
    segment4 = store->put(SegmentFixtures::buildSegment(
        chain1,
        Segment::Type::NextMain,
        3,
        0,
        Segment::State::Crafting,
        "G minor",
        16,
        0.45f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892.wav", true));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment4,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        &fake->program4,
        &fake->program4_sequence1_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment4,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        &fake->program15,
        &fake->program15_sequence0_binding0));
    for (const std::string &memeName: std::set<std::string>({"Regret", "Sky", "Hindsight", "Tropical"}))
      store->put(SegmentFixtures::buildSegmentMeme(segment4, memeName));
    store->put(SegmentFixtures::buildSegmentChord(segment4, 0.0f, "G minor"));
    store->put(SegmentFixtures::buildSegmentChord(segment4, 8.0f, "Ab minor"));
  }
};

TEST_F(CraftBeatNextMainTest, CraftBeatNextMain) {
  insertSegments3and4(false);
  const auto retrospective = SegmentRetrospective(store.get(), segment4->id);
  auto fabricator = Fabricator(sourceMaterial.get(), store.get(), &retrospective, segment4->id, std::nullopt);

  BeatCraft(&fabricator).doWork();

  // assert choice of beat-type sequence
  const auto segmentChoices =
      store->readAllSegmentChoices(segment4->id);
  ASSERT_EQ(SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Beat).has_value(), true);
}

TEST_F(CraftBeatNextMainTest, CraftBeatNextMain_okEvenWithoutPreviousSegmentBeatChoice) {
  insertSegments3and4(true);
  const auto retrospective = SegmentRetrospective(store.get(), segment4->id);
  auto fabricator = Fabricator(sourceMaterial.get(), store.get(), &retrospective, segment4->id, std::nullopt);

  BeatCraft(&fabricator).doWork();

  // assert choice of beat-type sequence
  const auto segmentChoices =
      store->readAllSegmentChoices(segment4->id);
  ASSERT_EQ(SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Beat).has_value(), true);
}
