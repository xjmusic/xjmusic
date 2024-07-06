// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <set>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "../../_helper/ContentFixtures.h"
#include "../../_helper/SegmentFixtures.h"

#include "xjmusic/craft/Craft.h"
#include "xjmusic/craft/TransitionCraft.h"
#include "xjmusic/fabricator/SegmentUtils.h"
#include "xjmusic/util/CsvUtils.h"

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

class CraftTransitionInitialTest : public testing::Test {
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

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    const auto tmpl = ContentFixtures::buildTemplate(&fake->project1, "Test");
    const auto chain2 = store->put(SegmentFixtures::buildChain(
        "Print #2",
        Chain::Type::Production,
        Chain::State::Fabricate,
        &tmpl));

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
        "chains-1-segments-9f7s89d8a7892.wav",
        true));
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

    store->put(SegmentFixtures::buildSegmentChord(segment6, 0.0f, "C minor"));
    store->put(SegmentFixtures::buildSegmentChord(segment6, 8.0f, "Db minor"));
  }

  };

TEST_F(CraftTransitionInitialTest, CraftTransitionInitial) {
  auto fabricator = Fabricator(sourceMaterial.get(), store.get(), segment6->id, std::nullopt);

  TransitionCraft(&fabricator).doWork();
}
