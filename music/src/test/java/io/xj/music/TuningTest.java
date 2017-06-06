// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.music;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 [#255] Note pitch is calculated at 32-bit floating point precision, based on root note configured in environment parameters.
 <p>
 Reference: http://www.phy.mtu.edu/~suits/notefreqs.html
 */
public class TuningTest {
  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void pitch() throws Exception {
    Tuning tuning = Tuning.atA4(432);

    assertEquals(16.05, tuning.pitch(Note.of("C0")), .01);
    assertEquals(17.01, tuning.pitch(Note.of("C#0")), .01);
    assertEquals(18.02, tuning.pitch(Note.of("D0")), .01);
    assertEquals(19.09, tuning.pitch(Note.of("D#0")), .01);
    assertEquals(20.23, tuning.pitch(Note.of("E0")), .01);
    assertEquals(21.43, tuning.pitch(Note.of("F0")), .01);
    assertEquals(22.70, tuning.pitch(Note.of("F#0")), .01);
    assertEquals(24.05, tuning.pitch(Note.of("G0")), .01);
    assertEquals(25.48, tuning.pitch(Note.of("G#0")), .01);
    assertEquals(27.00, tuning.pitch(Note.of("A0")), .01);
    assertEquals(28.61, tuning.pitch(Note.of("A#0")), .01);
    assertEquals(30.31, tuning.pitch(Note.of("B0")), .01);
    assertEquals(32.11, tuning.pitch(Note.of("C1")), .01);
    assertEquals(34.02, tuning.pitch(Note.of("C#1")), .01);
    assertEquals(36.04, tuning.pitch(Note.of("D1")), .01);
    assertEquals(38.18, tuning.pitch(Note.of("D#1")), .01);
    assertEquals(40.45, tuning.pitch(Note.of("E1")), .01);
    assertEquals(42.86, tuning.pitch(Note.of("F1")), .01);
    assertEquals(45.41, tuning.pitch(Note.of("F#1")), .01);
    assertEquals(48.11, tuning.pitch(Note.of("G1")), .01);
    assertEquals(50.97, tuning.pitch(Note.of("G#1")), .01);
    assertEquals(54.00, tuning.pitch(Note.of("A1")), .01);
    assertEquals(57.21, tuning.pitch(Note.of("A#1")), .01);
    assertEquals(60.61, tuning.pitch(Note.of("B1")), .01);
    assertEquals(64.22, tuning.pitch(Note.of("C2")), .01);
    assertEquals(68.04, tuning.pitch(Note.of("C#2")), .01);
    assertEquals(72.08, tuning.pitch(Note.of("D2")), .01);
    assertEquals(76.37, tuning.pitch(Note.of("D#2")), .01);
    assertEquals(80.91, tuning.pitch(Note.of("E2")), .01);
    assertEquals(85.72, tuning.pitch(Note.of("F2")), .01);
    assertEquals(90.82, tuning.pitch(Note.of("F#2")), .01);
    assertEquals(96.22, tuning.pitch(Note.of("G2")), .01);
    assertEquals(101.94, tuning.pitch(Note.of("G#2")), .01);
    assertEquals(108.00, tuning.pitch(Note.of("A2")), .01);
    assertEquals(114.42, tuning.pitch(Note.of("A#2")), .01);
    assertEquals(121.23, tuning.pitch(Note.of("B2")), .01);
    assertEquals(128.43, tuning.pitch(Note.of("C3")), .01);
    assertEquals(136.07, tuning.pitch(Note.of("C#3")), .01);
    assertEquals(144.16, tuning.pitch(Note.of("D3")), .01);
    assertEquals(152.74, tuning.pitch(Note.of("D#3")), .01);
    assertEquals(161.82, tuning.pitch(Note.of("E3")), .01);
    assertEquals(171.44, tuning.pitch(Note.of("F3")), .01);
    assertEquals(181.63, tuning.pitch(Note.of("F#3")), .01);
    assertEquals(192.43, tuning.pitch(Note.of("G3")), .01);
    assertEquals(203.88, tuning.pitch(Note.of("G#3")), .01);
    assertEquals(216.00, tuning.pitch(Note.of("A3")), .01);
    assertEquals(228.84, tuning.pitch(Note.of("A#3")), .01);
    assertEquals(242.45, tuning.pitch(Note.of("B3")), .01);
    assertEquals(256.87, tuning.pitch(Note.of("C4")), .01);
    assertEquals(272.14, tuning.pitch(Note.of("C#4")), .01);
    assertEquals(288.33, tuning.pitch(Note.of("D4")), .01);
    assertEquals(305.47, tuning.pitch(Note.of("D#4")), .01);
    assertEquals(323.63, tuning.pitch(Note.of("E4")), .01);
    assertEquals(342.88, tuning.pitch(Note.of("F4")), .01);
    assertEquals(363.27, tuning.pitch(Note.of("F#4")), .01);
    assertEquals(384.87, tuning.pitch(Note.of("G4")), .01);
    assertEquals(407.75, tuning.pitch(Note.of("G#4")), .01);
    assertEquals(432.00, tuning.pitch(Note.of("A4")), .01);
    assertEquals(457.69, tuning.pitch(Note.of("A#4")), .01);
    assertEquals(484.90, tuning.pitch(Note.of("B4")), .01);
    assertEquals(513.74, tuning.pitch(Note.of("C5")), .01);
    assertEquals(544.29, tuning.pitch(Note.of("C#5")), .01);
    assertEquals(576.65, tuning.pitch(Note.of("D5")), .01);
    assertEquals(610.94, tuning.pitch(Note.of("D#5")), .01);
    assertEquals(647.27, tuning.pitch(Note.of("E5")), .01);
    assertEquals(685.76, tuning.pitch(Note.of("F5")), .01);
    assertEquals(726.53, tuning.pitch(Note.of("F#5")), .01);
    assertEquals(769.74, tuning.pitch(Note.of("G5")), .01);
    assertEquals(815.51, tuning.pitch(Note.of("G#5")), .01);
    assertEquals(864.00, tuning.pitch(Note.of("A5")), .01);
    assertEquals(915.38, tuning.pitch(Note.of("A#5")), .01);
    assertEquals(969.81, tuning.pitch(Note.of("B5")), .01);
    assertEquals(1027.47, tuning.pitch(Note.of("C6")), .01);
    assertEquals(1088.57, tuning.pitch(Note.of("C#6")), .01);
    assertEquals(1153.30, tuning.pitch(Note.of("D6")), .01);
    assertEquals(1221.88, tuning.pitch(Note.of("D#6")), .01);
    assertEquals(1294.54, tuning.pitch(Note.of("E6")), .01);
    assertEquals(1371.51, tuning.pitch(Note.of("F6")), .01);
    assertEquals(1453.07, tuning.pitch(Note.of("F#6")), .01);
    assertEquals(1539.47, tuning.pitch(Note.of("G6")), .01);
    assertEquals(1631.01, tuning.pitch(Note.of("G#6")), .01);
    assertEquals(1728.00, tuning.pitch(Note.of("A6")), .01);
    assertEquals(1830.75, tuning.pitch(Note.of("A#6")), .01);
    assertEquals(1939.61, tuning.pitch(Note.of("B6")), .01);
    assertEquals(2054.95, tuning.pitch(Note.of("C7")), .01);
    assertEquals(2177.14, tuning.pitch(Note.of("C#7")), .01);
    assertEquals(2306.60, tuning.pitch(Note.of("D7")), .01);
    assertEquals(2443.76, tuning.pitch(Note.of("D#7")), .01);
    assertEquals(2589.07, tuning.pitch(Note.of("E7")), .01);
    assertEquals(2743.03, tuning.pitch(Note.of("F7")), .01);
    assertEquals(2906.14, tuning.pitch(Note.of("F#7")), .01);
    assertEquals(3078.95, tuning.pitch(Note.of("G7")), .01);
    assertEquals(3262.03, tuning.pitch(Note.of("G#7")), .01);
    assertEquals(3456.00, tuning.pitch(Note.of("A7")), .01);
    assertEquals(3661.50, tuning.pitch(Note.of("A#7")), .01);
    assertEquals(3879.23, tuning.pitch(Note.of("B7")), .01);
    assertEquals(4109.90, tuning.pitch(Note.of("C8")), .01);
    assertEquals(4354.29, tuning.pitch(Note.of("C#8")), .01);
    assertEquals(4613.21, tuning.pitch(Note.of("D8")), .01);
    assertEquals(4887.52, tuning.pitch(Note.of("D#8")), .01);
    assertEquals(5178.15, tuning.pitch(Note.of("E8")), .01);
    assertEquals(5486.06, tuning.pitch(Note.of("F8")), .01);
    assertEquals(5812.28, tuning.pitch(Note.of("F#8")), .01);
    assertEquals(6157.89, tuning.pitch(Note.of("G8")), .01);
    assertEquals(6524.06, tuning.pitch(Note.of("G#8")), .01);
    assertEquals(6912.00, tuning.pitch(Note.of("A8")), .01);
    assertEquals(7323.01, tuning.pitch(Note.of("A#8")), .01);
    assertEquals(7758.46, tuning.pitch(Note.of("B8")), .01);
  }

