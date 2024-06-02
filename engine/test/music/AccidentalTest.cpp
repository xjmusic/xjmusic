// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/music/Accidental.h"

using namespace Music;

TEST(Music_Accidental, TestAdjSymbolOf) {
  ASSERT_EQ(Accidental::Sharp, accidentalOf("C"));
  ASSERT_EQ(Accidental::Flat, accidentalOf("CMb5b7"));
  ASSERT_EQ(Accidental::Sharp, accidentalOf("C#"));
  ASSERT_EQ(Accidental::Flat, accidentalOf("Gb"));
  ASSERT_EQ(Accidental::Flat, accidentalOf("G♭M"));
  ASSERT_EQ(Accidental::Sharp, accidentalOf("A#m"));
  ASSERT_EQ(Accidental::Sharp, accidentalOf("A♯M♯5"));
  ASSERT_EQ(Accidental::Flat, accidentalOf("C minor"));
  ASSERT_EQ(Accidental::Flat, accidentalOf("C dim"));
  ASSERT_EQ(Accidental::Sharp, accidentalOf("CM M9 m7")); // More Sharpish than Flattish
  ASSERT_EQ(Accidental::Flat, accidentalOf("Cm m9 M7"));  // More Flattish than Sharpish
  ASSERT_EQ(Accidental::Sharp, accidentalOf("C major"));
}

TEST(Music_Accidental, TestAdjSymbolBegin) {
  ASSERT_EQ(Accidental::Natural, accidentalOfBeginning(""));
  ASSERT_EQ(Accidental::Natural, accidentalOfBeginning("Mb5b7"));
  ASSERT_EQ(Accidental::Sharp, accidentalOfBeginning("#"));
  ASSERT_EQ(Accidental::Flat, accidentalOfBeginning("b"));
  ASSERT_EQ(Accidental::Flat, accidentalOfBeginning("♭M"));
  ASSERT_EQ(Accidental::Sharp, accidentalOfBeginning("#m"));
  ASSERT_EQ(Accidental::Sharp, accidentalOfBeginning("♯M♯5"));
}


TEST(Music_Accidental, Normalized) {
  ASSERT_EQ("C#", accidentalNormalized("C#"));
  ASSERT_EQ("C#", accidentalNormalized("C♯"));
  ASSERT_EQ("Cb", accidentalNormalized("Cb"));
  ASSERT_EQ("Cb", accidentalNormalized("C♭"));
}
