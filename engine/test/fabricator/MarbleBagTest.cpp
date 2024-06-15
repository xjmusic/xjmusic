// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

#include <gtest/gtest.h>
#include "spdlog/spdlog.h"

#include "xjmusic/fabricator/MarbleBag.h"
#include "xjmusic/fabricator/FabricationException.h"

using namespace XJ;

/**
 * The Marble Bag: a bag of marbles
 * Because choices should be random https://github.com/xjmusic/xjmusic/issues/291
 */

class MarbleBagTest : public ::testing::Test {
protected:
  UUID frogId = ContentEntity::randomUUID();
  UUID bearId = ContentEntity::randomUUID();
  UUID zebraId = ContentEntity::randomUUID();
};

/**
 * adds all marbles from an object containing other marbles
 */
TEST_F(MarbleBagTest, addAll) {
  auto bag = MarbleBag::empty();
  bag.addAll(1, {
      {frogId,  1000},
      {bearId,  30},
      {zebraId, 5}
  });
  ASSERT_EQ(1035, bag.size());
}

/**
 * adds marbles to bag, picks 100 times and asserts allowed results
 */
TEST_F(MarbleBagTest, add_pick) {
  auto bag = MarbleBag::empty();
  bag.add(1, frogId, 1000);
  bag.add(1, bearId, 30);
  bag.add(1, zebraId, 5);
  spdlog::info("will pick 100 marbles from {}", bag.toString());
  auto result = MarbleBag::empty();
  auto allowed = std::set<UUID>{frogId, bearId, zebraId};
  for (auto i = 0; i < 100; i++) {
    auto pick = bag.pick();
    ASSERT_FALSE(allowed.find(pick) == allowed.end());
    result.add(1, pick);
  }
  spdlog::info("picked {}", result.toString());
}

/**
 * adds marbles to bag in multiple phases, picks 100 times and asserts allowed results
 * <p>
 * Marble bag has phases https://github.com/xjmusic/xjmusic/issues/291
 * <p>
 * This will consolidate the logic around "choose this if available, else that, else that"
 * XJ’s marble bag is actually divided into phases. When a marble is put into the bag, it is assigned a phase.
 * For example, if the phase 1 bag contains any marbles, we will pick from only the phase 1 bag and skip phases 2 and beyond.
 * This supports functionality such as “XJ always chooses a directly-bound program or instrument when available”
 */
TEST_F(MarbleBagTest, pick_phaseLowerPreferred) {
  auto bag = MarbleBag::empty();
  bag.add(1, frogId, 1000);
  bag.add(1, bearId, 30);
  bag.add(2, zebraId, 5);
  spdlog::info("will pick 100 marbles from {}", bag.toString());
  auto allowed = std::set<UUID>{frogId, bearId};
  for (auto i = 0; i < 100; i++) {
    auto pick = bag.pick();
    ASSERT_FALSE(allowed.find(pick) == allowed.end());
  }
}

TEST_F(MarbleBagTest, pick_skipEmptyPhases) {
  auto bag = MarbleBag::empty();
  bag.add(4, frogId, 1000);
  bag.add(5, bearId, 30);
  bag.add(6, zebraId, 5);
  spdlog::info("will pick 100 marbles from {}", bag.toString());
  auto allowed = std::set<UUID>{frogId, bearId};
  for (auto i = 0; i < 100; i++) {
    auto pick = bag.pick();
    ASSERT_FALSE(allowed.find(pick) == allowed.end());
  }
}

/**
 * tells us how many marbles are in the bag
 */
TEST_F(MarbleBagTest, size) {
  auto bag = MarbleBag::empty();
  bag.add(1, frogId, 1000);
  bag.add(1, bearId, 30);
  bag.add(1, zebraId, 5);
  ASSERT_EQ(1035, bag.size());
}

TEST_F(MarbleBagTest, isEmpty) {
  auto bag = MarbleBag::empty();
  ASSERT_TRUE(bag.isEmpty());
  bag.add(1, frogId, 1000);
  ASSERT_FALSE(bag.isEmpty());
}

TEST_F(MarbleBagTest, isEmpty_notIfAnyPhasesHaveMarbles) {
  auto bag = MarbleBag::empty();
  ASSERT_TRUE(bag.isEmpty());
  bag.add(1, bearId, 0);
  bag.add(2, frogId, 1000);
  ASSERT_FALSE(bag.isEmpty());
}

TEST_F(MarbleBagTest, quickPick) {
  int pick = MarbleBag::quickPick(100);
  ASSERT_TRUE(pick);
  ASSERT_TRUE(0 <= pick && pick < 100);
}

TEST_F(MarbleBagTest, quickPick_exceptionIfZero) {
  ASSERT_THROW(MarbleBag::quickPick(0), FabricationException);
}