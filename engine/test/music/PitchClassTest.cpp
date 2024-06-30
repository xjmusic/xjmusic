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
  assertPitchClassOf("C", C, "C", "C");
  assertPitchClassOf("C#", Cs, "C#", "Db");
  assertPitchClassOf("Cb", B, "B", "B");
  assertPitchClassOf("D", D, "D", "D");
  assertPitchClassOf("D#", Ds, "D#", "Eb");
  assertPitchClassOf("D♭", Cs, "C#", "Db");
  assertPitchClassOf("E", E, "E", "E");
  assertPitchClassOf("E#", F, "F", "F");
  assertPitchClassOf("E♭", Ds, "D#", "Eb");
  assertPitchClassOf("F", F, "F", "F");
  assertPitchClassOf("F#", Fs, "F#", "Gb");
  assertPitchClassOf("F♭", E, "E", "E");
  assertPitchClassOf("G", G, "G", "G");
  assertPitchClassOf("G♯", Gs, "G#", "Ab");
  assertPitchClassOf("Gb", Fs, "F#", "Gb");
  assertPitchClassOf("A", A, "A", "A");
  assertPitchClassOf("A#", As, "A#", "Bb");
  assertPitchClassOf("Ab", Gs, "G#", "Ab");
  assertPitchClassOf("B", B, "B", "B");
  assertPitchClassOf("B#", C, "C", "C");
  assertPitchClassOf("E♭", Ds, "D#", "Eb");
  assertPitchClassOf("Bb", As, "A#", "Bb");
  assertPitchClassOf("z", Atonal, "X", "X");
  assertPitchClassOf("zzzz", Atonal, "X", "X");
  assertPitchClassOf("C minor", C, "C", "C");
  assertPitchClassOf("C#5", Cs, "C#", "Db");
}

TEST(Music_PitchClass, StringOf) {
  ASSERT_EQ("C#", stringOf(PitchClass::Cs, Sharp));
  ASSERT_EQ("Db", stringOf(PitchClass::Cs, Flat));
  ASSERT_EQ("C#", stringOf(PitchClass::Cs, Natural));
}