// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/music/PitchClass.h"

using namespace XJ;

void assertPitchClassOf(
    const std::string &name,
    const PitchClass expectPitchClass,
    const std::string &expectStringSharp,
    const std::string &expectStringFlat
) {
  const PitchClass pitchClass = pitchClassOf(name);
  ASSERT_EQ(expectPitchClass, pitchClass) << name << " -> " << stringOf(pitchClass, Natural);
  ASSERT_EQ(expectStringSharp, stringOf(pitchClass, Sharp)) << name << " -> " << stringOf(pitchClass, Sharp);
  ASSERT_EQ(expectStringFlat, stringOf(pitchClass, Flat)) << name << " -> " << stringOf(pitchClass, Flat);
}

TEST(Music_PitchClass, PitchClassOf) {
  assertPitchClassOf("C", PitchClass::C, "C", "C");
  assertPitchClassOf("C#", PitchClass::Cs, "C#", "Db");
  assertPitchClassOf("Cb", PitchClass::B, "B", "B");
  assertPitchClassOf("D", PitchClass::D, "D", "D");
  assertPitchClassOf("D#", PitchClass::Ds, "D#", "Eb");
  assertPitchClassOf("D♭", PitchClass::Cs, "C#", "Db");
  assertPitchClassOf("E", PitchClass::E, "E", "E");
  assertPitchClassOf("E#", PitchClass::F, "F", "F");
  assertPitchClassOf("E♭", PitchClass::Ds, "D#", "Eb");
  assertPitchClassOf("F", PitchClass::F, "F", "F");
  assertPitchClassOf("F#", PitchClass::Fs, "F#", "Gb");
  assertPitchClassOf("F♭", PitchClass::E, "E", "E");
  assertPitchClassOf("G", PitchClass::G, "G", "G");
  assertPitchClassOf("G♯", PitchClass::Gs, "G#", "Ab");
  assertPitchClassOf("Gb", PitchClass::Fs, "F#", "Gb");
  assertPitchClassOf("A", PitchClass::A, "A", "A");
  assertPitchClassOf("A#", PitchClass::As, "A#", "Bb");
  assertPitchClassOf("Ab", PitchClass::Gs, "G#", "Ab");
  assertPitchClassOf("B", PitchClass::B, "B", "B");
  assertPitchClassOf("B#", PitchClass::C, "C", "C");
  assertPitchClassOf("E♭", PitchClass::Ds, "D#", "Eb");
  assertPitchClassOf("Bb", PitchClass::As, "A#", "Bb");
  assertPitchClassOf("z", PitchClass::Atonal, "X", "X");
  assertPitchClassOf("zzzz", PitchClass::Atonal, "X", "X");
  assertPitchClassOf("C minor", PitchClass::C, "C", "C");
  assertPitchClassOf("C#5", PitchClass::Cs, "C#", "Db");
}

TEST(Music_PitchClass, StringOf) {
  ASSERT_EQ("C#", stringOf(PitchClass::Cs, Sharp));
  ASSERT_EQ("Db", stringOf(PitchClass::Cs, Flat));
  ASSERT_EQ("C#", stringOf(PitchClass::Cs, Natural));
}