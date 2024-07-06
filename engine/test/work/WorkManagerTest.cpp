// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.


#include <set>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "../_helper/ContentFixtures.h"

#include "xjmusic/craft/Craft.h"
#include "xjmusic/fabricator/SegmentUtils.h"
#include "xjmusic/work/WorkManager.h"

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
  long long startTime = EntityUtils::currentTimeMillis();
  std::unique_ptr<SegmentEntityStore> store;
  std::unique_ptr<ContentEntityStore> content;
  std::unique_ptr<WorkManager> subject;
  std::unique_ptr<ContentFixtures> fake;

  void SetUp() override {
    content = std::make_unique<ContentEntityStore>();
    fake = std::make_unique<ContentFixtures>();
    fake->project1 = ContentFixtures::buildProject("fish");
    fake->library1 = ContentFixtures::buildLibrary(&fake->project1, "test");
    fake->generateFixtures(content.get(), GENERATED_FIXTURE_COMPLEXITY);

    // Manipulate the underlying entity store; reset before each test
    store = std::make_unique<SegmentEntityStore>();

    // subject
    auto settings = WorkSettings();
    Template tmpl = **content->getTemplates().begin();
    tmpl.shipKey = "complex_library_test";
    tmpl.config = TemplateConfig("outputEncoding=\"PCM_SIGNED\"\noutputContainer = \"WAV\"\ndeltaArcEnabled = false\n");
    content->put(tmpl);
    subject = std::make_unique<WorkManager>(store.get(), content.get(), settings);
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
  [[nodiscard]] bool hasSegmentsDubbedPastMinimumOffset() const {
    const auto segment = SegmentUtils::getLastCrafted(store->readAllSegments());
    return segment.has_value() && segment.value()->id >= MARATHON_NUMBER_OF_SEGMENTS;
  }
};

TEST_F(WorkManagerTest, HasSegmentsDubbedPastMinimumOffset) {
  subject->start();
  unsigned long long atChainMicros = 0;
  while (!hasSegmentsDubbedPastMinimumOffset() && isWithinTimeLimit()) {
    subject->runCycle(atChainMicros);
    spdlog::info("Ran cycle at {}", atChainMicros);
    atChainMicros += MICROS_PER_CYCLE;
  }

  // assertions
  ASSERT_TRUE(hasSegmentsDubbedPastMinimumOffset());
}