  @Test
  public void note() throws Exception {
    Tuning tuning = Tuning.atA4(432);

    assertTrue( Note.of("C0").sameAs(tuning.note(16.05)));
    assertTrue( Note.of("C#0").sameAs(tuning.note(17.00)));
    assertTrue( Note.of("D0").sameAs(tuning.note(18.02)));
    assertTrue( Note.of("D#0").sameAs(tuning.note(19.09)));
    assertTrue( Note.of("E0").sameAs(tuning.note(20.22)));
    assertTrue( Note.of("F0").sameAs(tuning.note(21.42)));
    assertTrue( Note.of("F#0").sameAs(tuning.note(22.70)));
    assertTrue( Note.of("G0").sameAs(tuning.note(24.05)));
    assertTrue( Note.of("G#0").sameAs(tuning.note(25.48)));
    assertTrue( Note.of("A0").sameAs(tuning.note(26.99)));
    assertTrue( Note.of("A#0").sameAs(tuning.note(28.60)));
    assertTrue( Note.of("B0").sameAs(tuning.note(30.30)));
    assertTrue( Note.of("C1").sameAs(tuning.note(32.10)));
    assertTrue( Note.of("C#1").sameAs(tuning.note(34.01)));
    assertTrue( Note.of("D1").sameAs(tuning.note(36.03)));
    assertTrue( Note.of("D#1").sameAs(tuning.note(38.17)));
    assertTrue( Note.of("E1").sameAs(tuning.note(40.44)));
    assertTrue( Note.of("F1").sameAs(tuning.note(42.85)));
    assertTrue( Note.of("F#1").sameAs(tuning.note(45.40)));
    assertTrue( Note.of("G1").sameAs(tuning.note(48.10)));
    assertTrue( Note.of("G#1").sameAs(tuning.note(50.96)));
    assertTrue( Note.of("A1").sameAs(tuning.note(53.99)));
    assertTrue( Note.of("A#1").sameAs(tuning.note(57.20)));
    assertTrue( Note.of("B1").sameAs(tuning.note(60.60)));
    assertTrue( Note.of("C2").sameAs(tuning.note(64.21)));
    assertTrue( Note.of("C#2").sameAs(tuning.note(68.03)));
    assertTrue( Note.of("D2").sameAs(tuning.note(72.07)));
    assertTrue( Note.of("D#2").sameAs(tuning.note(76.36)));
    assertTrue( Note.of("E2").sameAs(tuning.note(80.90)));
    assertTrue( Note.of("F2").sameAs(tuning.note(85.71)));
    assertTrue( Note.of("F#2").sameAs(tuning.note(90.81)));
    assertTrue( Note.of("G2").sameAs(tuning.note(96.21)));
    assertTrue( Note.of("G#2").sameAs(tuning.note(101.93)));
    assertTrue( Note.of("A2").sameAs(tuning.note(107.99)));
    assertTrue( Note.of("A#2").sameAs(tuning.note(114.41)));
    assertTrue( Note.of("B2").sameAs(tuning.note(121.22)));
    assertTrue( Note.of("C3").sameAs(tuning.note(128.42)));
    assertTrue( Note.of("C#3").sameAs(tuning.note(136.06)));
    assertTrue( Note.of("D3").sameAs(tuning.note(144.15)));
    assertTrue( Note.of("D#3").sameAs(tuning.note(152.73)));
    assertTrue( Note.of("E3").sameAs(tuning.note(161.81)));
    assertTrue( Note.of("F3").sameAs(tuning.note(171.43)));
    assertTrue( Note.of("F#3").sameAs(tuning.note(181.62)));
    assertTrue( Note.of("G3").sameAs(tuning.note(192.43)));
    assertTrue( Note.of("G#3").sameAs(tuning.note(203.87)));
    assertTrue( Note.of("A3").sameAs(tuning.note(215.99)));
    assertTrue( Note.of("A#3").sameAs(tuning.note(228.83)));
    assertTrue( Note.of("B3").sameAs(tuning.note(242.44)));
    assertTrue( Note.of("C4").sameAs(tuning.note(256.86)));
    assertTrue( Note.of("C#4").sameAs(tuning.note(272.13)));
    assertTrue( Note.of("D4").sameAs(tuning.note(288.32)));
    assertTrue( Note.of("D#4").sameAs(tuning.note(305.46)));
    assertTrue( Note.of("E4").sameAs(tuning.note(323.62)));
    assertTrue( Note.of("F4").sameAs(tuning.note(342.87)));
    assertTrue( Note.of("F#4").sameAs(tuning.note(363.26)));
    assertTrue( Note.of("G4").sameAs(tuning.note(384.86)));
    assertTrue( Note.of("G#4").sameAs(tuning.note(407.75)));
    assertTrue( Note.of("A4").sameAs(tuning.note(432.00)));
    assertTrue( Note.of("A#4").sameAs(tuning.note(457.69)));
    assertTrue( Note.of("B4").sameAs(tuning.note(484.91)));
    assertTrue( Note.of("C5").sameAs(tuning.note(513.74)));
    assertTrue( Note.of("C#5").sameAs(tuning.note(544.29)));
    assertTrue( Note.of("D5").sameAs(tuning.note(576.66)));
    assertTrue( Note.of("D#5").sameAs(tuning.note(610.95)));
    assertTrue( Note.of("E5").sameAs(tuning.note(647.28)));
    assertTrue( Note.of("F5").sameAs(tuning.note(685.77)));
    assertTrue( Note.of("F#5").sameAs(tuning.note(726.54)));
    assertTrue( Note.of("G5").sameAs(tuning.note(769.75)));
    assertTrue( Note.of("G#5").sameAs(tuning.note(815.52)));
    assertTrue( Note.of("A5").sameAs(tuning.note(864.01)));
    assertTrue( Note.of("A#5").sameAs(tuning.note(915.39)));
    assertTrue( Note.of("B5").sameAs(tuning.note(969.82)));
    assertTrue( Note.of("C6").sameAs(tuning.note(1027.48)));
    assertTrue( Note.of("C#6").sameAs(tuning.note(1088.58)));
    assertTrue( Note.of("D6").sameAs(tuning.note(1153.31)));
    assertTrue( Note.of("D#6").sameAs(tuning.note(1221.89)));
    assertTrue( Note.of("E6").sameAs(tuning.note(1294.55)));
    assertTrue( Note.of("F6").sameAs(tuning.note(1371.52)));
    assertTrue( Note.of("F#6").sameAs(tuning.note(1453.08)));
    assertTrue( Note.of("G6").sameAs(tuning.note(1539.48)));
    assertTrue( Note.of("G#6").sameAs(tuning.note(1631.02)));
    assertTrue( Note.of("A6").sameAs(tuning.note(1728.01)));
    assertTrue( Note.of("A#6").sameAs(tuning.note(1830.76)));
    assertTrue( Note.of("B6").sameAs(tuning.note(1939.62)));
    assertTrue( Note.of("C7").sameAs(tuning.note(2054.96)));
    assertTrue( Note.of("C#7").sameAs(tuning.note(2177.15)));
    assertTrue( Note.of("D7").sameAs(tuning.note(2306.61)));
    assertTrue( Note.of("D#7").sameAs(tuning.note(2443.77)));
    assertTrue( Note.of("E7").sameAs(tuning.note(2589.08)));
    assertTrue( Note.of("F7").sameAs(tuning.note(2743.04)));
    assertTrue( Note.of("F#7").sameAs(tuning.note(2906.15)));
    assertTrue( Note.of("G7").sameAs(tuning.note(3078.96)));
    assertTrue( Note.of("G#7").sameAs(tuning.note(3262.04)));
    assertTrue( Note.of("A7").sameAs(tuning.note(3456.01)));
    assertTrue( Note.of("A#7").sameAs(tuning.note(3661.51)));
    assertTrue( Note.of("B7").sameAs(tuning.note(3879.24)));
    assertTrue( Note.of("C8").sameAs(tuning.note(4109.91)));
    assertTrue( Note.of("C#8").sameAs(tuning.note(4354.30)));
    assertTrue( Note.of("D8").sameAs(tuning.note(4613.22)));
    assertTrue( Note.of("D#8").sameAs(tuning.note(4887.53)));
    assertTrue( Note.of("E8").sameAs(tuning.note(5178.16)));
    assertTrue( Note.of("F8").sameAs(tuning.note(5486.07)));
    assertTrue( Note.of("F#8").sameAs(tuning.note(5812.29)));
    assertTrue( Note.of("G8").sameAs(tuning.note(6157.90)));
    assertTrue( Note.of("G#8").sameAs(tuning.note(6524.07)));
    assertTrue( Note.of("A8").sameAs(tuning.note(6912.01)));
    assertTrue( Note.of("A#8").sameAs(tuning.note(7323.02)));
    assertTrue( Note.of("B8").sameAs(tuning.note(7758.47)));
  }

