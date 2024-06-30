// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/util/ValueUtils.h"

using namespace XJ;


void ASSERT_ARRAY_EQ(const std::vector<int> &vector1, const std::vector<int> &vector2) {
  ASSERT_EQ(vector1.size(), vector2.size());
  for (int i = 0; i < vector1.size(); i++) {
    ASSERT_EQ(vector1[i], vector2[i]);
  }
}

void ASSERT_ARRAY_EQ(const std::vector<std::string> &vector1, const std::vector<std::string> &vector2) {
  ASSERT_EQ(vector1.size(), vector2.size());
  for (int i = 0; i < vector1.size(); i++) {
    ASSERT_EQ(vector1[i], vector2[i]);
  }
}

TEST(ValueUtils, eitherOr_Double) {
  ASSERT_EQ(5.0, ValueUtils::eitherOr(5.0, nan("")));
  ASSERT_EQ(5.0, ValueUtils::eitherOr(nan(""), 5.0));
  ASSERT_EQ(5.0, ValueUtils::eitherOr(5.0, 7.0));
}

TEST(ValueUtils, eitherOr_String) {
  ASSERT_EQ("bing", ValueUtils::eitherOr("bing", ""));
  ASSERT_EQ("bing", ValueUtils::eitherOr("", "bing"));
  ASSERT_EQ("bing", ValueUtils::eitherOr("bing", "schwang"));
}

TEST(ValueUtils, dividedBy) {
  ASSERT_EQ(std::set({2, 8, 23, 31, 40}), ValueUtils::dividedBy(2.0, {4, 16, 62, 80, 46}));
  ASSERT_EQ(std::set({1, 6, 18, 24, 32}), ValueUtils::dividedBy(2.5, {4, 16, 62, 80, 46}));
}

TEST(ValueUtils, ratio) {
  ASSERT_NEAR(0.0, ValueUtils::ratio(0.0, 5.0), 0.01);
  ASSERT_NEAR(0.6, ValueUtils::ratio(3.0, 5.0), 0.01);
}

TEST(ValueUtils, isInteger) {
  ASSERT_EQ(false, ValueUtils::isInteger("a"));
  ASSERT_EQ(false, ValueUtils::isInteger("125a"));
  ASSERT_EQ(true, ValueUtils::isInteger("377"));
  ASSERT_EQ(false, ValueUtils::isInteger("237.1"));
  ASSERT_EQ(true, ValueUtils::isInteger("100000045"));
  ASSERT_EQ(false, ValueUtils::isInteger(" 97"));
  ASSERT_EQ(false, ValueUtils::isInteger(" 27773"));
  ASSERT_EQ(true, ValueUtils::isInteger("32"));
}

TEST(ValueUtils, limitDecimalPrecision) {
  ASSERT_NEAR(1.25, ValueUtils::limitDecimalPrecision(1.2545897987), 0.0000001);
}

TEST(ValueUtils, k) {
  ASSERT_EQ("128k", ValueUtils::k(128000));
}

TEST(ValueUtils, randomFrom) {
  std::vector<std::string> input = {"A", "B", "C"};

  const auto result = ValueUtils::randomFrom(input);

  ASSERT_TRUE(std::find(input.begin(), input.end(), result) != input.end());
}

TEST(ValueUtils, randomFrom_multiple) {
  std::vector<std::string> input = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

  const auto result = ValueUtils::randomFrom(input, 4);

  for (auto &s: result) {
    ASSERT_TRUE(std::find(input.begin(), input.end(), s) != input.end());
  }
}

TEST(ValueUtils, randomFrom_notEnough) {
  std::vector<std::string> input = {"A", "B", "C"};

  const auto result = ValueUtils::randomFrom(input, 5);

  for (auto &s: result) {
    ASSERT_TRUE(std::find(input.begin(), input.end(), s) != input.end());
  }
  ASSERT_EQ(3, input.size());
}

TEST(ValueUtils, randomFrom_noSource) {
  ASSERT_EQ(0, ValueUtils::randomFrom(std::vector<std::string>{}, 5).size());
}

TEST(ValueUtils, randomFrom_targetZero) {
  const std::vector<std::string> input = {"A", "B", "C"};

  ASSERT_EQ(0, ValueUtils::randomFrom(input, 0).size());
}

TEST(ValueUtils, randomFrom_zeroFromNoSource) {
  ASSERT_EQ(0, ValueUtils::randomFrom(std::vector<std::string>{}, 0).size());
}


TEST(ValueUtils, gcd) {
  ASSERT_EQ(4, ValueUtils::gcd(4, 12));
  ASSERT_EQ(3, ValueUtils::gcd(9, 12));
}

TEST(ValueUtils, factors) {
  ASSERT_ARRAY_EQ(std::vector{2, 3, 4}, ValueUtils::factors(12, std::vector{2, 3, 4, 5, 7}));
  ASSERT_ARRAY_EQ(std::vector{2, 3, 4, 5}, ValueUtils::factors(60, std::vector{2, 3, 4, 5, 7}));
  ASSERT_ARRAY_EQ(std::vector{2, 3, 5, 7}, ValueUtils::factors(210, std::vector{2, 3, 4, 5, 7}));
}

