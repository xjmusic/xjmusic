// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

#include <gtest/gtest.h>
#include <set>

#include "xjmusic/entities/meme/MemeStack.h"

using namespace XJ;

/**
 Basics: all memes are allowed
 */
TEST(MemeStackTest, IsAllowed) {
  ASSERT_TRUE(MemeStack::from(MemeTaxonomy::empty(), {"APPLES", "ORANGES"}).isAllowed({"APPLES"}));
  ASSERT_TRUE(MemeStack::from(MemeTaxonomy::empty(), {"APPLES", "ORANGES"}).isAllowed({"BANANAS"}));
  ASSERT_TRUE(MemeStack::from(MemeTaxonomy::empty(), {"APPLES", "ORANGES"}).isAllowed({}));
  ASSERT_TRUE(MemeStack::from(MemeTaxonomy::empty(), {}).isAllowed({"BANANAS"}));
}

/**
 Anti-Memes
 <p>
 Artist can add !MEME values into Programs https://github.com/xjmusic/workstation/issues/214
 */
TEST(MemeStackTest, AntiMemes) {
  ASSERT_TRUE(MemeStack::from(MemeTaxonomy::empty(), {"!APPLES", "ORANGES"}).isAllowed({"!APPLES"}));
  ASSERT_TRUE(
      MemeStack::from(MemeTaxonomy::empty(), {"!APPLES", "!ORANGES"}).isAllowed({"!APPLES", "!ORANGES", "BANANAS"}));
  ASSERT_FALSE(MemeStack::from(MemeTaxonomy::empty(), {"APPLES", "ORANGES"}).isAllowed({"!APPLES"}));
  ASSERT_FALSE(MemeStack::from(MemeTaxonomy::empty(), {"APPLES", "ORANGES"}).isAllowed({"!ORANGES"}));
  ASSERT_FALSE(MemeStack::from(MemeTaxonomy::empty(), {"APPLES", "ORANGES"}).isAllowed({"!ORANGES", "APPLES"}));
  ASSERT_FALSE(MemeStack::from(MemeTaxonomy::empty(), {"APPLES", "ORANGES"}).isAllowed({"ORANGES", "!APPLES"}));
  ASSERT_FALSE(MemeStack::from(MemeTaxonomy::empty(), {"APPLES", "ORANGES"}).isAllowed({"!ORANGES", "!APPLES"}));
  ASSERT_FALSE(MemeStack::from(MemeTaxonomy::empty(), {"!APPLES", "ORANGES"}).isAllowed({"!APPLES", "!ORANGES"}));
}

/**
 Unique Memes
 <p>
 Artist can add `$MEME` so only one is chosen https://github.com/xjmusic/workstation/issues/219
 */
TEST(MemeStackTest, UniqueMemes) {
  ASSERT_TRUE(MemeStack::from(MemeTaxonomy::empty(), {"APPLES", "ORANGES"}).isAllowed({"$PELICANS"}));
  ASSERT_TRUE(MemeStack::from(MemeTaxonomy::empty(), {"APPLES", "$PELICANS"}).isAllowed({"BANANAS"}));
  ASSERT_FALSE(MemeStack::from(MemeTaxonomy::empty(), {"APPLES", "ORANGES", "$PELICANS"}).isAllowed({"$PELICANS"}));
  ASSERT_FALSE(MemeStack::from(MemeTaxonomy::empty(), {"APPLES", "$PELICANS"}).isAllowed({"BANANAS", "$PELICANS"}));
}

/**
 Numeric memes with common letters and different integer prefix (e.g. 2STEP vs 4STEP) are known to be exclusive https://github.com/xjmusic/workstation/issues/217
 */
TEST(MemeStackTest, NumericMemes) {
  ASSERT_EQ(5, ParseNumeric::fromString("5BEAT").prefix);
  ASSERT_EQ("STEP", ParseNumeric::fromString("2STEP").body);
  ASSERT_TRUE(ParseNumeric::fromString("2STEP").valid);
  ASSERT_EQ(0, ParseNumeric::fromString("JAMMY").prefix);
  ASSERT_EQ("", ParseNumeric::fromString("JAMMY").body);
  ASSERT_FALSE(ParseNumeric::fromString("JAMMY").valid);
  ASSERT_TRUE(MemeStack::from(MemeTaxonomy::empty(), {"JAMS", "2STEP"}).isAllowed({"2STEP", "4NOTE"}));
  ASSERT_TRUE(MemeStack::from(MemeTaxonomy::empty(), {"JAMS", "4NOTE", "2STEP"}).isAllowed({"2STEP", "4NOTE"}));
  ASSERT_FALSE(MemeStack::from(MemeTaxonomy::empty(), {"JAMS", "2STEP"}).isAllowed({"4STEP", "4NOTE"}));
  ASSERT_FALSE(MemeStack::from(MemeTaxonomy::empty(), {"JAMS", "2STEP", "4NOTE"}).isAllowed({"2STEP", "3NOTE"}));
}

