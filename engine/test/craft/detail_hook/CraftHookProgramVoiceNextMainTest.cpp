// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <set>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "../../_helper/ContentFixtures.h"
#include "../../_helper/SegmentFixtures.h"

#include "xjmusic/craft/Craft.h"
#include "xjmusic/craft/DetailCraft.h"
#include "xjmusic/fabricator/SegmentUtils.h"
#include "xjmusic/util/CsvUtils.h"

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

class CraftHookProgramVoiceNextMainTest : public testing::Test {
protected:
    std::unique_ptr<ContentFixtures> fake;
  std::unique_ptr<SegmentEntityStore> store;
  std::unique_ptr<ContentEntityStore> sourceMaterial;
  Chain *chain1 = nullptr;
  const Segment *segment4 = nullptr;
  InstrumentAudio *audioKick = nullptr;
  InstrumentAudio *audioSnare = nullptr;

  void SetUp() override {
    store = std::make_unique<SegmentEntityStore>();


    // Mock request via HubClientFactory returns fake generated library of model content
    fake = std::make_unique<ContentFixtures>();
    sourceMaterial = std::make_unique<ContentEntityStore>();
    fake->setupFixtureB1(sourceMaterial.get());
    fake->setupFixtureB2(sourceMaterial.get());
    setupCustomFixtures();

    // Chain "Test Print #1" has 5 total segments
    chain1 = store->put(SegmentFixtures::buildChain(
        &fake->template1,
        "Test Print #1",
        Chain::Type::Production,
        Chain::State::Fabricate));
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
        Segment::Type::Continue,
        1,
        1,
        Segment::State::Crafting,
        "Db minor",
        64,
        0.85f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892.wav", true));
  }


  /**
   Some custom fixtures for testing

   @return list of all entities
   */
  void setupCustomFixtures() {
    // Instrument "808"
    const auto instrument1 = sourceMaterial->put(
        ContentFixtures::buildInstrument(&fake->library2, Instrument::Type::Hook, Instrument::Mode::Event,
                                         Instrument::State::Published, "Bongo Loop"));
    sourceMaterial->put(ContentFixtures::buildInstrumentMeme(instrument1, "heavy"));
    audioKick = sourceMaterial->put(ContentFixtures::buildInstrumentAudio(
        instrument1,
        "Kick",
        "19801735098q47895897895782138975898.wav",
        0.01f,
        2.123f,
        120.0f,
        0.6f,
        "KICK",
        "Eb",
        1.0f));
    audioSnare = sourceMaterial->put(ContentFixtures::buildInstrumentAudio(
        instrument1,
        "Snare",
        "a1g9f8u0k1v7f3e59o7j5e8s98.wav",
        0.01f,
        1.5f,
        120.0f,
        0.6f,
        "SNARE",
        "Ab",
        1.0f));
  }

  /**
 Insert fixture segments 3 and 4, including the hook choice for segment 3 only if specified
 */
  void insertSegments3and4() {
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
        "chains-1-segments-9f7s89d8a7892",
        true));
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

    // segment crafting
    segment4 = store->put(SegmentFixtures::buildSegment(
        chain1,
        Segment::Type::NextMain,
        0,
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

    store->put(SegmentFixtures::buildSegmentChord(segment4, 0.0f, "G minor"));
    store->put(SegmentFixtures::buildSegmentChord(segment4, 8.0f, "Ab minor"));
  }
};

TEST_F(CraftHookProgramVoiceNextMainTest, CraftHookVoiceNextMain) {
  insertSegments3and4();
  const auto retrospective = SegmentRetrospective(store.get(), segment4->id);
  auto fabricator = Fabricator(sourceMaterial.get(), store.get(), &retrospective, segment4->id, std::nullopt);

  DetailCraft(&fabricator).doWork();
}
