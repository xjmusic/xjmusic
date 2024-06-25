// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/util/StringUtils.h"

using namespace XJ;


TEST(StringUtilsTest, Split) {
  const std::string s = "\n  one,     two,\nthree";
  const char delimiter = ',';
  const std::vector<std::string> result = StringUtils::split(s, delimiter);

  ASSERT_EQ(3, result.size());
  ASSERT_EQ("one", result[0]);
  ASSERT_EQ("two", result[1]);
  ASSERT_EQ("three", result[2]);
}

TEST(StringUtilsTest, Join) {
  const std::vector<std::string> v = {"one", "two", "three"};
  const std::string delimiter = ",";
  const std::string result = StringUtils::join(v, delimiter);

  ASSERT_EQ("one,two,three", result);
}

TEST(StringUtilsTest, Trim) {
  const std::string s = "  test string\n  ";
  const std::string result = StringUtils::trim(s);

  ASSERT_EQ("test string", result);
}

TEST(StringUtilsTest, ToMeme) {
  ASSERT_EQ("JAMMYB!NS", StringUtils::toMeme("jaMMy b#!ns"));
  ASSERT_EQ("JAMMY", StringUtils::toMeme("jaMMy"));
  const std::string i1 = "j#MMy";
  ASSERT_EQ("JMMY", StringUtils::toMeme(&i1, "neuf"));
  ASSERT_EQ("NEUF", StringUtils::toMeme(nullptr, "neuf"));
  const std::string i2 = "%&(#";
  ASSERT_EQ("NEUF", StringUtils::toMeme(&i2, "neuf"));
  ASSERT_EQ("P", StringUtils::toMeme("%&(#p"));
  ASSERT_EQ("", StringUtils::toMeme("%&(#"));
  ASSERT_EQ("$UNIQUE", StringUtils::toMeme("$UNIQUE"));
}

TEST(StringUtilsTest, IsNullOrEmpty) {
  const std::string s1 = "test";
  const std::string s2;
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
  const std::regex abc("^([ABC]+)$");
  const std::regex rgxSlashPost("[^/]*/([A-G♯#♭b]+)$");

  ASSERT_FALSE(StringUtils::match(abc, "123").has_value());
  ASSERT_EQ("A", StringUtils::match(abc, "A"));
  ASSERT_EQ("Eb", StringUtils::match(rgxSlashPost, "C#/Eb"));
}

TEST(StringUtilsTest, CountMatches) {
  const std::regex abc("[ABC]");
  const std::regex accidentalSharp("[♯#]");
  const std::regex accidentalFlat("[♭b]");

  ASSERT_EQ(3, StringUtils::countMatches(abc, "A B C"));
  ASSERT_EQ(1, StringUtils::countMatches(accidentalSharp, "C#/Eb"));
  ASSERT_EQ(1, StringUtils::countMatches(accidentalFlat, "C#/Eb"));
  ASSERT_EQ(2, StringUtils::countMatches(accidentalFlat, "Gb/Ab"));
  ASSERT_EQ(2, StringUtils::countMatches(accidentalSharp, "F#/A#"));
}

TEST(StringUtilsTest, ToLowerScored) {
  EXPECT_EQ("hammy_jammy", StringUtils::toLowerScored("HAMMY jaMMy"));
  EXPECT_EQ("jammy", StringUtils::toLowerScored("jaMMy"));
  EXPECT_EQ("jam_42", StringUtils::toLowerScored("jaM &&$ 42"));
  EXPECT_EQ("jam_42", StringUtils::toLowerScored("  ## jaM &&$ 42"));
  EXPECT_EQ("jam_42", StringUtils::toLowerScored("jaM &&$ 42 !!!!"));
  EXPECT_EQ("hammy_jammy_bunbuns", StringUtils::toLowerScored("HAMMY $%& jaMMy bun%buns"));
  EXPECT_EQ("p", StringUtils::toLowerScored("%&(#p"));
  EXPECT_EQ("", StringUtils::toLowerScored("%&(#"));
}

