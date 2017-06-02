// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.music;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

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
