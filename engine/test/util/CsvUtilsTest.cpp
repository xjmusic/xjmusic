// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include <vector>
#include <string>

#include "xjmusic/util/CsvUtils.h"

using namespace XJ;


TEST(CsvUtilsTest, split) {
  const std::vector<std::string> expected = {"one", "two", "three"};

  const auto actual = CsvUtils::split("one,two,three");

  ASSERT_EQ(expected, actual);
}


TEST(CsvUtilsTest, split_empty) {
  ASSERT_EQ(std::vector<std::string>{}, CsvUtils::split(""));
}


TEST(CsvUtilsTest, split_alsoTrims) {
  const std::vector<std::string> expected = {"one", "two", "three"};

  const auto actual = CsvUtils::split("one, two, three");

  ASSERT_EQ(expected, actual);
}


TEST(CsvUtilsTest, splitProperSlug) {
  const std::vector<std::string> expected = {"One", "Two", "Three"};

  const auto actual = CsvUtils::splitProperSlug("one,two,three");

  ASSERT_EQ(expected, actual);
}


TEST(CsvUtilsTest, join) {
  const std::string expected = "one, two, three";

  const auto actual = CsvUtils::join({"one", "two", "three"});

  ASSERT_EQ(expected, actual);
}


TEST(CsvUtilsTest, from_keyValuePairs) {
  const auto result = CsvUtils::from({
                                   {"one",   "1"},
                                   {"two",   "2"},
                                   {"three", "3"}
                               });

  ASSERT_EQ(21, result.length());
  ASSERT_TRUE(result.find("one=1") != std::string::npos);
  ASSERT_TRUE(result.find("two=2") != std::string::npos);
  ASSERT_TRUE(result.find("three=3") != std::string::npos);
}


TEST(CsvUtilsTest, prettyFrom) {
  const std::string expected = "One, Two, or Three";

  const auto actual = CsvUtils::prettyFrom({"One", "Two", "Three"}, "or");

  ASSERT_EQ(expected, actual);
}

TEST(StringUtilsTest, toProperCsvAnd) {
  EXPECT_EQ("One", CsvUtils::toProperCsvAnd({"One"}));
  EXPECT_EQ("One and Two", CsvUtils::toProperCsvAnd({"One", "Two"}));
  EXPECT_EQ("One, Two, and Three", CsvUtils::toProperCsvAnd({"One", "Two", "Three"}));
}

TEST(StringUtilsTest, toProperCsvOr) {
  EXPECT_EQ("One", CsvUtils::toProperCsvOr({"One"}));
  EXPECT_EQ("One or Two", CsvUtils::toProperCsvOr({"One", "Two"}));
  EXPECT_EQ("One, Two, or Three", CsvUtils::toProperCsvOr({"One", "Two", "Three"}));
}

TEST(StringUtilsTest, toProperCsv) {
  EXPECT_EQ("One", CsvUtils::toProperCsv({"One"}, "or maybe"));
  EXPECT_EQ("One or maybe Two", CsvUtils::toProperCsv({"One", "Two"}, "or maybe"));
  EXPECT_EQ("One, Two, or maybe Three", CsvUtils::toProperCsv({"One", "Two", "Three"}, "or maybe"));
}