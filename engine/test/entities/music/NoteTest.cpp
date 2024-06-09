// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/entities/music/Note.h"
#include "xjmusic/util/StringUtils.h"

#include "../../_helper/TestHelpers.h"

using namespace XJ;

/**
 * [#303] Craft calculates drum audio pitch to conform to the allowable note closest to the original note, slightly favoring down-pitching versus up-pitching.
 */
TEST(Music_Note, setOctaveNearest) {
  ASSERT_EQ(3, Note::of("C7").setOctaveNearest(Note::of("B2")).octave);
  ASSERT_EQ(2, Note::of("F7").setOctaveNearest(Note::of("B2")).octave);
  ASSERT_EQ(3, Note::of("E7").setOctaveNearest(Note::of("B2")).octave);
}

TEST(Music_Note, noteToString) {
  ASSERT_EQ("C#5", Note::of("C#5").toString(Accidental::Sharp));
  ASSERT_EQ("Db5", Note::of("C#5").toString(Accidental::Flat));
}

TEST(Music_Note, sameAs) {
  ASSERT_TRUE(Note::of("C5") == Note::of("C5"));
  ASSERT_TRUE(Note::of("C#5") == Note::of("Db5"));
  ASSERT_FALSE(Note::of("C#5") == Note::of("Db6"));
  ASSERT_FALSE(Note::of("C#5") == Note::of("Eb5"));
}

TEST(Music_Note, delta) {
  // run from -20 to +20
  ASSERT_EQ(-20, Note::of("C5").delta(Note::of("E3")));
  ASSERT_EQ(-19, Note::of("C5").delta(Note::of("F3")));
  ASSERT_EQ(-18, Note::of("C5").delta(Note::of("Gb3")));
  ASSERT_EQ(-17, Note::of("C5").delta(Note::of("G3")));
  ASSERT_EQ(-16, Note::of("C5").delta(Note::of("Ab3")));
  ASSERT_EQ(-15, Note::of("C5").delta(Note::of("A3")));
  ASSERT_EQ(-14, Note::of("C5").delta(Note::of("Bb3")));
  ASSERT_EQ(-13, Note::of("C5").delta(Note::of("B3")));
  ASSERT_EQ(-12, Note::of("C5").delta(Note::of("C4")));
  ASSERT_EQ(-11, Note::of("C5").delta(Note::of("Db4")));
  ASSERT_EQ(-10, Note::of("C5").delta(Note::of("D4")));
  ASSERT_EQ(-9, Note::of("C5").delta(Note::of("Eb4")));
  ASSERT_EQ(-8, Note::of("C5").delta(Note::of("E4")));
  ASSERT_EQ(-7, Note::of("C5").delta(Note::of("F4")));
  ASSERT_EQ(-6, Note::of("C5").delta(Note::of("Gb4")));
  ASSERT_EQ(-5, Note::of("C5").delta(Note::of("G4")));
  ASSERT_EQ(-4, Note::of("C5").delta(Note::of("Ab4")));
  ASSERT_EQ(-3, Note::of("C5").delta(Note::of("A4")));
  ASSERT_EQ(-2, Note::of("C5").delta(Note::of("Bb4")));
  ASSERT_EQ(-1, Note::of("C5").delta(Note::of("B4")));
  ASSERT_EQ(0, Note::of("C5").delta(Note::of("C5")));
  ASSERT_EQ(1, Note::of("C5").delta(Note::of("C#5")));
  ASSERT_EQ(2, Note::of("C5").delta(Note::of("D5")));
  ASSERT_EQ(3, Note::of("C5").delta(Note::of("D#5")));
  ASSERT_EQ(4, Note::of("C5").delta(Note::of("E5")));
  ASSERT_EQ(5, Note::of("C5").delta(Note::of("F5")));
  ASSERT_EQ(6, Note::of("C5").delta(Note::of("F#5")));
  ASSERT_EQ(7, Note::of("C5").delta(Note::of("G5")));
  ASSERT_EQ(8, Note::of("C5").delta(Note::of("G#5")));
  ASSERT_EQ(9, Note::of("C5").delta(Note::of("A5")));
  ASSERT_EQ(10, Note::of("C5").delta(Note::of("A#5")));
  ASSERT_EQ(11, Note::of("C5").delta(Note::of("B5")));
  ASSERT_EQ(12, Note::of("C5").delta(Note::of("C6")));
  ASSERT_EQ(13, Note::of("C5").delta(Note::of("C#6")));
  ASSERT_EQ(14, Note::of("C5").delta(Note::of("D6")));
  ASSERT_EQ(15, Note::of("C5").delta(Note::of("D#6")));
  ASSERT_EQ(16, Note::of("C5").delta(Note::of("E6")));
  ASSERT_EQ(17, Note::of("C5").delta(Note::of("F6")));
  ASSERT_EQ(18, Note::of("C5").delta(Note::of("F#6")));
  ASSERT_EQ(19, Note::of("C5").delta(Note::of("G6")));
  ASSERT_EQ(20, Note::of("C5").delta(Note::of("G#6")));
  // spot checks relative to A4 (re: tuning)
  ASSERT_EQ(6, Note::of("A4").delta(Note::of("D#5")));
}

TEST(Music_Note, NamedTest) {
  Note note = Note::of("G");
  ASSERT_EQ(PitchClass::G, note.pitchClass);
}

