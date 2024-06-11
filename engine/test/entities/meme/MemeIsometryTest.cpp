// Copyright (c) 1999-2022, XJ Music Inc. (https://xj.io) All Rights Reserved.

#include <gtest/gtest.h>
#include <set>

#include "xjmusic/entities/meme/MemeIsometry.h"

using namespace XJ;

TEST(MemeIsometryTest, Of) {
  MemeIsometry result = MemeIsometry::of({"Smooth", "Catlike"});
  std::set<std::string> expected = {"CATLIKE", "SMOOTH"};
  ASSERT_EQ(expected, result.getSources());
}

TEST(MemeIsometryTest, Add) {
  MemeIsometry result = MemeIsometry::of({"Smooth"});
  result.add("Catlike");
  std::set<std::string> expected = {"CATLIKE", "SMOOTH"};
  ASSERT_EQ(expected, result.getSources());
}

TEST(MemeIsometryTest, GetSourceStems) {
  std::set<std::string> result = MemeIsometry::of({"Intensity", "Cool", "Dark"}).getSources();
  std::set<std::string> expected = {"COOL", "DARK", "INTENSITY"};
  ASSERT_EQ(expected, result);
}

TEST(MemeIsometryTest, GetConstellation) {
  ASSERT_EQ("CATLIKE_SMOOTH", MemeIsometry::of({"Smooth", "Catlike"}).getConstellation());
  ASSERT_EQ("COOL_DARK_INTENSITY", MemeIsometry::of({"Intensity", "Cool", "Dark"}).getConstellation());
  ASSERT_EQ("BAM_FLAM_SHIM_WHAM", MemeIsometry::of({"Wham", "Bam", "Shim", "Shim", "Shim", "Flam"}).getConstellation());
}

TEST(MemeIsometryTest, GetConstellationWithNotMeme) {
  ASSERT_EQ("!CLUMSY_CATLIKE_SMOOTH", MemeIsometry::of({"Smooth", "Catlike", "!Clumsy"}).getConstellation());
}
