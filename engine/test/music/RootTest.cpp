// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/music/Root.h"

#include "../_helper/TestHelpers.h"

using namespace XJ;

/**
 * Assert the pitch class and remaining text of a Root object
 *
 * @param text                to get root from
 * @param expectPitchClass    expected
 * @param expectRemainingText expected
 * @throws Exception on failure
 */
static void assertRoot(const std::string& text, const PitchClass expectPitchClass, const std::string& expectRemainingText) {
  const Root root = Root::of(text);
  ASSERT_EQ(expectPitchClass, root.pitchClass);
  ASSERT_EQ(expectRemainingText, root.remainingText);
}

TEST(Music_Root, RootOfTest) {
  assertRoot("C", PitchClass::C, "");
  assertRoot("Cmaj", PitchClass::C, "maj");
  assertRoot("B♭min", PitchClass::As, "min");
  assertRoot("C#dim", PitchClass::Cs, "dim");
  assertRoot("JAMS", PitchClass::Atonal, "JAMS");
  assertRoot("CM6add9", PitchClass::C, "M6add9");
  assertRoot("C dom7b9/13", PitchClass::C, "dom7b9/13");
  assertRoot("C+∆", PitchClass::C, "+∆");
  assertRoot("C Ø11", PitchClass::C, "Ø11");
  assertRoot("C+M7", PitchClass::C, "+M7");
  assertRoot("C+∆", PitchClass::C, "+∆");
  assertRoot("C∆#5", PitchClass::C, "∆#5");
  assertRoot("C+♮7", PitchClass::C, "+♮7");
  assertRoot("C°", PitchClass::C, "°");
}