TEST(ValueUtils, div) {
  ASSERT_EQ(2, ValueUtils::subDiv(2, 3));
  ASSERT_EQ(4, ValueUtils::subDiv(4, 3));
  ASSERT_EQ(4, ValueUtils::subDiv(12, 3));
  ASSERT_EQ(3, ValueUtils::subDiv(12, 4));
  ASSERT_EQ(4, ValueUtils::subDiv(16, 4));
  ASSERT_EQ(4, ValueUtils::subDiv(24, 3));
  ASSERT_EQ(3, ValueUtils::subDiv(24, 4));
  ASSERT_EQ(4, ValueUtils::subDiv(48, 3));
  ASSERT_EQ(3, ValueUtils::subDiv(48, 4));
  ASSERT_EQ(4, ValueUtils::subDiv(64, 4));
}

TEST(ValueUtils, multipleFloor) {
  ASSERT_EQ(0, ValueUtils::multipleFloor(12, 11));
  ASSERT_EQ(12, ValueUtils::multipleFloor(12, 20));
  ASSERT_EQ(36, ValueUtils::multipleFloor(12, 38));
  ASSERT_EQ(0, ValueUtils::multipleFloor(16, 11));
  ASSERT_EQ(16, ValueUtils::multipleFloor(16, 20));
  ASSERT_EQ(32, ValueUtils::multipleFloor(16, 34));
}

TEST(ValueUtils, interpolate) {
  ASSERT_NEAR(10, ValueUtils::interpolate(10, 20, 0, 1.0), 0.000001);
  ASSERT_NEAR(20, ValueUtils::interpolate(10, 20, 1, 1.0), 0.000001);
  ASSERT_NEAR(15, ValueUtils::interpolate(10, 20, 1, 0.5), 0.000001);
}

TEST(ValueUtils, enforceMaxStereo) {
  ASSERT_THROW(ValueUtils::enforceMaxStereo(3), std::runtime_error);
}

TEST(ValueUtils, withIdsRemoved) {
  const std::vector<UUID> input = {
      "c72084a7-797e-46a0-bf1d-791a9529c166",
      "acc9a249-4925-4cc8-98fd-c7c6fe1a3562",
      "0e2ffe25-4118-4a23-86e6-7d9e3570d994",
      "32afa764-8ee3-4348-a25c-8f3203242c17"
  };

  ASSERT_EQ(2, ValueUtils::withIdsRemoved(input, 2).size());
}

TEST(ValueUtils, emptyZero) {
  ASSERT_EQ("12", ValueUtils::emptyZero(12));
  ASSERT_EQ("-12", ValueUtils::emptyZero(-12));
  ASSERT_EQ("", ValueUtils::emptyZero(0));
}

TEST(ValueUtils, last) {
  const std::vector<std::string> input = {"One", "Two", "Three"};

  ASSERT_ARRAY_EQ(std::vector<std::string>{}, ValueUtils::last(-1, input));
  ASSERT_ARRAY_EQ(std::vector<std::string>{}, ValueUtils::last(0, input));
  ASSERT_ARRAY_EQ(std::vector<std::string>{"Three"}, ValueUtils::last(1, input));
  ASSERT_ARRAY_EQ(std::vector<std::string>{"Two", "Three"}, ValueUtils::last(2, input));
  ASSERT_ARRAY_EQ(std::vector<std::string>{"One", "Two", "Three"}, ValueUtils::last(3, input));
  ASSERT_ARRAY_EQ(std::vector<std::string>{"One", "Two", "Three"}, ValueUtils::last(4, input));
}

TEST(ValueUtils, constants) {
  ASSERT_EQ(24 * 60 * 60, ValueUtils::SECONDS_PER_DAY);
}

TEST(ValueUtils, getKeyOfHighestValue) {
  std::string uuidA = "c72084a7-797e-46a0-bf1d-791a9529c166";
  std::string uuidB = "acc9a249-4925-4cc8-98fd-c7c6fe1a3562";
  std::string uuidC = "0e2ffe25-4118-4a23-86e6-7d9e3570d994";
  ASSERT_EQ(uuidB, ValueUtils::getKeyOfHighestValue(std::map<UUID, int>{{uuidA, 1},
                                                                        {uuidB, 4},
                                                                        {uuidC, 3}})
                                                                        .value());
}

TEST(ValueUtils, roundToNearest) {
  ASSERT_EQ(255, ValueUtils::roundToNearest(5, 254));
  ASSERT_EQ(250, ValueUtils::roundToNearest(10, 254));
  ASSERT_EQ(260, ValueUtils::roundToNearest(10, 256));
  ASSERT_EQ(300, ValueUtils::roundToNearest(100, 254));
}

TEST(ValueUtils, microsPerMinutes) {
  ASSERT_EQ(60000000, ValueUtils::MICROS_PER_MINUTE);
}

TEST(ValueUtils, microsPerSeconds) {
  ASSERT_EQ(1000000, ValueUtils::MICROS_PER_SECOND);
}

TEST(ValueUtils, nanosPerSeconds) {
  ASSERT_EQ(1000000000, ValueUtils::NANOS_PER_SECOND);
}
