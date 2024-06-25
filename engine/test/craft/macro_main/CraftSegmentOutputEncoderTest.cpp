#include <gmock/gmock.h>
#include <gtest/gtest.h>
#include <set>
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
using ::testing::Return;
using ::testing::ReturnRef;

using namespace XJ;

class CraftSegmentOutputEncoderTest : public ::testing::Test {
protected:
  CraftFactory *craftFactory = nullptr;
  FabricatorFactory *fabricatorFactory = nullptr;
  SegmentEntityStore *store = nullptr;
  ContentEntityStore *sourceMaterial = nullptr;
  Segment *segment6 = nullptr;

  void SetUp() override {
    craftFactory = new CraftFactory();
    store = new SegmentEntityStore();
    fabricatorFactory = new FabricatorFactory(store);

    // Manipulate the underlying entity store; reset before each test
    store->clear();

    // Mock request via HubClientFactory returns fake generated library of model content
    const auto fake = new ContentFixtures();
    sourceMaterial = new ContentEntityStore();
    fake->setupFixtureB1(sourceMaterial);

    // Chain "Print #2" has 1 initial planned segment
    const auto chain2 = store->put(SegmentFixtures::buildChain(
        &fake->project1,
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
    delete fabricatorFactory;
    delete store;
    delete craftFactory;
    delete segment6;
  }
};

TEST_F(CraftSegmentOutputEncoderTest, CraftFoundationInitial) {
  const auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment6->id, 48000.0f, 2, std::nullopt);

  craftFactory->macroMain(fabricator, std::nullopt, {}).doWork();

  const auto result = store->readSegment(segment6->id).value();
  ASSERT_EQ(segment6->id, result->id);
  ASSERT_EQ(Segment::Type::Initial, result->type);
}
