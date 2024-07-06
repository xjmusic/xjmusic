// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <set>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "../../_helper/ContentFixtures.h"
#include "../../_helper/SegmentFixtures.h"

#include "xjmusic/craft/Craft.h"
#include "xjmusic/craft/DetailCraft.h"
#include "xjmusic/fabricator/ChainUtils.h"

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

/**
  Percussion-type Loop-mode fabrication composited of layered Patterns https://github.com/xjmusic/xjmusic/issues/267
  */
class CraftPercLoop_LayeredVoicesTest : public testing::Test {
protected:
    std::unique_ptr<ContentEntityStore> sourceMaterial;
  std::unique_ptr<SegmentEntityStore> store;
  std::unique_ptr<ContentFixtures> fake;
  const Segment *segment4 = nullptr;

  void SetUp() override {
    store = std::make_unique<SegmentEntityStore>();

    // Mock request via HubClientFactory returns fake generated library of model content
    fake = std::make_unique<ContentFixtures>();
    sourceMaterial = std::make_unique<ContentEntityStore>();
    fake->setupFixtureB1(sourceMaterial.get(), false);
    setupCustomFixtures();

    // Chain "Test Print #1" has 5 total segments
    const auto chain1 = store->put(
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
        "chains-1-segments-9f7s89d8a7892.wav",
        true));

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
        "chains-1-segments-9f7s89d8a7892.wav",
        true));
    store->put(SegmentFixtures::buildSegmentChoice(segment3, Program::Type::Macro, &fake->program4_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(segment3, Program::Type::Main, &fake->program5_sequence0_binding0));

    // segment crafting
    segment4 = store->put(SegmentFixtures::buildSegment(
        chain1,
        Segment::Type::Continue,
        3,
        3,
        Segment::State::Crafting,
        "D Major",
        16,
        0.45f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892.wav",
        true));
    store->put(SegmentFixtures::buildSegmentChoice(segment4, Program::Type::Macro, &fake->program4_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(segment4, Program::Type::Main, &fake->program5_sequence1_binding0));

    for (const std::string &memeName: std::set<std::string>({"Cozy", "Classic", "Outlook", "Rosy"}))
      store->put(SegmentFixtures::buildSegmentMeme(segment4, memeName));

    store->put(SegmentFixtures::buildSegmentChord(segment4, 0.0f, "A minor"));
    store->put(SegmentFixtures::buildSegmentChord(segment4, 8.0f, "D Major"));
  }


  /**
   Some custom fixtures for testing

   @return list of all entities
   */
  void setupCustomFixtures() const {
    // Instrument "808"
    const auto instrument1 = sourceMaterial->put(
        ContentFixtures::buildInstrument(&fake->library2, Instrument::Type::Percussion, Instrument::Mode::Event,
                                         Instrument::State::Published, "Bongo Loop"));
    sourceMaterial->put(ContentFixtures::buildMeme(instrument1, "heavy"));
    sourceMaterial->put(
        ContentFixtures::buildAudio(instrument1, "Kick", "19801735098q47895897895782138975898.wav", 0.01f, 2.123f,
                                    120.0f, 0.6f, "KICK", "Eb", 1.0f));
    sourceMaterial->put(
        ContentFixtures::buildAudio(instrument1, "Snare", "a1g9f8u0k1v7f3e59o7j5e8s98.wav", 0.01f, 1.5f, 120.0f, 0.6f,
                                    "SNARE", "Ab", 1.0f));
    sourceMaterial->put(
        ContentFixtures::buildAudio(instrument1, "Hihat", "iop0803k1k2l3h5a3s2d3f4g.wav", 0.01f, 1.5f, 120.0f, 0.6f,
                                    "HIHAT", "Ab", 1.0f));
  }
};

TEST_F(CraftPercLoop_LayeredVoicesTest, craftPercLoopVoiceContinue) {
  auto fabricator = Fabricator(sourceMaterial.get(), store.get(), segment4->id, std::nullopt);

  DetailCraft(&fabricator).doWork();
}
