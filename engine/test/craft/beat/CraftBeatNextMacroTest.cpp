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

class CraftBeatNextMacroTest : public testing::Test {
protected:
  CraftFactory *craftFactory = nullptr;
  FabricatorFactory *fabricatorFactory = nullptr;
  ContentFixtures *fake = nullptr;
  SegmentEntityStore *store = nullptr;
  ContentEntityStore *sourceMaterial = nullptr;
  Chain *chain1 = nullptr;
  Segment *segment4 = nullptr;

  void SetUp() override {
    craftFactory = new CraftFactory();
    store = new SegmentEntityStore();
    fabricatorFactory = new FabricatorFactory(store);

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new ContentFixtures();
    sourceMaterial = new ContentEntityStore();
    fake->setupFixtureB1(sourceMaterial);
    fake->setupFixtureB2(sourceMaterial);
    fake->setupFixtureB3(sourceMaterial);

    // Chain "Test Print #1" has 5 total segments
    chain1 = store->put(SegmentFixtures::buildChain("Test Print #1", Chain::Type::Production, Chain::State::Fabricate, &fake->template1, ""));
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
        0,
        Segment::State::Crafting,
        "Db minor",
        64,
        0.85f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892.wav", true));
  }

  void TearDown() override {
    delete craftFactory;
    delete fabricatorFactory;
    delete fake;
    delete store;
    delete sourceMaterial;
    delete chain1;
    delete segment4;
  }

  /**
   Insert fixture segments 3 and 4, including the beat choice for segment 3 only if specified

   @param excludeBeatChoiceForSegment3 if desired for the purpose of this test
   */
  void insertSegments3and4(const bool excludeBeatChoiceForSegment3) {
    // Chain "Test Print #1" has this segment that was just crafted
    const auto segment3 = store->put(SegmentFixtures::buildSegment(
        chain1,
        Segment::Type::Continue,
        2,
        2,
        Segment::State::Crafted,
        "Ab minor",
        64,
        0.30f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892.wav", true));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment3,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        &fake->program4,
        &fake->program4_sequence2_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment3,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        &fake->program5,
        &fake->program5_sequence1_binding0));
    if (!excludeBeatChoiceForSegment3)
      store->put(SegmentFixtures::buildSegmentChoice(
          segment3,
          SegmentChoice::DELTA_UNLIMITED,
          SegmentChoice::DELTA_UNLIMITED,
          &fake->program35));

    // Chain "Test Print #1" has a segment in crafting state - Foundation is complete
    segment4 = store->put(SegmentFixtures::buildSegment(
        chain1,
        Segment::Type::NextMacro,
        3,
        0,
        Segment::State::Crafting,
        "F minor",
        16,
        0.45f,
        125.0f,
        "chains-1-segments-9f7s89d8a7892.wav", true));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment4,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        &fake->program4,
        &fake->program4_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment4,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        &fake->program15,
        &fake->program15_sequence0_binding0));
    for (const std::string memeName: std::set<std::string>({"Hindsight", "Chunky", "Regret", "Tangy"}))
      store->put(SegmentFixtures::buildSegmentMeme(segment4, memeName));
    store->put(SegmentFixtures::buildSegmentChord(segment4, 0.0f, "F minor"));
    store->put(SegmentFixtures::buildSegmentChord(segment4, 8.0f, "Gb minor"));
  }
};

TEST_F(CraftBeatNextMacroTest, CraftBeatNextMacro) {
  insertSegments3and4(true);
  const auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment4->id, 48000.0f, 2, std::nullopt);

  craftFactory->beat(fabricator).doWork();

  // assert choice of beat-type sequence
  const auto segmentChoices =
      store->readAllSegmentChoices(segment4->id);
  ASSERT_EQ(SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Beat).has_value(), true);
}

TEST_F(CraftBeatNextMacroTest, CraftBeatNextMacro_okEvenWithoutPreviousSegmentBeatChoice) {
  insertSegments3and4(false);
  const auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment4->id, 48000.0f, 2, std::nullopt);

  craftFactory->beat(fabricator).doWork();

  // assert choice of beat-type sequence
  const auto segmentChoices =
      store->readAllSegmentChoices(segment4->id);
  ASSERT_EQ(SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Beat).has_value(), true);
}