TEST(StringUtilsTest, ToScored) {
  EXPECT_EQ("", StringUtils::toScored(""));
  EXPECT_EQ("HAMMY_jaMMy", StringUtils::toScored("HAMMY jaMMy"));
  EXPECT_EQ("jaMMy", StringUtils::toScored("jaMMy"));
  EXPECT_EQ("jaM_42", StringUtils::toScored("jaM &&$ 42"));
  EXPECT_EQ("jaM_42", StringUtils::toScored("  ## jaM &&$ 42"));
  EXPECT_EQ("jaM_42", StringUtils::toScored("jaM &&$ 42 !!!!"));
  EXPECT_EQ("HAMMY_jaMMy_bunbuns", StringUtils::toScored("HAMMY $%& jaMMy bun%buns"));
  EXPECT_EQ("p", StringUtils::toScored("%&(#p"));
  EXPECT_EQ("", StringUtils::toScored("%&(#"));
}

TEST(StringUtilsTest, ToUpperScored) {
  EXPECT_EQ("JAMMY_BUNS", StringUtils::toUpperCase(StringUtils::toScored("jaMMy b#!uns")));
  EXPECT_EQ("JAMMY_BUNS", StringUtils::toUpperCase(StringUtils::toScored("  jaMMy    b#!uns   ")));
  EXPECT_EQ("JAMMY", StringUtils::toUpperCase(StringUtils::toScored("jaMMy")));
  EXPECT_EQ("JMMY", StringUtils::toUpperCase(StringUtils::toScored("j#MMy")));
  EXPECT_EQ("P", StringUtils::toUpperCase(StringUtils::toScored("%&(#p")));
  EXPECT_EQ("", StringUtils::toUpperCase(StringUtils::toScored("%&(#")));
}

TEST(StringUtilsTest, ToProper) {
  EXPECT_EQ("Jammy biscuit", StringUtils::toProper("jammy biscuit"));
  EXPECT_EQ("Jammy", StringUtils::toProper("jammy"));
  EXPECT_EQ("J#mmy", StringUtils::toProper("j#mmy"));
  EXPECT_EQ("%&(#", StringUtils::toProper("%&(#"));
}

TEST(StringUtilsTest, ToProperSlug) {
  EXPECT_EQ("Jammybiscuit", StringUtils::toProperSlug("jammy biscuit"));
  EXPECT_EQ("Jammy", StringUtils::toProperSlug("jammy"));
  EXPECT_EQ("Jmmy", StringUtils::toProperSlug("j#mmy"));
  EXPECT_EQ("P", StringUtils::toProperSlug("%&(#p"));
  EXPECT_EQ("", StringUtils::toProperSlug("%&(#"));
  EXPECT_EQ("NextMain", StringUtils::toProperSlug("NextMain"));
}

TEST(StringUtilsTest, ToSlug) {
  EXPECT_EQ("jim", StringUtils::toSlug("jim"));
  EXPECT_EQ("jim251", StringUtils::toSlug("jim-251"));
  EXPECT_EQ("jim251", StringUtils::toSlug("j i m - 2 5 1"));
  EXPECT_EQ("jm251", StringUtils::toSlug("j!$m%-^2%5*1"));
}

TEST(StringUtilsTest, ToLowerSlug) {
  EXPECT_EQ("h4mmyjammy", StringUtils::toLowerSlug("H4MMY jaMMy"));
  EXPECT_EQ("jammy", StringUtils::toLowerSlug("jaMMy"));
  EXPECT_EQ("jmmy", StringUtils::toLowerSlug("j#MMy"));
  EXPECT_EQ("p", StringUtils::toLowerSlug("%&(#p"));
  EXPECT_EQ("", StringUtils::toLowerSlug("%&(#"));
}

TEST(StringUtilsTest, ToUpperSlug) {
  EXPECT_EQ("H4MMYJAMMY", StringUtils::toUpperSlug("H4MMY jaMMy"));
  EXPECT_EQ("JAMMY", StringUtils::toUpperSlug("jaMMy"));
  EXPECT_EQ("JMMY", StringUtils::toUpperSlug("j#MMy"));
  EXPECT_EQ("P", StringUtils::toUpperSlug("%&(#p"));
  EXPECT_EQ("", StringUtils::toUpperSlug("%&(#"));
}