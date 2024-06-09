// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include "xjmusic/entities/content/MemeTaxonomy.h"
#include <gtest/gtest.h>

using namespace XJ;

TEST(MemeTaxonomyTest, DefaultCategoryName) {
  std::vector<std::map<std::string, std::variant<std::string, std::vector<std::string>>>> input = {
      {
          {"memes", std::vector<std::string>{"WINTER", "SPRING", "SUMMER", "FALL"}}
      }
  };
  MemeTaxonomy subject(input);

  ASSERT_EQ("CATEGORY", subject.getCategories()[0].getName());
}

TEST(MemeTaxonomyTest, CategoryHasMemes) {
  std::vector<std::map<std::string, std::variant<std::string, std::vector<std::string>>>> input = {
      {
          {"memes", std::vector<std::string>{"WINTER", "SPRING", "SUMMER", "FALL"}}
      }
  };
  MemeTaxonomy subject(input);

  ASSERT_TRUE(subject.getCategories()[0].hasMemes());
}

TEST(MemeTaxonomyTest, FromString_toString) {
  MemeTaxonomy subject("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]");

  ASSERT_EQ("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]", subject.toString());
}

TEST(MemeTaxonomyTest, FromListOfMaps_toListOfMaps) {
  std::vector<std::map<std::string, std::variant<std::string, std::vector<std::string>>>> input = {
      {{"name", "COLOR"}, {"memes", std::vector<std::string>{"RED",   "GREEN",  "BLUE"}}},
      {{"name", "SIZE"},  {"memes", std::vector<std::string>{"LARGE", "MEDIUM", "SMALL"}}}
  };

  MemeTaxonomy subject(input);

  std::vector<std::map<std::string, std::variant<std::string, std::vector<std::string>>>> expected = {
      {{"name", "COLOR"}, {"memes", std::vector<std::string>{"RED",   "GREEN",  "BLUE"}}},
      {{"name", "SIZE"},  {"memes", std::vector<std::string>{"LARGE", "MEDIUM", "SMALL"}}}
  };
  ASSERT_EQ(expected, subject.toList());
}

TEST(MemeTaxonomyTest, TestStripNonAlphabetical) {
  MemeTaxonomy subject("COLOR [RED, GREEN, BLUE];    SIZE [LARGE, MEDIUM, SMALL ]");

  ASSERT_EQ("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]", subject.toString());
}

TEST(MemeTaxonomyTest, GetCategories) {
  MemeTaxonomy subject("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]");

  ASSERT_EQ(2, subject.getCategories().size());
  ASSERT_EQ("COLOR", subject.getCategories()[0].getName());
  ASSERT_EQ("RED", subject.getCategories()[0].getMemes()[0]);
  ASSERT_EQ("GREEN", subject.getCategories()[0].getMemes()[1]);
  ASSERT_EQ("BLUE", subject.getCategories()[0].getMemes()[2]);
  ASSERT_EQ("SIZE", subject.getCategories()[1].getName());
  ASSERT_EQ("LARGE", subject.getCategories()[1].getMemes()[0]);
  ASSERT_EQ("MEDIUM", subject.getCategories()[1].getMemes()[1]);
  ASSERT_EQ("SMALL", subject.getCategories()[1].getMemes()[2]);
}

TEST(MemeTaxonomyTest, IsAllowed) {
  ASSERT_TRUE(MemeTaxonomy("CITY[CHICAGO,DENVER,PHILADELPHIA]").isAllowed({"PEACHES"}));
  ASSERT_TRUE(MemeTaxonomy("CITY[CHICAGO,DENVER,PHILADELPHIA]").isAllowed({"DENVER"}));
  ASSERT_FALSE(MemeTaxonomy("CITY[CHICAGO,DENVER,PHILADELPHIA]").isAllowed({"DENVER", "PHILADELPHIA"}));
  ASSERT_TRUE(MemeTaxonomy("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]").isAllowed({"RED", "LARGE"}));
  ASSERT_TRUE(MemeTaxonomy("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]").isAllowed({"GREEN", "MEDIUM", "PEACHES"}));
  ASSERT_FALSE(MemeTaxonomy("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]").isAllowed({"RED", "BLUE", "LARGE"}));
  ASSERT_FALSE(MemeTaxonomy("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]").isAllowed(
      {"GREEN", "MEDIUM", "PEACHES", "SMALL"}));
}

TEST(MemeTaxonomyTest, IsAllowed_alreadyPresentFromTaxonomy) {
  ASSERT_TRUE(MemeTaxonomy("CITY[ABERDEEN,NAGOYA]").isAllowed({"ABERDEEN", "ABERDEEN"}));
}