  @Test
  public void deltaFromRootPitch() throws Exception {
    Tuning tuning = Tuning.atA4(432);

    assertEquals( Integer.valueOf(-57), tuning.deltaFromRootPitch(16.05));
    assertEquals( Integer.valueOf(-56), tuning.deltaFromRootPitch(17.00));
    assertEquals( Integer.valueOf(-55), tuning.deltaFromRootPitch(18.02));
    assertEquals( Integer.valueOf(-54), tuning.deltaFromRootPitch(19.09));
    assertEquals( Integer.valueOf(-53), tuning.deltaFromRootPitch(20.22));
    assertEquals( Integer.valueOf(-52), tuning.deltaFromRootPitch(21.42));
    assertEquals( Integer.valueOf(-51), tuning.deltaFromRootPitch(22.70));
    assertEquals( Integer.valueOf(-50), tuning.deltaFromRootPitch(24.05));
    assertEquals( Integer.valueOf(-49), tuning.deltaFromRootPitch(25.48));
    assertEquals( Integer.valueOf(-48), tuning.deltaFromRootPitch(26.99));
    assertEquals( Integer.valueOf(-47), tuning.deltaFromRootPitch(28.60));
    assertEquals( Integer.valueOf(-46), tuning.deltaFromRootPitch(30.30));
    assertEquals( Integer.valueOf(-45), tuning.deltaFromRootPitch(32.10));
    assertEquals( Integer.valueOf(-44), tuning.deltaFromRootPitch(34.01));
    assertEquals( Integer.valueOf(-43), tuning.deltaFromRootPitch(36.03));
    assertEquals( Integer.valueOf(-42), tuning.deltaFromRootPitch(38.17));
    assertEquals( Integer.valueOf(-41), tuning.deltaFromRootPitch(40.44));
    assertEquals( Integer.valueOf(-40), tuning.deltaFromRootPitch(42.85));
    assertEquals( Integer.valueOf(-39), tuning.deltaFromRootPitch(45.40));
    assertEquals( Integer.valueOf(-38), tuning.deltaFromRootPitch(48.10));
    assertEquals( Integer.valueOf(-37), tuning.deltaFromRootPitch(50.96));
    assertEquals( Integer.valueOf(-36), tuning.deltaFromRootPitch(53.99));
    assertEquals( Integer.valueOf(-35), tuning.deltaFromRootPitch(57.20));
    assertEquals( Integer.valueOf(-34), tuning.deltaFromRootPitch(60.60));
    assertEquals( Integer.valueOf(-33), tuning.deltaFromRootPitch(64.21));
    assertEquals( Integer.valueOf(-32), tuning.deltaFromRootPitch(68.03));
    assertEquals( Integer.valueOf(-31), tuning.deltaFromRootPitch(72.07));
    assertEquals( Integer.valueOf(-30), tuning.deltaFromRootPitch(76.36));
    assertEquals( Integer.valueOf(-29), tuning.deltaFromRootPitch(80.90));
    assertEquals( Integer.valueOf(-28), tuning.deltaFromRootPitch(85.71));
    assertEquals( Integer.valueOf(-27), tuning.deltaFromRootPitch(90.81));
    assertEquals( Integer.valueOf(-26), tuning.deltaFromRootPitch(96.21));
    assertEquals( Integer.valueOf(-25), tuning.deltaFromRootPitch(101.93));
    assertEquals( Integer.valueOf(-24), tuning.deltaFromRootPitch(107.99));
    assertEquals( Integer.valueOf(-23), tuning.deltaFromRootPitch(114.41));
    assertEquals( Integer.valueOf(-22), tuning.deltaFromRootPitch(121.22));
    assertEquals( Integer.valueOf(-21), tuning.deltaFromRootPitch(128.42));
    assertEquals( Integer.valueOf(-20), tuning.deltaFromRootPitch(136.06));
    assertEquals( Integer.valueOf(-19), tuning.deltaFromRootPitch(144.15));
    assertEquals( Integer.valueOf(-18), tuning.deltaFromRootPitch(152.73));
    assertEquals( Integer.valueOf(-17), tuning.deltaFromRootPitch(161.81));
    assertEquals( Integer.valueOf(-16), tuning.deltaFromRootPitch(171.43));
    assertEquals( Integer.valueOf(-15), tuning.deltaFromRootPitch(181.62));
    assertEquals( Integer.valueOf(-14), tuning.deltaFromRootPitch(192.43));
    assertEquals( Integer.valueOf(-13), tuning.deltaFromRootPitch(203.87));
    assertEquals( Integer.valueOf(-12), tuning.deltaFromRootPitch(215.99));
    assertEquals( Integer.valueOf(-11), tuning.deltaFromRootPitch(228.83));
    assertEquals( Integer.valueOf(-10), tuning.deltaFromRootPitch(242.44));
    assertEquals( Integer.valueOf(-9), tuning.deltaFromRootPitch(256.86));
    assertEquals( Integer.valueOf(-8), tuning.deltaFromRootPitch(272.13));
    assertEquals( Integer.valueOf(-7), tuning.deltaFromRootPitch(288.32));
    assertEquals( Integer.valueOf(-6), tuning.deltaFromRootPitch(305.46));
    assertEquals( Integer.valueOf(-5), tuning.deltaFromRootPitch(323.62));
    assertEquals( Integer.valueOf(-4), tuning.deltaFromRootPitch(342.87));
    assertEquals( Integer.valueOf(-3), tuning.deltaFromRootPitch(363.26));
    assertEquals( Integer.valueOf(-2), tuning.deltaFromRootPitch(384.86));
    assertEquals( Integer.valueOf(-1), tuning.deltaFromRootPitch(407.75));
    assertEquals( Integer.valueOf(0), tuning.deltaFromRootPitch(432.00));
    assertEquals( Integer.valueOf(1), tuning.deltaFromRootPitch(457.69));
    assertEquals( Integer.valueOf(2), tuning.deltaFromRootPitch(484.91));
    assertEquals( Integer.valueOf(3), tuning.deltaFromRootPitch(513.74));
    assertEquals( Integer.valueOf(4), tuning.deltaFromRootPitch(544.29));
    assertEquals( Integer.valueOf(5), tuning.deltaFromRootPitch(576.66));
    assertEquals( Integer.valueOf(6), tuning.deltaFromRootPitch(610.95));
    assertEquals( Integer.valueOf(7), tuning.deltaFromRootPitch(647.28));
    assertEquals( Integer.valueOf(8), tuning.deltaFromRootPitch(685.77));
    assertEquals( Integer.valueOf(9), tuning.deltaFromRootPitch(726.54));
    assertEquals( Integer.valueOf(10), tuning.deltaFromRootPitch(769.75));
    assertEquals( Integer.valueOf(11), tuning.deltaFromRootPitch(815.52));
    assertEquals( Integer.valueOf(12), tuning.deltaFromRootPitch(864.01));
    assertEquals( Integer.valueOf(13), tuning.deltaFromRootPitch(915.39));
    assertEquals( Integer.valueOf(14), tuning.deltaFromRootPitch(969.82));
    assertEquals( Integer.valueOf(15), tuning.deltaFromRootPitch(1027.48));
    assertEquals( Integer.valueOf(16), tuning.deltaFromRootPitch(1088.58));
    assertEquals( Integer.valueOf(17), tuning.deltaFromRootPitch(1153.31));
    assertEquals( Integer.valueOf(18), tuning.deltaFromRootPitch(1221.89));
    assertEquals( Integer.valueOf(19), tuning.deltaFromRootPitch(1294.55));
    assertEquals( Integer.valueOf(20), tuning.deltaFromRootPitch(1371.52));
    assertEquals( Integer.valueOf(21), tuning.deltaFromRootPitch(1453.08));
    assertEquals( Integer.valueOf(22), tuning.deltaFromRootPitch(1539.48));
    assertEquals( Integer.valueOf(23), tuning.deltaFromRootPitch(1631.02));
    assertEquals( Integer.valueOf(24), tuning.deltaFromRootPitch(1728.01));
    assertEquals( Integer.valueOf(25), tuning.deltaFromRootPitch(1830.76));
    assertEquals( Integer.valueOf(26), tuning.deltaFromRootPitch(1939.62));
    assertEquals( Integer.valueOf(27), tuning.deltaFromRootPitch(2054.96));
    assertEquals( Integer.valueOf(28), tuning.deltaFromRootPitch(2177.15));
    assertEquals( Integer.valueOf(29), tuning.deltaFromRootPitch(2306.61));
    assertEquals( Integer.valueOf(30), tuning.deltaFromRootPitch(2443.77));
    assertEquals( Integer.valueOf(31), tuning.deltaFromRootPitch(2589.08));
    assertEquals( Integer.valueOf(32), tuning.deltaFromRootPitch(2743.04));
    assertEquals( Integer.valueOf(33), tuning.deltaFromRootPitch(2906.15));
    assertEquals( Integer.valueOf(34), tuning.deltaFromRootPitch(3078.96));
    assertEquals( Integer.valueOf(35), tuning.deltaFromRootPitch(3262.04));
    assertEquals( Integer.valueOf(36), tuning.deltaFromRootPitch(3456.01));
    assertEquals( Integer.valueOf(37), tuning.deltaFromRootPitch(3661.51));
    assertEquals( Integer.valueOf(38), tuning.deltaFromRootPitch(3879.24));
    assertEquals( Integer.valueOf(39), tuning.deltaFromRootPitch(4109.91));
    assertEquals( Integer.valueOf(40), tuning.deltaFromRootPitch(4354.30));
    assertEquals( Integer.valueOf(41), tuning.deltaFromRootPitch(4613.22));
    assertEquals( Integer.valueOf(42), tuning.deltaFromRootPitch(4887.53));
    assertEquals( Integer.valueOf(43), tuning.deltaFromRootPitch(5178.16));
    assertEquals( Integer.valueOf(44), tuning.deltaFromRootPitch(5486.07));
    assertEquals( Integer.valueOf(45), tuning.deltaFromRootPitch(5812.29));
    assertEquals( Integer.valueOf(46), tuning.deltaFromRootPitch(6157.90));
    assertEquals( Integer.valueOf(47), tuning.deltaFromRootPitch(6524.07));
    assertEquals( Integer.valueOf(48), tuning.deltaFromRootPitch(6912.01));
    assertEquals( Integer.valueOf(49), tuning.deltaFromRootPitch(7323.02));
    assertEquals( Integer.valueOf(50), tuning.deltaFromRootPitch(7758.47));
  }

