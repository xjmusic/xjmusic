// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "AssertionHelpers.h"

namespace XJ {

  void assertNote(const std::string &expect, const Note &actual) {
    ASSERT_EQ(Note::of(expect).pitchClass, actual.pitchClass);
    ASSERT_EQ(Note::of(expect).octave, actual.octave);
  }

}