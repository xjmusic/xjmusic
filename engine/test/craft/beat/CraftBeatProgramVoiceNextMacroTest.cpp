#include <algorithm>
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

class CraftBeatProgramVoiceNextMacroTest : public testing::Test {
protected:
  CraftFactory *craftFactory = nullptr;
  FabricatorFactory *fabricatorFactory = nullptr;
  ContentEntityStore *sourceMaterial = nullptr;
  SegmentEntityStore *store = nullptr;
  ContentFixtures *fake = nullptr;
  Chain *chain1 = nullptr;
  const Segment *segment4 = nullptr;
  InstrumentAudio *audioKick = nullptr;
  InstrumentAudio *audioSnare = nullptr;

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
    setupCustomFixtures();

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
        1,
        Segment::State::Crafting,
        "Db minor",
        64,
        0.85f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892.wav",
        true));
  }

  void TearDown() override {
    delete craftFactory;
    delete fabricatorFactory;
    delete sourceMaterial;
    delete store;
    delete fake;
    delete chain1;
        delete audioKick;
    delete audioSnare;
  }

  /**
   Some custom fixtures for testing

   @return list of all entities
   */
  void setupCustomFixtures() {
    // Instrument "808"
    const auto instrument1 = sourceMaterial->put(ContentFixtures::buildInstrument(&fake->library2, Instrument::Type::Drum, Instrument::Mode::Event, Instrument::State::Published, "808 Drums"));
    sourceMaterial->put(ContentFixtures::buildMeme(instrument1, "heavy"));
    audioKick = sourceMaterial->put(ContentFixtures::buildAudio(instrument1, "Kick", "19801735098q47895897895782138975898.wav", 0.01f, 2.123f, 120.0f, 0.6f, "KICK", "Eb", 1.0f));
    audioSnare = sourceMaterial->put(ContentFixtures::buildAudio(instrument1, "Snare", "a1g9f8u0k1v7f3e59o7j5e8s98.wav", 0.01f, 1.5f, 120.0f, 0.6f, "SNARE", "Ab", 1.0f));
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
        &fake->program3,
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

TEST_F(CraftBeatProgramVoiceNextMacroTest, CraftBeatVoiceNextMacro) {
  insertSegments3and4(true);
  const auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment4->id, std::nullopt);

  craftFactory->beat(fabricator).doWork();

  // assert beat choice
  auto segmentChoices = fabricator->getChoices();

  // Assuming segmentChoices is a std::vector<SegmentChoice>
  const auto beatChoiceIt = std::find_if(segmentChoices.begin(), segmentChoices.end(), [](const SegmentChoice *c) {
    return c->programType == Program::Type::Beat;
  });
  ASSERT_FALSE(beatChoiceIt == segmentChoices.end());
  auto beatChoice = *beatChoiceIt;

  const auto arrangementIt = std::find_if(fabricator->getArrangements().begin(), fabricator->getArrangements().end(), [beatChoice](const SegmentChoiceArrangement *a) {
    return a->segmentChoiceId == beatChoice->id;
  });
  ASSERT_FALSE(arrangementIt == fabricator->getArrangements().end());

  int pickedKick = 0;
  int pickedSnare = 0;
  const auto picks = fabricator->getPicks();
  for (const auto pick: picks) {
    if (pick->instrumentAudioId == audioKick->id)
      pickedKick++;
    if (pick->instrumentAudioId == audioSnare->id)
      pickedSnare++;
  }
  ASSERT_EQ(8, pickedKick);
  ASSERT_EQ(8, pickedSnare);
}

TEST_F(CraftBeatProgramVoiceNextMacroTest, CraftBeatVoiceNextMacro_okIfNoBeatChoice) {
  insertSegments3and4(false);
  const auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment4->id, std::nullopt);

  craftFactory->beat(fabricator).doWork();
}
