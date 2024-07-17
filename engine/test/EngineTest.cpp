// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <set>

#include <gmock/gmock.h>
#include <gtest/gtest.h>

#include "./_helper/ContentFixtures.h"

#include "xjmusic/Engine.h"
#include "xjmusic/craft/Craft.h"
#include "xjmusic/fabricator/SegmentUtils.h"
#include "xjmusic/work/WorkManager.h"

static std::string ENGINE_TEST_PROJECT_PATH = "_data/test_project/TestProject.xj";

// NOLINTNEXTLINE
using ::testing::_;
using testing::Return;
using testing::ReturnRef;

using namespace XJ;

class XJEngineTest : public testing::Test {
protected:
  int MARATHON_NUMBER_OF_SEGMENTS = 50;
  long MICROS_PER_CYCLE = 1000000;
  long long MAXIMUM_TEST_WAIT_SECONDS = 10 * MARATHON_NUMBER_OF_SEGMENTS;
  long long MILLIS_PER_SECOND = 1000;
  int GENERATED_FIXTURE_COMPLEXITY = 3;
  long long startTime = EntityUtils::currentTimeMillis();
  Engine *subject = nullptr;

  void SetUp() override {
    // subject
    auto [controlMode, craftAheadSeconds, dubAheadSeconds, persistenceWindowSeconds] = WorkSettings();
    subject = new Engine(
        ENGINE_TEST_PROJECT_PATH,
        controlMode,
        craftAheadSeconds,
        dubAheadSeconds,
        persistenceWindowSeconds);
  }

  void TearDown() override {
    delete subject;
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
    const auto segment = SegmentUtils::getLastCrafted(subject->getSegmentStore()->readAllSegments());
    return segment.has_value() && segment.value()->id >= MARATHON_NUMBER_OF_SEGMENTS;
  }
};

TEST_F(XJEngineTest, ReadsAndRunsProjectFromDisk) {
  auto memeTaxonomy = subject->getMemeTaxonomy();
  ASSERT_TRUE(memeTaxonomy.has_value());
  auto categories = memeTaxonomy.value().getCategories();
  ASSERT_FALSE(categories.empty());
  ASSERT_EQ(categories.size(), 2);

  const auto tmpl = subject->getProjectContent()->getFirstTemplate();
  ASSERT_TRUE(tmpl.has_value());

  subject->start(tmpl.value()->id);
  unsigned long long atChainMicros = 0;
  while (!hasSegmentsDubbedPastMinimumOffset() && isWithinTimeLimit()) {
    auto audios = subject->runCycle(atChainMicros);
    ASSERT_FALSE(audios.empty());
    for (auto audio: audios) {
      // assert that this audio file exists
      ASSERT_TRUE(std::filesystem::exists(subject->getPathToBuildDirectory() / audio.getAudio()->waveformKey));
    }
    spdlog::info("Ran cycle at {}", atChainMicros);
    atChainMicros += MICROS_PER_CYCLE;
  }

  // assertions
  ASSERT_TRUE(hasSegmentsDubbedPastMinimumOffset());
}
