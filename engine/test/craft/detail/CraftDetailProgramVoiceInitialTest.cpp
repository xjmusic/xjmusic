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

class CraftDetailProgramVoiceInitialTest : public testing::Test {
protected:
  CraftFactory *craftFactory = nullptr;
  FabricatorFactory *fabricatorFactory = nullptr;
  ContentEntityStore *sourceMaterial = nullptr;
  SegmentEntityStore *store = nullptr;
  ContentFixtures *fake = nullptr;
  Chain *chain2 = nullptr;
  Segment *segment1 = nullptr;

  void SetUp() override {
    craftFactory = new CraftFactory();
    store = new SegmentEntityStore();
    fabricatorFactory = new FabricatorFactory(store);

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // force known detail selection by destroying program 35
    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new ContentFixtures();
    sourceMaterial = new ContentEntityStore();
    fake->setupFixtureB1(sourceMaterial, false);
    fake->setupFixtureB3(sourceMaterial);
    fake->setupFixtureB4_DetailBass(sourceMaterial);

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    chain2 = store->put(SegmentFixtures::buildChain(
        &fake->template1,
        "Print #2",
        Chain::Type::Production,
        Chain::State::Fabricate));
  }

  void TearDown() override {
    delete craftFactory;
    delete fabricatorFactory;
    delete sourceMaterial;
    delete store;
    delete fake;
    delete chain2;
    delete segment1;
  }

  /**
   Insert fixture segment 6, including the detail choice only if specified
   */
  void insertSegments() {
    // segment crafted
    const auto segment0 = store->put(SegmentFixtures::buildSegment(
        chain2,
        Segment::Type::Initial,
        0,
        0,
        Segment::State::Crafted,
        "D Major",
        32,
        0.55f,
        130.0f,
        "chains-1-segments-0970305977172.wav", true));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment0,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        &fake->program4,
        &fake->program4_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment0,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        &fake->program5,
        &fake->program5_sequence0_binding0));

    segment1 = store->put(SegmentFixtures::buildSegment(
        chain2,
        Segment::Type::Continue,
        1,
        1,
        Segment::State::Crafting,
        "D Major",
        32,
        0.55f,
        130.0f,
        "chains-1-segments-9f7s89d8a7892.wav", true));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment1,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        &fake->program4,
        &fake->program4_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(
        segment1,
        SegmentChoice::DELTA_UNLIMITED,
        SegmentChoice::DELTA_UNLIMITED,
        &fake->program5,
        &fake->program5_sequence0_binding0));
    for (const std::string memeName: std::set<std::string>({"Special", "Wild", "Pessimism", "Outlook"}))
      store->put(SegmentFixtures::buildSegmentMeme(segment1, memeName));
    const auto chord0 = store->put(SegmentFixtures::buildSegmentChord(segment1, 0.0f, "C minor"));
    store->put(SegmentFixtures::buildSegmentChordVoicing(chord0, Instrument::Type::Bass, "C2, Eb2, G2"));
    const auto chord1 = store->put(SegmentFixtures::buildSegmentChord(segment1, 8.0f, "Db minor"));
    store->put(SegmentFixtures::buildSegmentChordVoicing(chord1, Instrument::Type::Bass, "Db2, E2, Ab2"));
  }
};

TEST_F(CraftDetailProgramVoiceInitialTest, CraftDetailVoiceInitial) {
  insertSegments();

  const auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment1->id, 48000.0f, 2, std::nullopt);

  craftFactory->detail(fabricator).doWork();

  ASSERT_FALSE(fabricator->getChoices().empty());

  int pickedBloop = 0;
  const auto picks = fabricator->getPicks();
  for (const auto pick: picks) {
    if (pick->instrumentAudioId == fake->instrument9_audio8.id)
      pickedBloop++;
  }
  ASSERT_EQ(32, pickedBloop);
}

TEST_F(CraftDetailProgramVoiceInitialTest, CraftDetailVoiceInitial_okWhenNoDetailChoice) {
  insertSegments();
  const auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment1->id, 48000.0f, 2, std::nullopt);

  craftFactory->detail(fabricator).doWork();
}
