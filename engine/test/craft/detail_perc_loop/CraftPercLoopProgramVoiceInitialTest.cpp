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

class CraftPercLoopProgramVoiceInitialTest : public testing::Test {
protected:
    std::unique_ptr<ContentEntityStore> sourceMaterial;
  std::unique_ptr<SegmentEntityStore> store;
  std::unique_ptr<ContentFixtures> fake;
  Chain *chain2 = nullptr;
  const Segment *segment0 = nullptr;

  void SetUp() override {
    store = std::make_unique<SegmentEntityStore>();


    // force known percLoop selection by destroying program 35
    // Mock request via HubClientFactory returns fake generated library of model content
    fake = std::make_unique<ContentFixtures>();
    sourceMaterial = std::make_unique<ContentEntityStore>();
    fake->setupFixtureB1(sourceMaterial.get(), false);
    fake->setupFixtureB3(sourceMaterial.get());

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    Chain chain;
    chain.id = EntityUtils::computeUniqueId();
    chain.name = "Print #2";
    chain.config = TemplateConfig();
    chain.type = Chain::Type::Production;
    chain.state = Chain::State::Fabricate;
    chain2 = store->put(chain);
  }


  /**
 Insert fixture segment 6, including the percLoop choice only if specified
 */
  void insertSegment() {
    segment0 = store->put(SegmentFixtures::buildSegment(
        chain2,
        0,
        Segment::State::Crafting,
        "D Major",
        32,
        0.55f,
        130.0f,
        "chains-1-segments-9f7s89d8a7892.wav"));
    store->put(
        SegmentFixtures::buildSegmentChoice(segment0, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED,
                                            &fake->program4, &fake->program4_sequence0_binding0));
    store->put(
        SegmentFixtures::buildSegmentChoice(segment0, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED,
                                            &fake->program5, &fake->program5_sequence0_binding0));
    for (const std::string &memeName: std::set<std::string>({"Special", "Wild", "Pessimism", "Outlook"}))
      store->put(SegmentFixtures::buildSegmentMeme(segment0, memeName));

    store->put(SegmentFixtures::buildSegmentChord(segment0, 0.0f, "C minor"));
    store->put(SegmentFixtures::buildSegmentChord(segment0, 8.0f, "Db minor"));
  }
};


TEST_F(CraftPercLoopProgramVoiceInitialTest, CraftPercLoopVoiceInitial) {
  insertSegment();

  auto fabricator = Fabricator(sourceMaterial.get(), store.get(), segment0->id, std::nullopt);

  DetailCraft(&fabricator).doWork();
}

TEST_F(CraftPercLoopProgramVoiceInitialTest, CraftPercLoopVoiceInitial_okWhenNoPercLoopChoice) {
  insertSegment();
  auto fabricator = Fabricator(sourceMaterial.get(), store.get(), segment0->id, std::nullopt);

  DetailCraft(&fabricator).doWork();
}
