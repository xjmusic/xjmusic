// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <set>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "../../_helper/ContentFixtures.h"
#include "../../_helper/SegmentFixtures.h"

#include "xjmusic/craft/Craft.h"
#include "xjmusic/craft/MacroMainCraft.h"
#include "xjmusic/fabricator/ChainUtils.h"
#include "xjmusic/fabricator/FabricatorFactory.h"
#include "xjmusic/util/CsvUtils.h"

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

class CraftSegmentOutputEncoderTest : public testing::Test {
protected:
  std::unique_ptr<FabricatorFactory> fabricatorFactory;
  std::unique_ptr<SegmentEntityStore> store;
  std::unique_ptr<ContentEntityStore> sourceMaterial;
  const Segment *segment6 = nullptr;

  void SetUp() override {
    store = std::make_unique<SegmentEntityStore>();
    fabricatorFactory = std::make_unique<FabricatorFactory>(store.get());

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    const auto fake = std::make_unique<ContentFixtures>();
    sourceMaterial = std::make_unique<ContentEntityStore>();
    fake->setupFixtureB1(sourceMaterial.get());

    // Chain "Print #2" has 1 initial planned segment
    const auto chain2 = store->put(SegmentFixtures::buildChain(
        "Print #2",
        Chain::Type::Production,
        Chain::State::Fabricate,
        &fake->template1));
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

  void TearDown() override {
    delete sourceMaterial;

      }
};

TEST_F(CraftSegmentOutputEncoderTest, CraftFoundationInitial) {
  const auto fabricator = fabricatorFactory->fabricate(sourceMaterial.get(), segment6->id, std::nullopt);

  MacroMainCraft(fabricator, std::nullopt, {}).doWork();

  const auto result = store->readSegment(segment6->id).value();
  ASSERT_EQ(segment6->id, result->id);
  ASSERT_EQ(Segment::Type::Initial, result->type);
}
