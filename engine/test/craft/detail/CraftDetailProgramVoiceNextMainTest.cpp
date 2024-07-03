// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <set>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "../../_helper/ContentFixtures.h"
#include "../../_helper/SegmentFixtures.h"

#include "xjmusic/craft/Craft.h"
#include "xjmusic/craft/DetailCraft.h"
#include "xjmusic/fabricator/FabricatorFactory.h"
#include "xjmusic/fabricator/SegmentUtils.h"
#include "xjmusic/util/CsvUtils.h"

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

class CraftDetailProgramVoiceNextMainTest : public testing::Test {
protected:
  FabricatorFactory *fabricatorFactory = nullptr;
  ContentEntityStore *sourceMaterial = nullptr;
  SegmentEntityStore *store = nullptr;
  ContentFixtures *fake = nullptr;
  Chain *chain1 = nullptr;
  const Segment *segment4 = nullptr;

  void SetUp() override {
    store = new SegmentEntityStore();
    fabricatorFactory = new FabricatorFactory(store);

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new ContentFixtures();
    sourceMaterial = new ContentEntityStore();
    fake->setupFixtureB1(sourceMaterial);
    fake->setupFixtureB2(sourceMaterial);
    fake->setupFixtureB4_DetailBass(sourceMaterial);


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
        Segment::Type::Continue,
        1,
        1,
        Segment::State::Crafting,
        "Db minor",
        64,
        0.85f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892",
        true));
  }

  void TearDown() override {
    delete fabricatorFactory;
    delete sourceMaterial;
    delete store;
    delete fake;
    delete chain1;
  }

  /**
   Insert fixture segments 3 and 4, including the detail choice for segment 3 only if specified

   @param excludeDetailChoiceForSegment3 if desired for the purpose of this test
   */
  void insertSegments3and4(const bool excludeDetailChoiceForSegment3) {
    // segment just crafted
    // Testing entities for reference
    const auto segment3 = store->put(SegmentFixtures::buildSegment(
        chain1,
        Segment::Type::Initial,
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
    if (!excludeDetailChoiceForSegment3)
      store->put(SegmentFixtures::buildSegmentChoice(
          segment3,
          SegmentChoice::DELTA_UNLIMITED,
          SegmentChoice::DELTA_UNLIMITED,
          &fake->program10));
    segment4 = store->put(SegmentFixtures::buildSegment(
        chain1,
        Segment::Type::NextMain,
        3,
        3,
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

    const auto chord0 = store->put(SegmentFixtures::buildSegmentChord(segment4, 0.0f, "G minor"));
    store->put(SegmentFixtures::buildSegmentChordVoicing(chord0, Instrument::Type::Bass, "G2, Bb2, D3"));
    const auto chord1 = store->put(SegmentFixtures::buildSegmentChord(segment4, 8.0f, "Ab minor"));
    store->put(SegmentFixtures::buildSegmentChordVoicing(chord1, Instrument::Type::Bass, "Ab2, C3, Eb3"));
  }
};

TEST_F(CraftDetailProgramVoiceNextMainTest, CraftDetailVoiceNextMain) {
  insertSegments3and4(true);
  const auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment4->id, std::nullopt);

  DetailCraft(fabricator).doWork();

  std::set<const SegmentChoice *> choices = fabricator->getCurrentDetailChoices();
  ASSERT_FALSE(fabricator->getArrangements(choices).empty());

  int pickedBloop = 0;
  const auto picks = fabricator->getPicks();
  for (const auto pick: picks) {
    if (pick->instrumentAudioId == fake->instrument9_audio8.id)
      pickedBloop++;
  }
  ASSERT_EQ(16, pickedBloop);
}

TEST_F(CraftDetailProgramVoiceNextMainTest, CraftDetailVoiceNextMain_okIfNoDetailChoice) {
  insertSegments3and4(false);
  const auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment4->id, std::nullopt);

  DetailCraft(fabricator).doWork();
}
