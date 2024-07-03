// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.


#include <set>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "../_helper/ContentFixtures.h"
#include "../_helper/YamlTest.h"

#include "xjmusic/content/ContentEntityStore.h"
#include "xjmusic/craft/Craft.h"
#include "xjmusic/fabricator/ChainUtils.h"
#include "xjmusic/fabricator/FabricatorFactory.h"
#include "xjmusic/fabricator/SegmentUtils.h"
#include "xjmusic/segment/SegmentEntityStore.h"
#include "xjmusic/util/ValueUtils.h"

#include <xjmusic/work/WorkManager.h>

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

class WorkManagerTest : public testing::Test {
protected:
  int MARATHON_NUMBER_OF_SEGMENTS = 50;
  long MICROS_PER_CYCLE = 1000000;
  long long MAXIMUM_TEST_WAIT_SECONDS = 10 * MARATHON_NUMBER_OF_SEGMENTS;
  long long MILLIS_PER_SECOND = 1000;
  int GENERATED_FIXTURE_COMPLEXITY = 3;
  long WORK_CYCLE_MILLIS = 120;
  long long startTime = EntityUtils::currentTimeMillis();
  SegmentEntityStore *store = nullptr;
  ContentEntityStore *content = nullptr;
  WorkManager *work = nullptr;
  ContentFixtures *fake = nullptr;

  void SetUp() override {
    content = new ContentEntityStore();
    fake = new ContentFixtures();
    fake->project1 = ContentFixtures::buildProject("fish");
    fake->library1 = ContentFixtures::buildLibrary(&fake->project1, "test");
    fake->generateFixtures(content, GENERATED_FIXTURE_COMPLEXITY);

    Template tmpl = **content->getTemplates().begin();
    tmpl.shipKey = "complex_library_test";
    tmpl.config = "outputEncoding=\"PCM_SIGNED\"\noutputContainer = \"WAV\"\ndeltaArcEnabled = false\n";
    content->put(tmpl);

    // Manipulate the underlying entity store; reset before each test
    store = new SegmentEntityStore();
    const auto fabricatorFactory = new FabricatorFactory(store);

    // Dependencies
    const auto
    // work
    work = new WorkManager(fabricatorFactory, store);
    auto settings = WorkSettings();
    settings.inputTemplate = tmpl;
    work->start(content, settings);
  }

  void TearDown() override {
    delete store;
    delete fake;
    delete content;
    delete work;
  }

  /**
   Whether this test is within the time limit

   @return true if within time limit
   */
  bool isWithinTimeLimit() {
    if (MAXIMUM_TEST_WAIT_SECONDS * MILLIS_PER_SECOND > EntityUtils::currentTimeMillis() - startTime)
      return true;
    spdlog::error("EXCEEDED TEST TIME LIMIT OF {} SECONDS", MAXIMUM_TEST_WAIT_SECONDS);
    return false;
  }

  /**
   Does the specified chain contain at least N segments?

   @return true if it has at least N segments
   */
  bool hasSegmentsDubbedPastMinimumOffset() const {
    const auto segment = SegmentUtils::getLastCrafted(store->readAllSegments());
    return segment.has_value() && segment.value()->id >= MARATHON_NUMBER_OF_SEGMENTS;
  }
};

TEST_F(WorkManagerTest, HasSegmentsDubbedPastMinimumOffset) {
  unsigned long long atChainMicros = 0;
  while (!hasSegmentsDubbedPastMinimumOffset() && isWithinTimeLimit()) {
    work->runCycle(atChainMicros);
    spdlog::info("Ran cycle at {}", atChainMicros);
    atChainMicros += MICROS_PER_CYCLE;
  }

  // assertions
  ASSERT_TRUE(hasSegmentsDubbedPastMinimumOffset());
}
