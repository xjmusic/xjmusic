// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.

#include <gtest/gtest.h>

#include "xjmusic/music/Tuning.h"

#include "../_helper/TestHelpers.h"

using namespace XJ;

/**
 [#255] Note pitch is calculated at 32-bit floating point precision, based on root note configured in environment parameters.
 <p>
 Reference: http://www.phy.mtu.edu/~suits/notefreqs.html
 */

TEST(Music_Tuning, pitch) {
  Tuning tuning = Tuning::atA4(432);

  ASSERT_NEAR(16.05, tuning.pitch(Note::of("C0")), .01);
  ASSERT_NEAR(17.01, tuning.pitch(Note::of("C#0")), .01);
  ASSERT_NEAR(18.02, tuning.pitch(Note::of("D0")), .01);
  ASSERT_NEAR(19.09, tuning.pitch(Note::of("D#0")), .01);
  ASSERT_NEAR(20.23, tuning.pitch(Note::of("E0")), .01);
  ASSERT_NEAR(21.43, tuning.pitch(Note::of("F0")), .01);
  ASSERT_NEAR(22.70, tuning.pitch(Note::of("F#0")), .01);
  ASSERT_NEAR(24.05, tuning.pitch(Note::of("G0")), .01);
  ASSERT_NEAR(25.48, tuning.pitch(Note::of("G#0")), .01);
  ASSERT_NEAR(27.00, tuning.pitch(Note::of("A0")), .01);
  ASSERT_NEAR(28.61, tuning.pitch(Note::of("A#0")), .01);
  ASSERT_NEAR(30.31, tuning.pitch(Note::of("B0")), .01);
  ASSERT_NEAR(32.11, tuning.pitch(Note::of("C1")), .01);
  ASSERT_NEAR(34.02, tuning.pitch(Note::of("C#1")), .01);
  ASSERT_NEAR(36.04, tuning.pitch(Note::of("D1")), .01);
  ASSERT_NEAR(38.18, tuning.pitch(Note::of("D#1")), .01);
  ASSERT_NEAR(40.45, tuning.pitch(Note::of("E1")), .01);
  ASSERT_NEAR(42.86, tuning.pitch(Note::of("F1")), .01);
  ASSERT_NEAR(45.41, tuning.pitch(Note::of("F#1")), .01);
  ASSERT_NEAR(48.11, tuning.pitch(Note::of("G1")), .01);
  ASSERT_NEAR(50.97, tuning.pitch(Note::of("G#1")), .01);
  ASSERT_NEAR(54.00, tuning.pitch(Note::of("A1")), .01);
  ASSERT_NEAR(57.21, tuning.pitch(Note::of("A#1")), .01);
  ASSERT_NEAR(60.61, tuning.pitch(Note::of("B1")), .01);
  ASSERT_NEAR(64.22, tuning.pitch(Note::of("C2")), .01);
  ASSERT_NEAR(68.04, tuning.pitch(Note::of("C#2")), .01);
  ASSERT_NEAR(72.08, tuning.pitch(Note::of("D2")), .01);
  ASSERT_NEAR(76.37, tuning.pitch(Note::of("D#2")), .01);
  ASSERT_NEAR(80.91, tuning.pitch(Note::of("E2")), .01);
  ASSERT_NEAR(85.72, tuning.pitch(Note::of("F2")), .01);
  ASSERT_NEAR(90.82, tuning.pitch(Note::of("F#2")), .01);
  ASSERT_NEAR(96.22, tuning.pitch(Note::of("G2")), .01);
  ASSERT_NEAR(101.94, tuning.pitch(Note::of("G#2")), .01);
  ASSERT_NEAR(108.00, tuning.pitch(Note::of("A2")), .01);
  ASSERT_NEAR(114.42, tuning.pitch(Note::of("A#2")), .01);
  ASSERT_NEAR(121.23, tuning.pitch(Note::of("B2")), .01);
  ASSERT_NEAR(128.43, tuning.pitch(Note::of("C3")), .01);
  ASSERT_NEAR(136.07, tuning.pitch(Note::of("C#3")), .01);
  ASSERT_NEAR(144.16, tuning.pitch(Note::of("D3")), .01);
  ASSERT_NEAR(152.74, tuning.pitch(Note::of("D#3")), .01);
  ASSERT_NEAR(161.82, tuning.pitch(Note::of("E3")), .01);
  ASSERT_NEAR(171.44, tuning.pitch(Note::of("F3")), .01);
  ASSERT_NEAR(181.63, tuning.pitch(Note::of("F#3")), .01);
  ASSERT_NEAR(192.43, tuning.pitch(Note::of("G3")), .01);
  ASSERT_NEAR(203.88, tuning.pitch(Note::of("G#3")), .01);
  ASSERT_NEAR(216.00, tuning.pitch(Note::of("A3")), .01);
  ASSERT_NEAR(228.84, tuning.pitch(Note::of("A#3")), .01);
  ASSERT_NEAR(242.45, tuning.pitch(Note::of("B3")), .01);
  ASSERT_NEAR(256.87, tuning.pitch(Note::of("C4")), .01);
  ASSERT_NEAR(272.14, tuning.pitch(Note::of("C#4")), .01);
  ASSERT_NEAR(288.33, tuning.pitch(Note::of("D4")), .01);
  ASSERT_NEAR(305.47, tuning.pitch(Note::of("D#4")), .01);
  ASSERT_NEAR(323.63, tuning.pitch(Note::of("E4")), .01);
  ASSERT_NEAR(342.88, tuning.pitch(Note::of("F4")), .01);
  ASSERT_NEAR(363.27, tuning.pitch(Note::of("F#4")), .01);
  ASSERT_NEAR(384.87, tuning.pitch(Note::of("G4")), .01);
  ASSERT_NEAR(407.75, tuning.pitch(Note::of("G#4")), .01);
  ASSERT_NEAR(432.00, tuning.pitch(Note::of("A4")), .01);
  ASSERT_NEAR(457.69, tuning.pitch(Note::of("A#4")), .01);
  ASSERT_NEAR(484.90, tuning.pitch(Note::of("B4")), .01);
  ASSERT_NEAR(513.74, tuning.pitch(Note::of("C5")), .01);
  ASSERT_NEAR(544.29, tuning.pitch(Note::of("C#5")), .01);
  ASSERT_NEAR(576.65, tuning.pitch(Note::of("D5")), .01);
  ASSERT_NEAR(610.94, tuning.pitch(Note::of("D#5")), .01);
  ASSERT_NEAR(647.27, tuning.pitch(Note::of("E5")), .01);
  ASSERT_NEAR(685.76, tuning.pitch(Note::of("F5")), .01);
  ASSERT_NEAR(726.53, tuning.pitch(Note::of("F#5")), .01);
  ASSERT_NEAR(769.74, tuning.pitch(Note::of("G5")), .01);
  ASSERT_NEAR(815.51, tuning.pitch(Note::of("G#5")), .01);
  ASSERT_NEAR(864.00, tuning.pitch(Note::of("A5")), .01);
  ASSERT_NEAR(915.38, tuning.pitch(Note::of("A#5")), .01);
  ASSERT_NEAR(969.81, tuning.pitch(Note::of("B5")), .01);
  ASSERT_NEAR(1027.47, tuning.pitch(Note::of("C6")), .01);
  ASSERT_NEAR(1088.57, tuning.pitch(Note::of("C#6")), .01);
  ASSERT_NEAR(1153.30, tuning.pitch(Note::of("D6")), .01);
  ASSERT_NEAR(1221.88, tuning.pitch(Note::of("D#6")), .01);
  ASSERT_NEAR(1294.54, tuning.pitch(Note::of("E6")), .01);
  ASSERT_NEAR(1371.51, tuning.pitch(Note::of("F6")), .01);
  ASSERT_NEAR(1453.07, tuning.pitch(Note::of("F#6")), .01);
  ASSERT_NEAR(1539.47, tuning.pitch(Note::of("G6")), .01);
  ASSERT_NEAR(1631.01, tuning.pitch(Note::of("G#6")), .01);
  ASSERT_NEAR(1728.00, tuning.pitch(Note::of("A6")), .01);
  ASSERT_NEAR(1830.75, tuning.pitch(Note::of("A#6")), .01);
  ASSERT_NEAR(1939.61, tuning.pitch(Note::of("B6")), .01);
  ASSERT_NEAR(2054.95, tuning.pitch(Note::of("C7")), .01);
  ASSERT_NEAR(2177.14, tuning.pitch(Note::of("C#7")), .01);
  ASSERT_NEAR(2306.60, tuning.pitch(Note::of("D7")), .01);
  ASSERT_NEAR(2443.76, tuning.pitch(Note::of("D#7")), .01);
  ASSERT_NEAR(2589.07, tuning.pitch(Note::of("E7")), .01);
  ASSERT_NEAR(2743.03, tuning.pitch(Note::of("F7")), .01);
  ASSERT_NEAR(2906.14, tuning.pitch(Note::of("F#7")), .01);
  ASSERT_NEAR(3078.95, tuning.pitch(Note::of("G7")), .01);
  ASSERT_NEAR(3262.03, tuning.pitch(Note::of("G#7")), .01);
  ASSERT_NEAR(3456.00, tuning.pitch(Note::of("A7")), .01);
  ASSERT_NEAR(3661.50, tuning.pitch(Note::of("A#7")), .01);
  ASSERT_NEAR(3879.23, tuning.pitch(Note::of("B7")), .01);
  ASSERT_NEAR(4109.90, tuning.pitch(Note::of("C8")), .01);
  ASSERT_NEAR(4354.29, tuning.pitch(Note::of("C#8")), .01);
  ASSERT_NEAR(4613.21, tuning.pitch(Note::of("D8")), .01);
  ASSERT_NEAR(4887.52, tuning.pitch(Note::of("D#8")), .01);
  ASSERT_NEAR(5178.15, tuning.pitch(Note::of("E8")), .01);
  ASSERT_NEAR(5486.06, tuning.pitch(Note::of("F8")), .01);
  ASSERT_NEAR(5812.28, tuning.pitch(Note::of("F#8")), .01);
  ASSERT_NEAR(6157.89, tuning.pitch(Note::of("G8")), .01);
  ASSERT_NEAR(6524.06, tuning.pitch(Note::of("G#8")), .01);
  ASSERT_NEAR(6912.00, tuning.pitch(Note::of("A8")), .01);
  ASSERT_NEAR(7323.01, tuning.pitch(Note::of("A#8")), .01);
  ASSERT_NEAR(7758.46, tuning.pitch(Note::of("B8")), .01);
}

