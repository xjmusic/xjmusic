// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <set>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "../../_helper/ContentFixtures.h"
#include "../../_helper/SegmentFixtures.h"

#include "xjmusic/craft/Craft.h"
#include "xjmusic/craft/MacroMainCraft.h"
#include "xjmusic/fabricator/SegmentUtils.h"
#include "xjmusic/util/ValueUtils.h"

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

class CraftFoundationInitialTest : public testing::Test {
protected:
    std::unique_ptr<ContentEntityStore> sourceMaterial;
  std::unique_ptr<SegmentEntityStore> store;
  std::unique_ptr<ContentFixtures> fake;
  const Segment *segment6 = nullptr;

  void SetUp() override {
    store = std::make_unique<SegmentEntityStore>();

    // Mock request via HubClientFactory returns fake generated library of model content
    fake = std::make_unique<ContentFixtures>();
    sourceMaterial = std::make_unique<ContentEntityStore>();
    fake->setupFixtureB1(sourceMaterial.get());

    // Chain "Print #2" has 1 initial planned segment
    const auto chain2 = store->put(SegmentFixtures::buildChain(
        &fake->template1,
        "Print #2",
        Chain::Type::Production,
        Chain::State::Fabricate));
    segment6 = store->put(SegmentFixtures::buildSegment(
        chain2,
        0,
        Segment::State::Planned,
        "C",
        8,
        0.8f,
        120.0f,
        "chain-1-waveform-12345.wav"));
  }
};

TEST_F(CraftFoundationInitialTest, CraftFoundationInitial) {
  const auto retrospective = SegmentRetrospective(store.get(), segment6->id);
  auto fabricator = Fabricator(sourceMaterial.get(), store.get(), &retrospective, segment6->id, std::nullopt);

  MacroMainCraft(&fabricator, std::nullopt, {}).doWork();

  auto result = store->readSegment(segment6->id).value();
  ASSERT_EQ(segment6->id, result->id);
  ASSERT_EQ(Segment::Type::Initial, result->type);
  ASSERT_EQ(16 * ValueUtils::MICROS_PER_MINUTE / 140, result->durationMicros);
  ASSERT_EQ(16, result->total);
  ASSERT_NEAR(0.2, result->intensity, 0.01);
  ASSERT_EQ("G", result->key);
  ASSERT_NEAR(140.0f, result->tempo, 0.01);
  // assert memes
  ASSERT_EQ(
      std::set<std::string>({"TROPICAL", "WILD", "OUTLOOK", "OPTIMISM"}),
      SegmentMeme::getNames(store->readAllSegmentMemes(result->id)));
  // assert chords
  ASSERT_EQ(
      std::set<std::string>({"G", "Ab -"}),
      SegmentChord::getNames(store->readAllSegmentChords(result->id)));
  // assert choices
  auto segmentChoices = store->readAllSegmentChoices(result->id);
  auto macroChoice = SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Macro);
  ASSERT_TRUE(macroChoice.has_value());
  ASSERT_EQ(fake->program4_sequence0_binding0.id, macroChoice.value()->programSequenceBindingId);
  ASSERT_EQ(0, fabricator.getSequenceBindingOffsetForChoice(macroChoice.value()));
  auto mainChoice = SegmentUtils::findFirstOfType(segmentChoices, Program::Type::Main);
  ASSERT_TRUE(mainChoice.has_value());
  ASSERT_EQ(fake->program5_sequence0_binding0.id, mainChoice.value()->programSequenceBindingId);
  ASSERT_EQ(0, fabricator.getSequenceBindingOffsetForChoice(mainChoice.value()));
}
