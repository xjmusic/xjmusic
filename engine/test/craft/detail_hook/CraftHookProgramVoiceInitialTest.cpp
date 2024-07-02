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

class CraftHookProgramVoiceInitialTest : public testing::Test {
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

    // force known hook selection by destroying program 35
    // Mock request via HubClientFactory returns fake generated library of model content
    fake = new ContentFixtures();
    sourceMaterial = new ContentEntityStore();
    fake->setupFixtureB1(sourceMaterial, false);
    fake->setupFixtureB3(sourceMaterial);

    // Chain "Print #2" has 1 initial segment in crafting state - Foundation is complete
    chain2 = new Chain();
    chain2->id = EntityUtils::computeUniqueId();
    chain2->name = "Print #2";
    chain2->templateConfig = TemplateConfig::DEFAULT;
    chain2->type = Chain::Type::Production;
    chain2->state = Chain::State::Fabricate;
    store->put(*chain2);
  }

  void TearDown() override {
    delete craftFactory;
    delete fabricatorFactory;
    delete sourceMaterial;
    delete store;
    delete fake;
    delete chain2;
      }

  /**
 Insert fixture segment 6, including the hook choice only if specified
 */
  void insertSegment() {
    segment0 = store->put(SegmentFixtures::buildSegment(
        chain2,
        0,
        Segment::State::Crafting,
        "D Major",
        32,
        0.55f,
        130.0f,
        "chains-1-segments-9f7s89d8a7892.wav"));
    store->put(SegmentFixtures::buildSegmentChoice(segment0, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED, &fake->program4, &fake->program4_sequence0_binding0));
    store->put(SegmentFixtures::buildSegmentChoice(segment0, SegmentChoice::DELTA_UNLIMITED, SegmentChoice::DELTA_UNLIMITED, &fake->program5, &fake->program5_sequence0_binding0));
    for (const std::string memeName: std::set<std::string>({"Special", "Wild", "Pessimism", "Outlook"}))
      store->put(SegmentFixtures::buildSegmentMeme(segment0, memeName));

    store->put(SegmentFixtures::buildSegmentChord(segment0, 0.0f, "C minor"));
    store->put(SegmentFixtures::buildSegmentChord(segment0, 8.0f, "Db minor"));
  }
};

TEST_F(CraftHookProgramVoiceInitialTest, CraftHookVoiceInitial) {
  insertSegment();

  const auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment0->id, std::nullopt);

  craftFactory->detail(fabricator).doWork();
}

TEST_F(CraftHookProgramVoiceInitialTest, CraftHookVoiceInitial_okWhenNoHookChoice) {
  insertSegment();
  const auto fabricator = fabricatorFactory->fabricate(sourceMaterial, segment0->id, std::nullopt);

  craftFactory->detail(fabricator).doWork();
}