TEST(Music_Tuning, note) {
  Tuning tuning = Tuning::atA4(432);

  ASSERT_TRUE(Note::of("C0") == tuning.getTones(16.05));
  ASSERT_TRUE(Note::of("C#0") == tuning.getTones(17.00));
  ASSERT_TRUE(Note::of("D0") == tuning.getTones(18.02));
  ASSERT_TRUE(Note::of("D#0") == tuning.getTones(19.09));
  ASSERT_TRUE(Note::of("E0") == tuning.getTones(20.22));
  ASSERT_TRUE(Note::of("F0") == tuning.getTones(21.42));
  ASSERT_TRUE(Note::of("F#0") == tuning.getTones(22.70));
  ASSERT_TRUE(Note::of("G0") == tuning.getTones(24.05));
  ASSERT_TRUE(Note::of("G#0") == tuning.getTones(25.48));
  ASSERT_TRUE(Note::of("A0") == tuning.getTones(26.99));
  ASSERT_TRUE(Note::of("A#0") == tuning.getTones(28.60));
  ASSERT_TRUE(Note::of("B0") == tuning.getTones(30.30));
  ASSERT_TRUE(Note::of("C1") == tuning.getTones(32.10));
  ASSERT_TRUE(Note::of("C#1") == tuning.getTones(34.01));
  ASSERT_TRUE(Note::of("D1") == tuning.getTones(36.03));
  ASSERT_TRUE(Note::of("D#1") == tuning.getTones(38.17));
  ASSERT_TRUE(Note::of("E1") == tuning.getTones(40.44));
  ASSERT_TRUE(Note::of("F1") == tuning.getTones(42.85));
  ASSERT_TRUE(Note::of("F#1") == tuning.getTones(45.40));
  ASSERT_TRUE(Note::of("G1") == tuning.getTones(48.10));
  ASSERT_TRUE(Note::of("G#1") == tuning.getTones(50.96));
  ASSERT_TRUE(Note::of("A1") == tuning.getTones(53.99));
  ASSERT_TRUE(Note::of("A#1") == tuning.getTones(57.20));
  ASSERT_TRUE(Note::of("B1") == tuning.getTones(60.60));
  ASSERT_TRUE(Note::of("C2") == tuning.getTones(64.21));
  ASSERT_TRUE(Note::of("C#2") == tuning.getTones(68.03));
  ASSERT_TRUE(Note::of("D2") == tuning.getTones(72.07));
  ASSERT_TRUE(Note::of("D#2") == tuning.getTones(76.36));
  ASSERT_TRUE(Note::of("E2") == tuning.getTones(80.90));
  ASSERT_TRUE(Note::of("F2") == tuning.getTones(85.71));
  ASSERT_TRUE(Note::of("F#2") == tuning.getTones(90.81));
  ASSERT_TRUE(Note::of("G2") == tuning.getTones(96.21));
  ASSERT_TRUE(Note::of("G#2") == tuning.getTones(101.93));
  ASSERT_TRUE(Note::of("A2") == tuning.getTones(107.99));
  ASSERT_TRUE(Note::of("A#2") == tuning.getTones(114.41));
  ASSERT_TRUE(Note::of("B2") == tuning.getTones(121.22));
  ASSERT_TRUE(Note::of("C3") == tuning.getTones(128.42));
  ASSERT_TRUE(Note::of("C#3") == tuning.getTones(136.06));
  ASSERT_TRUE(Note::of("D3") == tuning.getTones(144.15));
  ASSERT_TRUE(Note::of("D#3") == tuning.getTones(152.73));
  ASSERT_TRUE(Note::of("E3") == tuning.getTones(161.81));
  ASSERT_TRUE(Note::of("F3") == tuning.getTones(171.43));
  ASSERT_TRUE(Note::of("F#3") == tuning.getTones(181.62));
  ASSERT_TRUE(Note::of("G3") == tuning.getTones(192.43));
  ASSERT_TRUE(Note::of("G#3") == tuning.getTones(203.87));
  ASSERT_TRUE(Note::of("A3") == tuning.getTones(215.99));
  ASSERT_TRUE(Note::of("A#3") == tuning.getTones(228.83));
  ASSERT_TRUE(Note::of("B3") == tuning.getTones(242.44));
  ASSERT_TRUE(Note::of("C4") == tuning.getTones(256.86));
  ASSERT_TRUE(Note::of("C#4") == tuning.getTones(272.13));
  ASSERT_TRUE(Note::of("D4") == tuning.getTones(288.32));
  ASSERT_TRUE(Note::of("D#4") == tuning.getTones(305.46));
  ASSERT_TRUE(Note::of("E4") == tuning.getTones(323.62));
  ASSERT_TRUE(Note::of("F4") == tuning.getTones(342.87));
  ASSERT_TRUE(Note::of("F#4") == tuning.getTones(363.26));
  ASSERT_TRUE(Note::of("G4") == tuning.getTones(384.86));
  ASSERT_TRUE(Note::of("G#4") == tuning.getTones(407.75));
  ASSERT_TRUE(Note::of("A4") == tuning.getTones(432.00));
  ASSERT_TRUE(Note::of("A#4") == tuning.getTones(457.69));
  ASSERT_TRUE(Note::of("B4") == tuning.getTones(484.91));
  ASSERT_TRUE(Note::of("C5") == tuning.getTones(513.74));
  ASSERT_TRUE(Note::of("C#5") == tuning.getTones(544.29));
  ASSERT_TRUE(Note::of("D5") == tuning.getTones(576.66));
  ASSERT_TRUE(Note::of("D#5") == tuning.getTones(610.95));
  ASSERT_TRUE(Note::of("E5") == tuning.getTones(647.28));
  ASSERT_TRUE(Note::of("F5") == tuning.getTones(685.77));
  ASSERT_TRUE(Note::of("F#5") == tuning.getTones(726.54));
  ASSERT_TRUE(Note::of("G5") == tuning.getTones(769.75));
  ASSERT_TRUE(Note::of("G#5") == tuning.getTones(815.52));
  ASSERT_TRUE(Note::of("A5") == tuning.getTones(864.01));
  ASSERT_TRUE(Note::of("A#5") == tuning.getTones(915.39));
  ASSERT_TRUE(Note::of("B5") == tuning.getTones(969.82));
  ASSERT_TRUE(Note::of("C6") == tuning.getTones(1027.48));
  ASSERT_TRUE(Note::of("C#6") == tuning.getTones(1088.58));
  ASSERT_TRUE(Note::of("D6") == tuning.getTones(1153.31));
  ASSERT_TRUE(Note::of("D#6") == tuning.getTones(1221.89));
  ASSERT_TRUE(Note::of("E6") == tuning.getTones(1294.55));
  ASSERT_TRUE(Note::of("F6") == tuning.getTones(1371.52));
  ASSERT_TRUE(Note::of("F#6") == tuning.getTones(1453.08));
  ASSERT_TRUE(Note::of("G6") == tuning.getTones(1539.48));
  ASSERT_TRUE(Note::of("G#6") == tuning.getTones(1631.02));
  ASSERT_TRUE(Note::of("A6") == tuning.getTones(1728.01));
  ASSERT_TRUE(Note::of("A#6") == tuning.getTones(1830.76));
  ASSERT_TRUE(Note::of("B6") == tuning.getTones(1939.62));
  ASSERT_TRUE(Note::of("C7") == tuning.getTones(2054.96));
  ASSERT_TRUE(Note::of("C#7") == tuning.getTones(2177.15));
  ASSERT_TRUE(Note::of("D7") == tuning.getTones(2306.61));
  ASSERT_TRUE(Note::of("D#7") == tuning.getTones(2443.77));
  ASSERT_TRUE(Note::of("E7") == tuning.getTones(2589.08));
  ASSERT_TRUE(Note::of("F7") == tuning.getTones(2743.04));
  ASSERT_TRUE(Note::of("F#7") == tuning.getTones(2906.15));
  ASSERT_TRUE(Note::of("G7") == tuning.getTones(3078.96));
  ASSERT_TRUE(Note::of("G#7") == tuning.getTones(3262.04));
  ASSERT_TRUE(Note::of("A7") == tuning.getTones(3456.01));
  ASSERT_TRUE(Note::of("A#7") == tuning.getTones(3661.51));
  ASSERT_TRUE(Note::of("B7") == tuning.getTones(3879.24));
  ASSERT_TRUE(Note::of("C8") == tuning.getTones(4109.91));
  ASSERT_TRUE(Note::of("C#8") == tuning.getTones(4354.30));
  ASSERT_TRUE(Note::of("D8") == tuning.getTones(4613.22));
  ASSERT_TRUE(Note::of("D#8") == tuning.getTones(4887.53));
  ASSERT_TRUE(Note::of("E8") == tuning.getTones(5178.16));
  ASSERT_TRUE(Note::of("F8") == tuning.getTones(5486.07));
  ASSERT_TRUE(Note::of("F#8") == tuning.getTones(5812.29));
  ASSERT_TRUE(Note::of("G8") == tuning.getTones(6157.90));
  ASSERT_TRUE(Note::of("G#8") == tuning.getTones(6524.07));
  ASSERT_TRUE(Note::of("A8") == tuning.getTones(6912.01));
  ASSERT_TRUE(Note::of("A#8") == tuning.getTones(7323.02));
  ASSERT_TRUE(Note::of("B8") == tuning.getTones(7758.47));
}

