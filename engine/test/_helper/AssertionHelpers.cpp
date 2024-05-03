// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "AssertionHelpers.h"

void assertNote(const std::string &expect, const Music::Note &actual) {
  ASSERT_EQ(Music::Note::of(expect).pitchClass, actual.pitchClass);
  ASSERT_EQ(Music::Note::of(expect).octave, actual.octave);
}