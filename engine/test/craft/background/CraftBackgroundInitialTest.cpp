// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <set>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "../../_helper/ContentFixtures.h"
#include "../../_helper/SegmentFixtures.h"

#include "xjmusic/craft/BackgroundCraft.h"
#include "xjmusic/craft/Craft.h"
#include "xjmusic/fabricator/FabricatorFactory.h"
#include "xjmusic/util/CsvUtils.h"

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

class CraftBackgroundInitialTest : public testing::Test {
protected:
  FabricatorFactory *fabricatorFactory = nullptr;
  ContentEntityStore *sourceMaterial = nullptr;
  SegmentEntityStore *store = nullptr;
  const Segment *segment6 = nullptr;

  void SetUp() override {
    store = new SegmentEntityStore();
    fabricatorFactory = new FabricatorFactory(store);

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    const auto fake = new ContentFixtures();
    sourceMaterial = new ContentEntityStore();
    fake->setupFixtureB1(sourceMaterial);
    fake->setupFixtureB2(sourceMaterial);
    fake->setupFixtureB3(sourceMaterial);


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

  void TearDown() override {
    delete fabricatorFactory;
    delete sourceMaterial;
    delete store;
  }
};

TEST_F(CraftBackgroundInitialTest, CraftBackgroundInitial) {
  const auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment6->id, std::nullopt);

  BackgroundCraft(fabricator).doWork();
}
