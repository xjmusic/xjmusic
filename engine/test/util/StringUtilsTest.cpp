// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/util/StringUtils.h"

using namespace Util;

TEST(StringUtilsTest, Split) {
  std::string s = "\n  one,     two,\nthree";
  char delimiter = ',';
  std::vector<std::string> result = StringUtils::split(s, delimiter);

  ASSERT_EQ(3, result.size());
  ASSERT_EQ("one", result[0]);
  ASSERT_EQ("two", result[1]);
  ASSERT_EQ("three", result[2]);
}

TEST(StringUtilsTest, Join) {
  std::vector<std::string> v = {"one", "two", "three"};
  std::string delimiter = ",";
  std::string result = StringUtils::join(v, delimiter);

  ASSERT_EQ("one,two,three", result);
}

TEST(StringUtilsTest, Trim) {
  std::string s = "  test string\n  ";
  std::string result = StringUtils::trim(s);

  ASSERT_EQ("test string", result);
}

TEST(StringUtilsTest, ToMeme) {
  ASSERT_EQ("JAMMYB!NS", StringUtils::toMeme("jaMMy b#!ns"));
  ASSERT_EQ("JAMMY", StringUtils::toMeme("jaMMy"));
  std::string i1 = "j#MMy";
  ASSERT_EQ("JMMY", StringUtils::toMeme(&i1, "neuf"));
  ASSERT_EQ("NEUF", StringUtils::toMeme(nullptr, "neuf"));
  std::string i2 = "%&(#";
  ASSERT_EQ("NEUF", StringUtils::toMeme(&i2, "neuf"));
  ASSERT_EQ("P", StringUtils::toMeme("%&(#p"));
  ASSERT_EQ("", StringUtils::toMeme("%&(#"));
  ASSERT_EQ("$UNIQUE", StringUtils::toMeme("$UNIQUE"));
}

TEST(StringUtilsTest, IsNullOrEmpty) {
  std::string s1 = "test";
  std::string s2;
  ASSERT_TRUE(StringUtils::isNullOrEmpty(nullptr));
  ASSERT_FALSE(StringUtils::isNullOrEmpty(&s1));
  ASSERT_TRUE(StringUtils::isNullOrEmpty(&s2));
}

TEST(StringUtilsTest, ToAlphabetical) {
  ASSERT_EQ("Pajamas", StringUtils::toAlphabetical("Pajamas"));
  ASSERT_EQ("Pajamas", StringUtils::toAlphabetical("1P34aj2a3ma321s"));
  ASSERT_EQ("Pajamas", StringUtils::toAlphabetical("  P#$ aj#$@a   @#$$$$ma         s"));
  ASSERT_EQ("Pajamas", StringUtils::toAlphabetical("P_+_+_+_+_(@(#%&!&&&@&%!@)_$*(!_)@()_#()(((a j a m a s "));
  ASSERT_EQ("Pajamas", StringUtils::toAlphabetical("Pajamas"));
}

TEST(StringUtilsTest, ToAlphanumeric) {
  ASSERT_EQ("Pajamas", StringUtils::toAlphanumeric("Pajamas!!!!!!"));
  ASSERT_EQ("17Pajamas", StringUtils::toAlphanumeric("17 Pajamas?"));
  ASSERT_EQ("Pajamas5", StringUtils::toAlphanumeric("  P#$ aj#$@a   @#$$$$ma         s5"));
  ASSERT_EQ("Pajamas25", StringUtils::toAlphanumeric("P_+_+_+_+_(@(#%&!&&&@&%!@)_$*(!_)@()_#()(((a j a m a s 2    5"));
  ASSERT_EQ("Pajamas", StringUtils::toAlphanumeric("Pajamas"));
}

TEST(StringUtilsTest, ToUpperCase) {
  ASSERT_EQ("PAJAMAS", StringUtils::toUpperCase("Pajamas"));
  ASSERT_EQ("PAJAMAS", StringUtils::toUpperCase("pajamas"));
  ASSERT_EQ("PAJAMAS", StringUtils::toUpperCase("pAjAmAs"));
}

TEST(StringUtilsTest, ToLowerCase) {
  ASSERT_EQ("pajamas", StringUtils::toLowerCase("Pajamas"));
  ASSERT_EQ("pajamas", StringUtils::toLowerCase("pajamas"));
  ASSERT_EQ("pajamas", StringUtils::toLowerCase("pAjAmAs"));
}

TEST(StringUtilsTest, FormatFloat) {
  ASSERT_EQ("1.0", StringUtils::formatFloat(1.0f));
  ASSERT_EQ("1.05", StringUtils::formatFloat(1.05000));
  ASSERT_EQ("1.000007", StringUtils::formatFloat(1.00000700000));
  ASSERT_EQ("1.0", StringUtils::formatFloat(1.00000000300000000000000)); // past floating point precision
}

TEST(StringUtilsTest, StripExtraSpaces) {
  ASSERT_EQ("just a shadow", StringUtils::stripExtraSpaces(" just   a     shadow   "));
}

TEST(StringUtilsTest, Match) {
  std::regex abc("^([ABC]+)$");
  std::regex rgxSlashPost("[^/]*/([A-G♯#♭b]+)$");

  ASSERT_FALSE(StringUtils::match(abc, "123").has_value());
  ASSERT_EQ("A", StringUtils::match(abc, "A"));
  ASSERT_EQ("Eb", StringUtils::match(rgxSlashPost, "C#/Eb"));
}

TEST(StringUtilsTest, CountMatches) {
  std::regex abc("[ABC]");
  std::regex accidentalSharp("[♯#]");
  std::regex accidentalFlat("[♭b]");

  ASSERT_EQ(3, StringUtils::countMatches(abc, "A B C"));
  ASSERT_EQ(1, StringUtils::countMatches(accidentalSharp, "C#/Eb"));
  ASSERT_EQ(1, StringUtils::countMatches(accidentalFlat, "C#/Eb"));
  ASSERT_EQ(2, StringUtils::countMatches(accidentalFlat, "Gb/Ab"));
  ASSERT_EQ(2, StringUtils::countMatches(accidentalSharp, "F#/A#"));
}
