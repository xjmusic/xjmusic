#include <gmock/gmock.h>
#include <gtest/gtest.h>
#include <set>
#include <spdlog/spdlog.h>
#include <vector>

#include "../../_helper/ContentFixtures.h"
#include "../../_helper/SegmentFixtures.h"
#include "../../_helper/YamlTest.h"

#include "xjmusic/craft/Craft.h"
#include "xjmusic/craft/CraftFactory.h"
#include "xjmusic/fabricator/ChainUtils.h"
#include "xjmusic/fabricator/FabricatorFactory.h"
#include "xjmusic/util/CsvUtils.h"
#include "xjmusic/util/ValueUtils.h"

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

static int TEST_REPEAT_ITERATIONS = 14;

/**
   Test to ensure that the following Macro-Program is based on its first sequence-binding meme
   matching the last sequence-binding meme of the preceding Macro-Program
   <p>
   Segment memes expected to be taken directly of sequence_pattern binding https://github.com/xjmusic/xjmusic/issues/298
   Macro program sequence should advance after each main program https://github.com/xjmusic/xjmusic/issues/299
   */
TEST(CraftSegmentPatternMemeTest, CraftSegment) {
  for (int i = 1; i <= TEST_REPEAT_ITERATIONS; i++) {
    spdlog::info("ATTEMPT NUMBER {}", i);

    const auto craftFactory = new CraftFactory();
    const auto store = new SegmentEntityStore();
    const auto fabricatorFactory = new FabricatorFactory(store);

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    const auto fake = new ContentFixtures();
    const auto sourceMaterial = new ContentEntityStore();
    fake->setupFixtureB1(sourceMaterial);
    fake->setupFixtureB2(sourceMaterial);

    // Chain "Test Print #1" has 5 total segments
    const auto chain = store->put(SegmentFixtures::buildChain("Test Print #1", Chain::Type::Production, Chain::State::Fabricate, &fake->template1, ""));

    // Preceding Segment
    const auto previousSegment = store->put(SegmentFixtures::buildSegment(
        chain,
        1,
        Segment::State::Crafting,
        "F Major",
        64,
        0.30f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892.wav"));
    store->put(SegmentFixtures::buildSegmentChoice(previousSegment, Program::Type::Macro, &fake->program4_sequence1_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(previousSegment, Program::Type::Main, &fake->program5_sequence1_binding0));

    // Following Segment
    const auto segment = store->put(SegmentFixtures::buildSegment(chain, 2, Segment::State::Planned, "C", 8, 0.8f, 120, "chain-1-waveform-12345"));

    craftFactory->macroMain(fabricatorFactory->fabricate(sourceMaterial, segment->id, 48000.0f, 2, std::nullopt), std::nullopt, {}).doWork();

    const auto result = store->readSegment(segment->id).value();
    ASSERT_EQ(Segment::Type::NextMacro, result->type);
    ASSERT_EQ(std::set<std::string>({"REGRET", "HINDSIGHT", "CHUNKY", "TANGY"}),
              SegmentMeme::getNames(store->readAllSegmentMemes(result->id)));
  }
}
