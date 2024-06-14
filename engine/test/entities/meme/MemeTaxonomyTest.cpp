// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

#include <gtest/gtest.h>
#include <set>

#include "xjmusic/entities/meme/MemeTaxonomy.h"

using namespace XJ;


/**
 TemplateConfig has Meme categories
 https://github.com/xjmusic/xjmusic/issues/209
 <p>
 A template configuration has a field called `memeTaxonomy` which defines the taxonomy of memes.
 <p>
 For example, this might look like
 <p>
 ```
 memeTaxonomy=CITY[CHICAGO,DENVER,PHILADELPHIA]
 ```
 <p>
 That would tell XJ about the existence of a meme category called City with values `CHICAGO`, `DENVER`, and `PHILADELPHIA`. And these would function as exclusion like numeric memes, e.g. after content having `CHICAGO` is chosen, we can choose nothing with `DENVER` or `PHILADELPHIA`.
 */

TEST(MemeTaxonomyTest, DefaultCategoryName) {
  std::set<MapStringToOneOrManyString> input = {
      {{"memes", std::set<std::string>{"WINTER", "SPRING", "SUMMER", "FALL"}}},
  };

  auto subject = MemeTaxonomy::fromSet(input);

  EXPECT_EQ("CATEGORY", subject.getCategories().begin()->getName());
}

TEST(MemeTaxonomyTest, CategoryHasMemes) {
  std::set<MapStringToOneOrManyString> input = {
      {{"memes", std::set<std::string>{"WINTER", "SPRING", "SUMMER", "FALL"}}},
  };

  MemeTaxonomy subject(input);

  ASSERT_TRUE(subject.getCategories().begin()->hasMemes());
}

TEST(MemeTaxonomyTest, FromStringToString) {
  MemeTaxonomy subject("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]");

  ASSERT_EQ("COLOR[BLUE,GREEN,RED];SIZE[LARGE,MEDIUM,SMALL]", subject.toString());
}

TEST(MemeTaxonomyTest, FromSetToList) {
  std::set<MapStringToOneOrManyString> input = {
      {{"name", "CO111LOR"}, {"memes", std::set<std::string>{"RED ", "GR333EEN", "BLUE#@"}}},
      {{"name", "SIZ$%@E"}, {"memes", std::set<std::string>{"L44A&*(RGE", "MED$IUM", "SMA)(&&LL"}}}
  };

  MemeTaxonomy subject(input);

  std::set<MapStringToOneOrManyString> expected = {
      {{"name", "COLOR"}, {"memes", std::set<std::string>{"RED", "GREEN", "BLUE"}}},
      {{"name", "SIZE"}, {"memes", std::set<std::string>{"LARGE", "MEDIUM", "SMALL"}}}
  };

  ASSERT_EQ(expected, subject.toList());
}

TEST(MemeTaxonomyTest, TestStripNonAlphabetical) {
  MemeTaxonomy subject("COLOR [RED, GREEN, BLUE];    SIZE [LARGE, MEDIUM, SMALL ]");

  ASSERT_EQ("COLOR[BLUE,GREEN,RED];SIZE[LARGE,MEDIUM,SMALL]", subject.toString());
}

TEST(MemeTaxonomyTest, GetCategories) {
  MemeTaxonomy subject("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]");

  std::vector<MemeCategory> categories;
  for (const auto &category: subject.getCategories()) {
    categories.push_back(category);
  }
  std::sort(categories.begin(), categories.end());

  ASSERT_EQ(2, categories.size());
  ASSERT_EQ("COLOR", categories[0].getName());
  ASSERT_TRUE(categories[0].getMemes().find("RED") != categories[0].getMemes().end());
  ASSERT_TRUE(categories[0].getMemes().find("GREEN") != categories[0].getMemes().end());
  ASSERT_TRUE(categories[0].getMemes().find("BLUE") != categories[0].getMemes().end());
  ASSERT_EQ("SIZE", categories[1].getName());
  ASSERT_TRUE(categories[1].getMemes().find("LARGE") != categories[1].getMemes().end());
  ASSERT_TRUE(categories[1].getMemes().find("MEDIUM") != categories[1].getMemes().end());
  ASSERT_TRUE(categories[1].getMemes().find("SMALL") != categories[1].getMemes().end());
}

TEST(MemeTaxonomyTest, IsAllowed) {
  ASSERT_TRUE(MemeTaxonomy("CITY[CHICAGO,DENVER,PHILADELPHIA]").isAllowed({"PEACHES"}));
  ASSERT_TRUE(MemeTaxonomy("CITY[CHICAGO,DENVER,PHILADELPHIA]").isAllowed({"DENVER"}));
  ASSERT_FALSE(MemeTaxonomy("CITY[CHICAGO,DENVER,PHILADELPHIA]").isAllowed({"DENVER", "PHILADELPHIA"}));
  ASSERT_TRUE(MemeTaxonomy("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]").isAllowed({"RED", "LARGE"}));
  ASSERT_TRUE(MemeTaxonomy("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]").isAllowed({"GREEN", "MEDIUM", "PEACHES"}));
  ASSERT_FALSE(MemeTaxonomy("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]").isAllowed({"RED", "BLUE", "LARGE"}));
  ASSERT_FALSE(MemeTaxonomy("COLOR[RED,GREEN,BLUE];SIZE[LARGE,MEDIUM,SMALL]").isAllowed({"GREEN", "MEDIUM", "PEACHES", "SMALL"}));
}

TEST(MemeTaxonomyTest, IsAllowed_alreadyPresentFromTaxonomy) {
  ASSERT_TRUE(MemeTaxonomy("CITY[ABERDEEN,NAGOYA]").isAllowed({"ABERDEEN", "ABERDEEN"}));
}

