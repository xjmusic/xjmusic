// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>
#include <set>

#include "xjmusic/meme/MemeIsometry.h"
#include "../_helper/TestHelpers.h"

using namespace XJ;

TEST(MemeIsometryTest, Of) {
  MemeIsometry result = MemeIsometry::of({"Smooth", "Catlike"});
  const std::set<std::string> expected = {"CATLIKE", "SMOOTH"};
  ASSERT_EQ(expected, result.getSources());
}

TEST(MemeIsometryTest, Add) {
  MemeIsometry result = MemeIsometry::of({"Smooth"});
  result.add("Catlike");
  const std::set<std::string> expected = {"CATLIKE", "SMOOTH"};
  ASSERT_EQ(expected, result.getSources());
}

TEST(MemeIsometryTest, GetSourceStems) {
  const std::set<std::string> result = MemeIsometry::of({"Intensity", "Cool", "Dark"}).getSources();
  const std::set<std::string> expected = {"COOL", "DARK", "INTENSITY"};
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

TEST(MemeIsometryTest, Of_List) {
  auto subject = MemeIsometry::of(MemeTaxonomy::empty(), {"Smooth", "Catlike"});

  std::set<std::string> sources = subject.getSources();
  std::vector sourcesVector(sources.begin(), sources.end());
  std::sort(sourcesVector.begin(), sourcesVector.end());

  const std::vector<std::string> expected = {"CATLIKE", "SMOOTH"};
  ASSERT_EQ(expected, sourcesVector);
}

TEST(MemeIsometryTest, AddMore) {
  auto subject = MemeIsometry::of(MemeTaxonomy::empty(), {"Smooth"});

  ProgramMeme meme;
  meme.id = EntityUtils::computeUniqueId();
  meme.name = "Catlike";

  subject.add(meme);

  std::set<std::string> sources = subject.getSources();
  std::vector sourcesVector(sources.begin(), sources.end());
  std::sort(sourcesVector.begin(), sourcesVector.end());

  const std::vector<std::string> expected = {"CATLIKE", "SMOOTH"};
  ASSERT_EQ(expected, sourcesVector);
}

TEST(MemeIsometryTest, DoNotMutate) {
  auto subject = MemeIsometry::of(MemeTaxonomy::empty(), {"Intensity", "Cool", "Dark"}).getSources();

  const std::vector<std::string> expected = {"COOL", "DARK", "INTENSITY"};
  std::vector sourcesVector(subject.begin(), subject.end());
  std::sort(sourcesVector.begin(), sourcesVector.end());

  ASSERT_EQ(expected, sourcesVector);
}

TEST(MemeIsometryTest, Score) {
  const auto subject = MemeIsometry::of(MemeTaxonomy::empty(), {"Smooth", "Catlike"});

  ASSERT_NEAR(1.0, subject.score({"Smooth"}), 0.1);
  ASSERT_NEAR(1.0, subject.score({"Catlike"}), 0.1);
  ASSERT_NEAR(2.0, subject.score({"Smooth", "Catlike"}), 0.1);
}

TEST(MemeIsometryTest, ScoreEliminatesDuplicates) {
  const auto subject = MemeIsometry::of(MemeTaxonomy::empty(), {"Smooth", "Smooth", "Catlike"});

  ASSERT_NEAR(1.0, subject.score({"Smooth"}), 0.1);
  ASSERT_NEAR(1.0, subject.score({"Catlike"}), 0.1);
  ASSERT_NEAR(2.0, subject.score({"Smooth", "Catlike"}), 0.1);
}