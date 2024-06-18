// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/music/Bar.h"

using namespace XJ;

TEST(Music_Bar, Instance_getBeats_setBeats) {
  ASSERT_EQ(4, Bar::of(4).beats);
}

TEST(Music_Bar, Instance_failsFromNull) {
  EXPECT_THROW({
                 try {
                   Bar::of(-1);
                 }
                 catch (const std::runtime_error &e) {
                   // and this tests that it has the correct message
                   EXPECT_STREQ("Bar must beats greater than zero!", e.what());
                   throw;
                 }
               }, std::runtime_error);
}


TEST(Music_Bar, ComputeSubsectionBeats) {
  auto bar_3beat = Bar::of(3);
  auto bar_4beat = Bar::of(4);

  ASSERT_EQ(12, bar_3beat.computeSubsectionBeats(12));
  ASSERT_EQ(12, bar_3beat.computeSubsectionBeats(12));
  ASSERT_EQ(12, bar_3beat.computeSubsectionBeats(24));
  ASSERT_EQ(12, bar_3beat.computeSubsectionBeats(48));
  ASSERT_EQ(12, bar_4beat.computeSubsectionBeats(12));
  ASSERT_EQ(12, bar_4beat.computeSubsectionBeats(12));
  ASSERT_EQ(12, bar_4beat.computeSubsectionBeats(24));
  ASSERT_EQ(16, bar_4beat.computeSubsectionBeats(16));
  ASSERT_EQ(16, bar_4beat.computeSubsectionBeats(16));
  ASSERT_EQ(12, bar_4beat.computeSubsectionBeats(48));
  ASSERT_EQ(16, bar_4beat.computeSubsectionBeats(64));
  ASSERT_EQ(16, bar_4beat.computeSubsectionBeats(64));
  ASSERT_EQ(2, bar_3beat.computeSubsectionBeats(2));
  ASSERT_EQ(4, bar_3beat.computeSubsectionBeats(4));
}

