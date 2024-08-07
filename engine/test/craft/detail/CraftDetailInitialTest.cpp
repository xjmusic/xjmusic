// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <set>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "../../_helper/ContentFixtures.h"
#include "../../_helper/SegmentFixtures.h"

#include "xjmusic/craft/Craft.h"
#include "xjmusic/craft/DetailCraft.h"
#include "xjmusic/fabricator/SegmentUtils.h"

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

class CraftDetailInitialTest : public testing::Test {
protected:
    std::unique_ptr<ContentEntityStore> sourceMaterial;
  std::unique_ptr<SegmentEntityStore> store;
  const Segment *segment6 = nullptr;

  void SetUp() override {
    store = std::make_unique<SegmentEntityStore>();


    // Mock request via HubClientFactory returns fake generated library of model content
    const auto fake = std::make_unique<ContentFixtures>();
    sourceMaterial = std::make_unique<ContentEntityStore>();
    fake->setupFixtureB1(sourceMaterial.get());
    fake->setupFixtureB2(sourceMaterial.get());
    fake->setupFixtureB3(sourceMaterial.get());
    fake->setupFixtureB4_DetailBass(sourceMaterial.get());


    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    const auto chain2 = store->put(SegmentFixtures::buildChain(
        &fake->template1,
        "Print #2",
        Chain::Type::Production,
        Chain::State::Fabricate));

    // segment crafting
    segment6 = store->put(SegmentFixtures::buildSegment(
        chain2,
        Segment::Type::Initial,
        0,
        0,
        Segment::State::Crafting,
        "C minor",
        16,
        0.55f,
        130.0f,
        "chains-1-segments-9f7s89d8a7892.wav", true));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment6,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        &fake->program4,
        &fake->program4_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment6,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        &fake->program5,
        &fake->program5_sequence0_binding0));
    for (const std::string &memeName: std::set<std::string>({"Special", "Wild", "Pessimism", "Outlook"}))
      store->put(SegmentFixtures::buildSegmentMeme(segment6, memeName));
    const auto chord0 = store->put(SegmentFixtures::buildSegmentChord(segment6, 0.0f, "C minor"));
    store->put(SegmentFixtures::buildSegmentChordVoicing(chord0, Instrument::Type::Bass, "C2, Eb2, G2"));
    const auto chord1 = store->put(SegmentFixtures::buildSegmentChord(segment6, 8.0f, "Db minor"));
    store->put(SegmentFixtures::buildSegmentChordVoicing(chord1, Instrument::Type::Bass, "Db2, E2, Ab2"));
  }
};

TEST_F(CraftDetailInitialTest, CraftDetailInitial) {
  const auto retrospective = SegmentRetrospective(store.get(), segment6->id);
  auto fabricator = Fabricator(sourceMaterial.get(), store.get(), &retrospective, segment6->id, std::nullopt);

  DetailCraft(&fabricator).doWork();

  // assert choice of detail-type sequence
  const auto choices = store->readAllSegmentChoices(segment6->id);
  ASSERT_EQ(SegmentUtils::findFirstOfType(choices, Program::Type::Detail).has_value(), true);

  // Detail Craft v1 -- segment chords voicings belong to chords and segments https://github.com/xjmusic/xjmusic/issues/284
  const auto voicings = store->readAllSegmentChordVoicings(segment6->id);
  ASSERT_EQ(2, voicings.size());
}