TEST(Music_Tuning, deltaFromRootPitch) {
  Tuning tuning = Tuning::atA4(432);

  ASSERT_EQ(-57, tuning.deltaFromRootPitch(16.05));
  ASSERT_EQ(-56, tuning.deltaFromRootPitch(17.00));
  ASSERT_EQ(-55, tuning.deltaFromRootPitch(18.02));
  ASSERT_EQ(-54, tuning.deltaFromRootPitch(19.09));
  ASSERT_EQ(-53, tuning.deltaFromRootPitch(20.22));
  ASSERT_EQ(-52, tuning.deltaFromRootPitch(21.42));
  ASSERT_EQ(-51, tuning.deltaFromRootPitch(22.70));
  ASSERT_EQ(-50, tuning.deltaFromRootPitch(24.05));
  ASSERT_EQ(-49, tuning.deltaFromRootPitch(25.48));
  ASSERT_EQ(-48, tuning.deltaFromRootPitch(26.99));
  ASSERT_EQ(-47, tuning.deltaFromRootPitch(28.60));
  ASSERT_EQ(-46, tuning.deltaFromRootPitch(30.30));
  ASSERT_EQ(-45, tuning.deltaFromRootPitch(32.10));
  ASSERT_EQ(-44, tuning.deltaFromRootPitch(34.01));
  ASSERT_EQ(-43, tuning.deltaFromRootPitch(36.03));
  ASSERT_EQ(-42, tuning.deltaFromRootPitch(38.17));
  ASSERT_EQ(-41, tuning.deltaFromRootPitch(40.44));
  ASSERT_EQ(-40, tuning.deltaFromRootPitch(42.85));
  ASSERT_EQ(-39, tuning.deltaFromRootPitch(45.40));
  ASSERT_EQ(-38, tuning.deltaFromRootPitch(48.10));
  ASSERT_EQ(-37, tuning.deltaFromRootPitch(50.96));
  ASSERT_EQ(-36, tuning.deltaFromRootPitch(53.99));
  ASSERT_EQ(-35, tuning.deltaFromRootPitch(57.20));
  ASSERT_EQ(-34, tuning.deltaFromRootPitch(60.60));
  ASSERT_EQ(-33, tuning.deltaFromRootPitch(64.21));
  ASSERT_EQ(-32, tuning.deltaFromRootPitch(68.03));
  ASSERT_EQ(-31, tuning.deltaFromRootPitch(72.07));
  ASSERT_EQ(-30, tuning.deltaFromRootPitch(76.36));
  ASSERT_EQ(-29, tuning.deltaFromRootPitch(80.90));
  ASSERT_EQ(-28, tuning.deltaFromRootPitch(85.71));
  ASSERT_EQ(-27, tuning.deltaFromRootPitch(90.81));
  ASSERT_EQ(-26, tuning.deltaFromRootPitch(96.21));
  ASSERT_EQ(-25, tuning.deltaFromRootPitch(101.93));
  ASSERT_EQ(-24, tuning.deltaFromRootPitch(107.99));
  ASSERT_EQ(-23, tuning.deltaFromRootPitch(114.41));
  ASSERT_EQ(-22, tuning.deltaFromRootPitch(121.22));
  ASSERT_EQ(-21, tuning.deltaFromRootPitch(128.42));
  ASSERT_EQ(-20, tuning.deltaFromRootPitch(136.06));
  ASSERT_EQ(-19, tuning.deltaFromRootPitch(144.15));
  ASSERT_EQ(-18, tuning.deltaFromRootPitch(152.73));
  ASSERT_EQ(-17, tuning.deltaFromRootPitch(161.81));
  ASSERT_EQ(-16, tuning.deltaFromRootPitch(171.43));
  ASSERT_EQ(-15, tuning.deltaFromRootPitch(181.62));
  ASSERT_EQ(-14, tuning.deltaFromRootPitch(192.43));
  ASSERT_EQ(-13, tuning.deltaFromRootPitch(203.87));
  ASSERT_EQ(-12, tuning.deltaFromRootPitch(215.99));
  ASSERT_EQ(-11, tuning.deltaFromRootPitch(228.83));
  ASSERT_EQ(-10, tuning.deltaFromRootPitch(242.44));
  ASSERT_EQ(-9, tuning.deltaFromRootPitch(256.86));
  ASSERT_EQ(-8, tuning.deltaFromRootPitch(272.13));
  ASSERT_EQ(-7, tuning.deltaFromRootPitch(288.32));
  ASSERT_EQ(-6, tuning.deltaFromRootPitch(305.46));
  ASSERT_EQ(-5, tuning.deltaFromRootPitch(323.62));
  ASSERT_EQ(-4, tuning.deltaFromRootPitch(342.87));
  ASSERT_EQ(-3, tuning.deltaFromRootPitch(363.26));
  ASSERT_EQ(-2, tuning.deltaFromRootPitch(384.86));
  ASSERT_EQ(-1, tuning.deltaFromRootPitch(407.75));
  ASSERT_EQ(0, tuning.deltaFromRootPitch(432.00));
  ASSERT_EQ(1, tuning.deltaFromRootPitch(457.69));
  ASSERT_EQ(2, tuning.deltaFromRootPitch(484.91));
  ASSERT_EQ(3, tuning.deltaFromRootPitch(513.74));
  ASSERT_EQ(4, tuning.deltaFromRootPitch(544.29));
  ASSERT_EQ(5, tuning.deltaFromRootPitch(576.66));
  ASSERT_EQ(6, tuning.deltaFromRootPitch(610.95));
  ASSERT_EQ(7, tuning.deltaFromRootPitch(647.28));
  ASSERT_EQ(8, tuning.deltaFromRootPitch(685.77));
  ASSERT_EQ(9, tuning.deltaFromRootPitch(726.54));
  ASSERT_EQ(10, tuning.deltaFromRootPitch(769.75));
  ASSERT_EQ(11, tuning.deltaFromRootPitch(815.52));
  ASSERT_EQ(12, tuning.deltaFromRootPitch(864.01));
  ASSERT_EQ(13, tuning.deltaFromRootPitch(915.39));
  ASSERT_EQ(14, tuning.deltaFromRootPitch(969.82));
  ASSERT_EQ(15, tuning.deltaFromRootPitch(1027.48));
  ASSERT_EQ(16, tuning.deltaFromRootPitch(1088.58));
  ASSERT_EQ(17, tuning.deltaFromRootPitch(1153.31));
  ASSERT_EQ(18, tuning.deltaFromRootPitch(1221.89));
  ASSERT_EQ(19, tuning.deltaFromRootPitch(1294.55));
  ASSERT_EQ(20, tuning.deltaFromRootPitch(1371.52));
  ASSERT_EQ(21, tuning.deltaFromRootPitch(1453.08));
  ASSERT_EQ(22, tuning.deltaFromRootPitch(1539.48));
  ASSERT_EQ(23, tuning.deltaFromRootPitch(1631.02));
  ASSERT_EQ(24, tuning.deltaFromRootPitch(1728.01));
  ASSERT_EQ(25, tuning.deltaFromRootPitch(1830.76));
  ASSERT_EQ(26, tuning.deltaFromRootPitch(1939.62));
  ASSERT_EQ(27, tuning.deltaFromRootPitch(2054.96));
  ASSERT_EQ(28, tuning.deltaFromRootPitch(2177.15));
  ASSERT_EQ(29, tuning.deltaFromRootPitch(2306.61));
  ASSERT_EQ(30, tuning.deltaFromRootPitch(2443.77));
  ASSERT_EQ(31, tuning.deltaFromRootPitch(2589.08));
  ASSERT_EQ(32, tuning.deltaFromRootPitch(2743.04));
  ASSERT_EQ(33, tuning.deltaFromRootPitch(2906.15));
  ASSERT_EQ(34, tuning.deltaFromRootPitch(3078.96));
  ASSERT_EQ(35, tuning.deltaFromRootPitch(3262.04));
  ASSERT_EQ(36, tuning.deltaFromRootPitch(3456.01));
  ASSERT_EQ(37, tuning.deltaFromRootPitch(3661.51));
  ASSERT_EQ(38, tuning.deltaFromRootPitch(3879.24));
  ASSERT_EQ(39, tuning.deltaFromRootPitch(4109.91));
  ASSERT_EQ(40, tuning.deltaFromRootPitch(4354.30));
  ASSERT_EQ(41, tuning.deltaFromRootPitch(4613.22));
  ASSERT_EQ(42, tuning.deltaFromRootPitch(4887.53));
  ASSERT_EQ(43, tuning.deltaFromRootPitch(5178.16));
  ASSERT_EQ(44, tuning.deltaFromRootPitch(5486.07));
  ASSERT_EQ(45, tuning.deltaFromRootPitch(5812.29));
  ASSERT_EQ(46, tuning.deltaFromRootPitch(6157.90));
  ASSERT_EQ(47, tuning.deltaFromRootPitch(6524.07));
  ASSERT_EQ(48, tuning.deltaFromRootPitch(6912.01));
  ASSERT_EQ(49, tuning.deltaFromRootPitch(7323.02));
  ASSERT_EQ(50, tuning.deltaFromRootPitch(7758.47));
}