  @Test
  public void tuning_MusicalException_noRootNote() throws Exception {
    expectMusicalFailure("Must specify a root note for tuning");
    Tuning.at(null, 23.8);
  }

  @Test
  public void tuning_MusicalException_noRootPitch() throws Exception {
    expectMusicalFailure("Must specify a root pitch (in Hz) for tuning");
    Tuning.at(Note.of("G5"), null);
  }

  @Test
  public void tuning_MusicalException_rootPitchTooHigh() throws Exception {
    expectMusicalFailure("Root pitch must be between 1.000000 and 100000.000000 (Hz)");
    Tuning.at(Note.of("G12"), 240000.0);
  }

  @Test
  public void tuning_MusicalException_rootPitchTooLow() throws Exception {
    expectMusicalFailure("Root pitch must be between 1.000000 and 100000.000000 (Hz)");
    Tuning.at(Note.of("G12"), 0.02);
  }

  @Test
  public void tuning_MusicalException_rootNoteInvalidPitchClass() throws Exception {
    expectMusicalFailure("Root note must have a pitch class (e.g. 'C')");
    Tuning.at(Note.of("moo"), 212.48);
  }

  @Test
  public void tuning_MusicalException_rootOctaveTooLow() throws Exception {
    expectMusicalFailure("Root note octave must be between 0 and 15");
    Tuning.at(Note.of("G-2"), 1.48);
  }

  @Test
  public void tuning_MusicalException_rootOctaveTooHigh() throws Exception {
    expectMusicalFailure("Root note octave must be between 0 and 15");
    Tuning.at(Note.of("G73"), 97492.06);
  }

  private void expectMusicalFailure(String msg) {
    failure.expect(MusicalException.class);
    failure.expectMessage(msg);
  }

}
