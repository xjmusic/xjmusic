// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <set>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "../../_helper/ContentFixtures.h"
#include "../../_helper/SegmentFixtures.h"

#include "xjmusic/craft/Craft.h"
#include "xjmusic/craft/MacroMainCraft.h"
#include "xjmusic/fabricator/FabricatorFactory.h"
#include "xjmusic/fabricator/SegmentUtils.h"
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
   */
TEST(CraftFoundationNextMacroTest, CraftFoundationNextMacro) {
  for (int i = 0; i < TEST_REPEAT_ITERATIONS; i++) {
    auto store = std::make_unique<SegmentEntityStore>();
    auto fabricatorFactory = std::make_unique<FabricatorFactory>(store.get());

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    auto fake = std::make_unique<ContentFixtures>();
    auto sourceMaterial = std::make_unique<ContentEntityStore>();
    fake->setupFixtureB1(sourceMaterial.get());
    fake->setupFixtureB2(sourceMaterial.get());

    // Chain "Test Print #1" has 5 total segments
    const auto chain1 = store->put(
        SegmentFixtures::buildChain("Test Print #1", Chain::Type::Production, Chain::State::Fabricate, &fake->template1,
                                    ""));
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
        "Ab minor",
        64,
        0.30f,
        120.0f,
        "chains-1-segments-9f7s89d8a7892.wav"));
    store->put(SegmentFixtures::buildSegmentChoice(segment3, Program::Type::Macro, &fake->program4_sequence2_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(segment3, Program::Type::Main, &fake->program5_sequence1_binding0));

    // Chain "Test Print #1" has a planned segment
    auto segment4 = store->put(
        SegmentFixtures::buildSegment(chain1, 3, Segment::State::Planned, "C", 8, 0.8f, 120, "chain-1-waveform-12345"));

    auto fabricator = fabricatorFactory->fabricate(sourceMaterial.get(), segment4->id, std::nullopt);

    MacroMainCraft(fabricator, std::nullopt, {}).doWork();

    auto result = store->readSegment(segment4->id).value();
    ASSERT_EQ(Segment::Type::NextMacro, result->type);
    ASSERT_EQ(16 * ValueUtils::MICROS_PER_MINUTE / 140, result->durationMicros);
    ASSERT_EQ(16, result->total);
    ASSERT_NEAR(0.2, result->intensity, 0.01);
    ASSERT_EQ("G -", result->key);
    ASSERT_NEAR(140, result->tempo, 0.01);
    // assert memes
    ASSERT_EQ(
        std::set<std::string>({"REGRET", "CHUNKY", "HINDSIGHT", "TANGY"}),
        SegmentMeme::getNames(store->readAllSegmentMemes(result->id)));
    // assert chords
    ASSERT_EQ(std::set<std::string>({"Ab -", "G -"}),
              SegmentChord::getNames(store->readAllSegmentChords(result->id)));
    // assert choices
    auto segmentChoices =
        store->readAllSegmentChoices(result->id);
    // assert macro choice
    auto macroChoice = SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Macro);
    ASSERT_EQ(fake->program3_sequence0_binding0.id, macroChoice.value()->programSequenceBindingId);
    ASSERT_EQ(0, fabricator->getSequenceBindingOffsetForChoice(macroChoice.value()));
    // assert main choice
    auto mainChoice = SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Main);
    ASSERT_EQ(fake->program15_sequence0_binding0.id, mainChoice.value()->programSequenceBindingId);
    ASSERT_EQ(0, fabricator->getSequenceBindingOffsetForChoice(mainChoice.value()));
  }
}