/**
 Strong-meme like LEMONS! should always favor LEMONS https://github.com/xjmusic/workstation/issues/218
 */
TEST(MemeStackTest, StrongMemes) {
  ASSERT_EQ("LEMONS", ParseStrong::fromString("LEMONS!").body);
  ASSERT_TRUE(ParseStrong::fromString("LEMONS!").valid);
  ASSERT_FALSE(ParseStrong::fromString("LEMONS").valid);
  ASSERT_TRUE(MemeStack::from(MemeTaxonomy::empty(), {"JAMS", "LEMONS!"}).isAllowed({"4NOTE", "LEMONS"}));
  ASSERT_FALSE(MemeStack::from(MemeTaxonomy::empty(), {"JAMS", "ORANGES"}).isAllowed({"4NOTE", "LEMONS!"}));
  ASSERT_TRUE(MemeStack::from(MemeTaxonomy::empty(), {"JAMS", "LEMONS!"}).isAllowed({"4NOTE", "ORANGES"}));
}
/**
 Strong-meme like LEMONS! should always favor LEMONS https://github.com/xjmusic/workstation/issues/218
 */
TEST(MemeStackTest, StrongMemes_OkayToAddBothStrongAndRegular_ButNotOnlyStrong) {
  ASSERT_TRUE(MemeStack::from(MemeTaxonomy::empty(), {"LEMONS", "LEMONS!"}).isValid());
  ASSERT_FALSE(MemeStack::from(MemeTaxonomy::empty(), {"LEMONS!"}).isValid());
  ASSERT_TRUE(MemeStack::from(MemeTaxonomy::empty(), {"LEMONS"}).isAllowed({"LEMONS!"}));
}

TEST(MemeStackTest, StrongMemes_WithTaxonomy) {
  std::set<MapStringToOneOrManyString> input = {
      {{"name", "VOXHOOK"}, {"memes", std::set<std::string>{"DONTLOOK", "NEEDU", "FLOAT", "ALLGO"}}},
      {{"name", "SEASON"},  {"memes", std::set<std::string>{"WINTER", "SPRING", "SUMMER", "FALL"}}}
  };

  auto taxonomy = MemeTaxonomy::fromSet(input);

  ASSERT_TRUE(MemeStack::from(
      taxonomy,
      {
          "!TIGHTY",
          "4NOTE",
          "ALLGO",
          "EARTH",
          "FIRE",
          "KNOCKY",
          "LARGE",
          "NEW",
          "OPEN",
          "SMALL",
          "STRAIGHT",
          "STRONGMEME",
          "WATER",
          "WIDEOPEN",
          "WIND"
      }
  ).isAllowed(
      {
          "SMALL",
          "FIRE",
          "STRONGMEME!"
      }
  ));
}

/**
 TemplateConfig has Meme categories
 https://github.com/xjmusic/workstation/issues/209
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
TEST(MemeStackTest, MemeCategories) {
  auto taxonomy = MemeTaxonomy::fromString("CITY[CHICAGO,DENVER,PHILADELPHIA]");

  ASSERT_TRUE(MemeStack::from(taxonomy, {"CHICAGO", "ORANGES"}).isAllowed({"PEACHES"}));
  ASSERT_FALSE(MemeStack::from(taxonomy, {"CHICAGO", "ORANGES"}).isAllowed({"DENVER"}));
  ASSERT_TRUE(MemeStack::from(taxonomy, {"CHICAGO", "ORANGES"}).isValid());
  ASSERT_TRUE(MemeStack::from(taxonomy, {"DENVER", "ORANGES"}).isValid());
  ASSERT_FALSE(MemeStack::from(taxonomy, {"CHICAGO", "DENVER", "ORANGES"}).isValid());
}

TEST(MemeStackTest, MemeCategories_AllowAlreadyPresentFromTaxonomy) {
  auto taxonomy = MemeTaxonomy::fromString("CITY[ABERDEEN,NAGOYA]");

  ASSERT_TRUE(MemeStack::from(taxonomy, {"ABERDEEN"}).isAllowed({"ABERDEEN"}));
}

/**
 Refuse to make a choice that violates the meme stack https://github.com/xjmusic/workstation/issues/211
 */
TEST(MemeStackTest, IsValid) {
  ASSERT_TRUE(MemeStack::from(MemeTaxonomy::empty(),
                              {"APPLES", "!ORANGES", "$BANANAS", "APPLES!", "5LEMONS", "12MONKEYS"}).isValid());
  ASSERT_FALSE(MemeStack::from(MemeTaxonomy::empty(),
                               {"APPLES", "!APPLES", "$BANANAS", "APPLES!", "5LEMONS", "12MONKEYS"}).isValid());
  ASSERT_FALSE(MemeStack::from(MemeTaxonomy::empty(),
                               {"APPLES", "!ORANGES", "$BANANAS", "APPLES!", "5LEMONS", "12LEMONS"}).isValid());
}