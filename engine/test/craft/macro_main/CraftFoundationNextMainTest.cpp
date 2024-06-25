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
#include "xjmusic/fabricator/FabricationFatalException.h"
#include "xjmusic/fabricator/FabricatorFactory.h"
#include "xjmusic/fabricator/SegmentUtils.h"
#include "xjmusic/util/CsvUtils.h"
#include "xjmusic/util/ValueUtils.h"

// NOLINTNEXTLINE
using ::testing::_;
using ::testing::Return;
using ::testing::ReturnRef;

using namespace XJ;

class CraftFoundationNextMainTest : public ::testing::Test {
protected:
  CraftFactory *craftFactory = nullptr;
  FabricatorFactory *fabricatorFactory = nullptr;
  ContentFixtures *fake = nullptr;
  SegmentEntityStore *store = nullptr;
  ContentEntityStore *sourceMaterial = nullptr;
  Chain *chain1 = nullptr;
  Segment *segment4 = nullptr;

  void SetUp() override {
    store = new SegmentEntityStore();
    fabricatorFactory = new FabricatorFactory(store);
    craftFactory = new CraftFactory();

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    sourceMaterial = new ContentEntityStore();
    fake = new ContentFixtures();
    fake->setupFixtureB1(sourceMaterial);
    fake->setupFixtureB2(sourceMaterial);

    // Chain "Test Print #1" has 5 total segments
    chain1 = store->put(SegmentFixtures::buildChain(&fake->project1, "Test Print #1", Chain::Type::Production, Chain::State::Fabricate, &fake->template1, ""));
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
        1,
        Segment::State::Crafting,
        "Db minor",
        64,
        0.85f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892.wav"));

    // Chain "Test Print #1" has this segment that was just crafted
    const auto segment3 = store->put(SegmentFixtures::buildSegment(
        chain1,
        2,
        Segment::State::Crafted,
        "F Major",
        64,
        0.30f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892.wav"));
    store->put(SegmentFixtures::buildSegmentChoice(segment3, Program::Type::Macro, &fake->program4_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(segment3, Program::Type::Main, &fake->program5_sequence1_binding0));

    // Chain "Test Print #1" has a planned segment
    segment4 = store->put(SegmentFixtures::buildSegment(chain1, 3, Segment::State::Planned, "C", 8, 0.8f, 120, "chain-1-waveform-12345"));
  }

  void TearDown() override {
    delete fake;
    delete sourceMaterial;
    delete store;
    delete fabricatorFactory;
    delete craftFactory;
    delete chain1;
    delete segment4;
  }
};

TEST_F(CraftFoundationNextMainTest, CraftFoundationNextMain) {
  auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment4->id, 48000.0f, 2, std::nullopt);

  craftFactory->macroMain(fabricator, std::nullopt, {}).doWork();

  auto result = store->readSegment(segment4->id).value();
  ASSERT_EQ(Segment::Type::NextMain, result->type);
  ASSERT_EQ(16 * ValueUtils::MICROS_PER_MINUTE / 140, result->durationMicros);
  ASSERT_EQ(16, result->total);
  ASSERT_NEAR(0.2, result->intensity, 0.01);
  ASSERT_EQ("G -", result->key);
  ASSERT_NEAR(140, result->tempo, 0.01);
  // assert memes
  ASSERT_EQ(std::set<std::string>({"HINDSIGHT", "TROPICAL", "COZY", "WILD", "REGRET"}),
            SegmentMeme::getNames(store->readAllSegmentMemes(result->id)));
  // assert chords
  ASSERT_EQ(std::set<std::string>({"G -", "Ab -"}),
            SegmentChord::getNames(store->readAllSegmentChords(result->id)));
  // assert choices
  auto segmentChoices =
      store->readAllSegmentChoices(result->id);
  // assert macro choice
  auto macroChoice = SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Macro);
  ASSERT_EQ(fake->program4_sequence1_binding0.id, macroChoice.value()->programSequenceBindingId);
  ASSERT_EQ(1, fabricator->getSequenceBindingOffsetForChoice(macroChoice.value()));
  // assert main choice
  auto mainChoice = SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Main);
  ASSERT_EQ(fake->program15_sequence0_binding0.id, mainChoice.value()->programSequenceBindingId);
  ASSERT_EQ(0, fabricator->getSequenceBindingOffsetForChoice(mainChoice.value()));
}

/**
   Engineer wants a Segment to be reverted, and re-queued for Craft, in the event that such a Segment has just failed its Craft process, in order to ensure Chain fabrication fault tolerance https://github.com/xjmusic/xjmusic/issues/297
   */
TEST_F(CraftFoundationNextMainTest, CraftFoundationNextMain_revertsAndRequeueOnFailure) {
  // Chain "Test Print #1" has a dangling (preceded by another planned segment) planned segment
  const auto segment5 = store->put(SegmentFixtures::buildSegment(
      chain1,
      4,
      Segment::State::Planned,
      "C",
      8,
      0.8f,
      120.0f,
      "chain-1-waveform-12345.wav"));

  ASSERT_THROW(fabricatorFactory->fabricate(sourceMaterial, segment5->id, 48000.0f, 2, std::nullopt), FabricationFatalException);
}
