// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/music/Octave.h"

using namespace Music;

TEST(Music_Octave, of) {
  ASSERT_EQ(0, octaveOf("F"));
  ASSERT_EQ(2, octaveOf("Bb2"));
  ASSERT_EQ(3, octaveOf("D#3"));
  ASSERT_EQ(5, octaveOf("D5"));
  ASSERT_EQ(-2, octaveOf("D-2"));
  ASSERT_EQ(-2, octaveOf("D--2"));
}
