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
using ::testing::Return;
using ::testing::ReturnRef;

using namespace XJ;

class CraftBeatProgramVoiceInitialTest : public ::testing::Test {
protected:
  CraftFactory *craftFactory = nullptr;
  FabricatorFactory *fabricatorFactory = nullptr;
  ContentEntityStore *sourceMaterial = nullptr;
  SegmentEntityStore *store = nullptr;
  ContentFixtures *fake = nullptr;
  Chain *chain2 = nullptr;
  Segment *segment0 = nullptr;

  void SetUp() override {
    craftFactory = new CraftFactory();
    store = new SegmentEntityStore();
    fabricatorFactory = new FabricatorFactory(store);

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // force known beat selection by destroying program 35
    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new ContentFixtures();
    sourceMaterial = new ContentEntityStore();
    fake->setupFixtureB1(sourceMaterial, false);
    fake->setupFixtureB3(sourceMaterial);

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    const auto tmpl = ContentFixtures::buildTemplate(&fake->project1, "Tests");
    chain2 = store->put(SegmentFixtures::buildChain(
        &fake->project1,
        "Print #2",
        Chain::Type::Production,
        Chain::State::Fabricate,
        &tmpl));
  }

  void TearDown() override {
    delete craftFactory;
    delete fabricatorFactory;
    delete sourceMaterial;
    delete store;
    delete fake;
    delete chain2;
    delete segment0;
  }

  /**
   Insert fixture segment 6, including the beat choice only if specified
   */
  void insertSegment() {
    segment0 = store->put(SegmentFixtures::buildSegment(
        chain2,
        Segment::Type::Initial,
        0,
        0,
        Segment::State::Crafting,
        "D Major",
        32,
        0.55f,
        130.0f,
        "chains-1-segments-9f7s89d8a7892.wav", true));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment0,
        0,
        SegmentChoice::DELTA_UNLIMITED,
        &fake->program4,
        &fake->program4_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment0,
        0,
        SegmentChoice::DELTA_UNLIMITED,
        &fake->program5,
        &fake->program5_sequence0_binding0));
    for (const std::string memeName: std::set<std::string>({"Special", "Wild", "Pessimism", "Outlook"}))
      store->put(SegmentFixtures::buildSegmentMeme(segment0, memeName));

    store->put(SegmentFixtures::buildSegmentChord(segment0, 0.0f, "C minor"));
    store->put(SegmentFixtures::buildSegmentChord(segment0, 8.0f, "Db minor"));
  }
};

TEST_F(CraftBeatProgramVoiceInitialTest, CraftBeatVoiceInitial) {
  insertSegment();

  const auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment0->id, 48000.0f, 2, std::nullopt);

  craftFactory->beat(fabricator).doWork();

  const auto result = store->readSegment(segment0->id).value();
  ASSERT_FALSE(store->readAllSegmentChoices(result->id).empty());
}

TEST_F(CraftBeatProgramVoiceInitialTest, CraftBeatVoiceInitial_okWhenNoBeatChoice) {
  insertSegment();
  const auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment0->id, 48000.0f, 2, std::nullopt);

  craftFactory->beat(fabricator).doWork();
}