TEST(Music_Note, OfPitchClassTest) {
  Note note = Note::of(PitchClass::C, 5);
  ASSERT_EQ(5, note.octave);
  ASSERT_EQ(PitchClass::C, note.pitchClass);
}

TEST(Music_Note, compareTo) {
  auto notes = std::vector<Note>{
      Note::of("C1"),
      Note::of("F#1"),
      Note::of("E1"),
      Note::of("C3"),
      Note::of("D#3"),
      Note::of("D1"),
      Note::of("D2"),
      Note::of("D3"),
      Note::of("E2"),
      Note::of("C2"),
      Note::of("D#2"),
      Note::of("E3"),
      Note::of("D#1"),
      Note::of("F#2")};
  std::sort(notes.begin(), notes.end());
  std::vector<std::string> noteStrings;
  std::transform(notes.begin(), notes.end(), std::back_inserter(noteStrings), [](Note note) {
    return note.toString(Accidental::Sharp);
  });

  ASSERT_EQ( "C1,D1,D#1,E1,F#1,C2,D2,D#2,E2,F#2,C3,D3,D#3,E3", StringUtils::join(noteStrings, ","));
}

TEST(Music_Note, isLower) {
  ASSERT_TRUE(Note::of("A5") < Note::of("A6"));
  ASSERT_TRUE(Note::of("A5") < Note::of("A#5"));
  ASSERT_FALSE(Note::of("A5") < Note::of("Ab5"));
}

TEST(Music_Note, isHigher) {
  ASSERT_TRUE(Note::of("G6") > Note::of("F6"));
  ASSERT_TRUE(Note::of("A5") > Note::of("A4"));
  ASSERT_TRUE(Note::of("A5") > Note::of("Ab5"));
  ASSERT_FALSE(Note::of("A5") > Note::of("A#5"));
}

TEST(Music_Note, atonal) {
  ASSERT_EQ(PitchClass::Atonal, Note::atonal().pitchClass);
}

TEST(Music_Note, isAtonal) {
  ASSERT_TRUE(Note::atonal().isAtonal());
  ASSERT_TRUE(Note::of("X0").isAtonal());
}

/**
 * NC sections should not cache notes from the previous section https://www.pivotaltracker.com/story/show/179409784
 */
TEST(Music_Note, IfValid) {
  ASSERT_EQ(PitchClass::G, Note::ifValid("G6").value().pitchClass);
  ASSERT_EQ(PitchClass::Gs, Note::ifValid("G#6").value().pitchClass);
  ASSERT_EQ(PitchClass::Atonal, Note::ifValid("X").value().pitchClass);
  ASSERT_FALSE(Note::ifValid("(None)").has_value());
  ASSERT_FALSE(Note::ifValid("abc").has_value());
}

/**
 * NC sections should not cache notes from the previous section https://www.pivotaltracker.com/story/show/179409784
 */
TEST(Music_Note, IfTonal) {
  ASSERT_EQ(PitchClass::G, Note::ifTonal("G6").value().pitchClass);
  ASSERT_EQ(PitchClass::Gs, Note::ifTonal("G#6").value().pitchClass);
  ASSERT_FALSE(Note::ifTonal("X").has_value());
  ASSERT_FALSE(Note::ifTonal("(None)").has_value());
  ASSERT_FALSE(Note::ifTonal("abc").has_value());
}

/**
 * NC sections should not cache notes from the previous section https://www.pivotaltracker.com/story/show/179409784
 */
TEST(Music_Note, IsValid) {
  ASSERT_TRUE(Note::isValid("G6"));
  ASSERT_TRUE(Note::isValid("G#6"));
  ASSERT_TRUE(Note::isValid("X"));
  ASSERT_FALSE(Note::isValid("(None)"));
  ASSERT_FALSE(Note::isValid("abc"));
}

TEST(Music_Note, containsAnyValidNotes) {
  ASSERT_TRUE(Note::containsAnyValidNotes("C, D, E"));
  ASSERT_TRUE(Note::containsAnyValidNotes("X"));// drum event note
  ASSERT_TRUE(Note::containsAnyValidNotes("C3, D3, E3"));
  ASSERT_FALSE(Note::containsAnyValidNotes("Y, Z"));
  ASSERT_FALSE(Note::containsAnyValidNotes("(None)"));// NC voicing
}

TEST(Music_Note, median) {
  ASSERT_FALSE(Note::median(std::nullopt, std::nullopt).has_value());
  TestHelpers::assertNote("C5", Note::median(Note::of("C5"), std::nullopt).value());
  TestHelpers::assertNote("G#5", Note::median(std::nullopt, Note::of("G#5")).value());
  TestHelpers::assertNote("E5", Note::median(Note::of("C5"), Note::of("G#5")).value());
}

TEST(Music_Note, nextUp) {
  ASSERT_EQ(PitchClass::Atonal, Note::of("X").nextUp(PitchClass::C).pitchClass);
  TestHelpers::assertNote("C4", Note::of("B3").nextUp(PitchClass::C));
}

TEST(Music_Note, nextDown) {
  ASSERT_EQ(PitchClass::Atonal, Note::of("X").nextDown(PitchClass::C).pitchClass);
  TestHelpers::assertNote("C4", Note::of("D4").nextDown(PitchClass::C));
}