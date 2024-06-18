// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/music/SlashRoot.h"

using namespace XJ;


TEST(Music_SlashRoot, of) {
  ASSERT_EQ(PitchClass::As, SlashRoot::of("Gm/Bb").pitchClass);
}


TEST(Music_SlashRoot, getPitchClass) {
  ASSERT_FALSE(SlashRoot::of("Cm7").pitchClass.has_value());
  ASSERT_EQ(PitchClass::G, SlashRoot::of("Eb/G").pitchClass);
}


TEST(Music_SlashRoot, orDefault) {
  ASSERT_EQ(PitchClass::As, SlashRoot::of("Eb").orDefault(PitchClass::As));
}


TEST(Music_SlashRoot, pre) {
  ASSERT_EQ("", SlashRoot::of("/G").pre);
  ASSERT_EQ("maj7", SlashRoot::computePre("maj7"));
  ASSERT_EQ("m", SlashRoot::computePre("m/Bb"));
  ASSERT_EQ("", SlashRoot::computePre("/G"));
}


TEST(Music_SlashRoot, has_value) {
  ASSERT_FALSE(SlashRoot::has_value("Gm"));
  ASSERT_TRUE(SlashRoot::has_value("Gm/Bb"));
}


/**
 * XJ should correctly choose chords with tensions notated via slash and not confuse them with slash chords
 * https://www.pivotaltracker.com/story/show/183738228
 */
TEST(Music_SlashRoot, onlyNotes) {
  ASSERT_EQ("", SlashRoot::of("C 7/9/13").post);
  ASSERT_EQ("", SlashRoot::of("C 7/9").post);
  ASSERT_EQ("E", SlashRoot::of("C 7/E").post);
}


TEST(Music_SlashRoot, isSame) {
  ASSERT_TRUE(SlashRoot::of("C/E") == SlashRoot::of("A/E"));
}


TEST(Music_SlashRoot, display) {
  ASSERT_EQ("", SlashRoot::of("G 7/9/13").display(Accidental::Sharp));
  ASSERT_EQ("/E", SlashRoot::of("A/E").display(Accidental::Sharp));
  ASSERT_EQ("/Eb", SlashRoot::of("Ab/Eb").display(Accidental::Flat));
  ASSERT_EQ("/D#", SlashRoot::of("Ab/Eb").display(Accidental::Sharp));
}


/**
 * XJ should correctly choose chords with tensions notated via slash and not confuse them with slash chords
 * https://www.pivotaltracker.com/story/show/183738228
 */
TEST(Music_SlashRoot, constructor_dontConfuseTensionWithSlash) {
  auto tension = SlashRoot::of("C 7/9");
  ASSERT_EQ("C 7/9", tension.pre);
  ASSERT_FALSE(tension.pitchClass.has_value());
  ASSERT_EQ("", tension.post);
}


/**
 * XJ should correctly choose chords with tensions notated via slash and not confuse them with slash chords
 * https://www.pivotaltracker.com/story/show/183738228
 */
TEST(Music_SlashRoot, isPresent_dontConfuseTensionWithSlash) {
  ASSERT_TRUE(SlashRoot::has_value("C/E"));
  ASSERT_FALSE(SlashRoot::has_value("C/9"));
}

