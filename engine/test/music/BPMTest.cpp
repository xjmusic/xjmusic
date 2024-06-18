// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/music/BPM.h"

using namespace XJ;


TEST(Music_BPM, velocity) {
  ASSERT_NEAR(1, BPM::velocity(60), 0);
  ASSERT_NEAR(0.5, BPM::velocity(120), 0);
  ASSERT_NEAR(0.495, BPM::velocity(121), 0.001);
}


TEST(Music_BPM, beatsNanos) {
  ASSERT_EQ(100000000000L, BPM::beatsNanos(100, 60));
  ASSERT_EQ(31735537190L, BPM::beatsNanos(64, 121));
}