TEST(Music_Tuning, MusicalException_noRootNote) {
  EXPECT_THROW_WITH_MESSAGE(Tuning::at(Note::atonal(), 23.8), "Root note must have a pitch class (e.g. 'C')");
}

TEST(Music_Tuning, MusicalException_rootPitchTooHigh) {
  EXPECT_THROW_WITH_MESSAGE(Tuning::at(Note::of("G12"), 240000.0),
                            "Root pitch must be between 1 and 100000 (Hz)");
}

TEST(Music_Tuning, MusicalException_rootPitchTooLow) {
  EXPECT_THROW_WITH_MESSAGE(Tuning::at(Note::of("G12"), 0.02),
                            "Root pitch must be between 1 and 100000 (Hz)");
}

TEST(Music_Tuning, MusicalException_rootNoteInvalidPitchClass) {
  EXPECT_THROW_WITH_MESSAGE(Tuning::at(Note::of("moo"), 432), "Root note must have a pitch class (e.g. 'C')");
}

TEST(Music_Tuning, MusicalException_rootOctaveTooLow) {
  EXPECT_THROW_WITH_MESSAGE(Tuning::at(Note::of("G-2"), 1.48), "Root note octave must be between 0 and 15");
}

TEST(Music_Tuning, MusicalException_rootOctaveTooHigh) {
  EXPECT_THROW_WITH_MESSAGE(Tuning::at(Note::of("G73"), 97492.06), "Root note octave must be between 0 and 15");
}

