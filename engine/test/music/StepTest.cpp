// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/music/Step.h"

using namespace XJ;

TEST(Music_Step, To) {
  const Step step = Step::to(B, -1);

  ASSERT_EQ(-1, step.deltaOctave);
  ASSERT_EQ(PitchClass::B, step.pitchClass);
}

TEST(Music_Step, Delta) {
  ASSERT_EQ(5, Step::delta(PitchClass::Cs, PitchClass::Fs));
  ASSERT_EQ(-5, Step::delta(PitchClass::Fs, PitchClass::Cs));
  ASSERT_EQ(2, Step::delta(PitchClass::Gs, PitchClass::As));
  ASSERT_EQ(-3, Step::delta(PitchClass::C, PitchClass::A));
  ASSERT_EQ(4, Step::delta(PitchClass::D, PitchClass::Fs));
  ASSERT_EQ(-6, Step::delta(PitchClass::F, PitchClass::B));
  ASSERT_EQ(0, Step::delta(PitchClass::Cs, PitchClass::Atonal));
  ASSERT_EQ(0, Step::delta(PitchClass::Atonal, PitchClass::Cs));
  ASSERT_EQ(0, Step::delta(PitchClass::Atonal, PitchClass::Atonal));
}

