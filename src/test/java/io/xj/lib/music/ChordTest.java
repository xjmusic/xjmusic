// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.music;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

import static io.xj.lib.music.Interval.I3;
import static io.xj.lib.music.Interval.I6;
import static io.xj.lib.music.Interval.I7;
import static io.xj.lib.music.Interval.I9;
import static io.xj.lib.music.PitchClass.A;
import static io.xj.lib.music.PitchClass.As;
import static io.xj.lib.music.PitchClass.C;
import static io.xj.lib.music.PitchClass.Cs;
import static io.xj.lib.music.PitchClass.D;
import static io.xj.lib.music.PitchClass.Ds;
import static io.xj.lib.music.PitchClass.F;
import static io.xj.lib.music.PitchClass.Fs;
import static io.xj.lib.music.PitchClass.None;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ChordTest {
  private static final String EXPECTED_CHORDS_YAML = "/music/expect_chord.yaml";
  private static final String KEY_CHORDS = "chords";
  private static final String KEY_ROOT_PITCH_CLASS = "root";
  private static final Object KEY_PITCHES = "pitches";

  @Test
  public void TestChordExpectations() {
    Yaml yaml = new Yaml();

    Map<?, ?> wrapper = (Map<?, ?>) yaml.load(getClass().getResourceAsStream(EXPECTED_CHORDS_YAML));
    assertNotNull(wrapper);

    Map<?, ?> chords = (Map<?, ?>) wrapper.get(KEY_CHORDS);
    assertNotNull(chords);

    chords.keySet().forEach((chordName) -> {
      Map<?, ?> chord = (Map<?, ?>) chords.get(chordName);
      PitchClass expectRootPitchClass = PitchClass.of(String.valueOf(chord.get(KEY_ROOT_PITCH_CLASS)));
      assertNotNull(expectRootPitchClass);

      Map<?, ?> rawPitches = (Map<?, ?>) chord.get(KEY_PITCHES);
      assertNotNull(rawPitches);

      Map<Interval, PitchClass> expectPitches = Maps.newHashMap();
      rawPitches.forEach((rawInterval, rawPitchClass) ->
        expectPitches.put(
          Interval.valueOf(Integer.parseInt(String.valueOf(rawInterval))),
          PitchClass.of(String.valueOf(rawPitchClass))));

      assertChordExpectations(expectRootPitchClass, expectPitches, Chord.of(String.valueOf(chordName)));
    });
  }

  private void assertChordExpectations(PitchClass expectRootPitchClass, Map<Interval, PitchClass> expectPitchClasses, Chord chord) {
    System.out.println(
      "Expect pitch classes " + IntervalPitchGroup.detailsOf(expectPitchClasses, chord.getAdjSymbol()) + " for " +
        "Chord " + chord.details());
    Map<Interval, PitchClass> pitchClasses = chord.getPitchClasses();
    assertEquals(expectRootPitchClass, chord.getRoot());
    assertEquals("same number of pitch classes", expectPitchClasses.size(), pitchClasses.size());
    expectPitchClasses.forEach((expectInterval, expectPitchClass) ->
      assertEquals(String.format("same pitch class at interval %s", expectInterval.getValue()), expectPitchClass, pitchClasses.get(expectInterval)));
  }

  @Test
  public void TestOf_Invalid() {
    Chord chord = Chord.of("P-funk");
    assertEquals(None, chord.getRoot());
  }

  @Test
  public void TestChordOf() {
    Chord chord = Chord.of("Cm nondominant -5 +6 +7 +9");
    Map<Interval, PitchClass> pitchClasses = chord.getPitchClasses();
    assertEquals(4, pitchClasses.size());
    assertEquals(Ds, pitchClasses.get(I3));
    assertEquals(A, pitchClasses.get(I6));
    assertEquals(As, pitchClasses.get(I7));
    assertEquals(D, pitchClasses.get(I9));
  }

  @Test
  public void TestTranspose() {
    Chord chord = Chord.of("Cm nondominant -5 +6 +7 +9").transpose(3);
    Map<Interval, PitchClass> pitchClasses = chord.getPitchClasses();
    assertEquals(4, pitchClasses.size());
    assertEquals(Fs, pitchClasses.get(I3));
    assertEquals(C, pitchClasses.get(I6));
    assertEquals(Cs, pitchClasses.get(I7));
    assertEquals(F, pitchClasses.get(I9));
  }

  @Test
  public void TestTranspose_DescriptionChange() {
    Chord chord = Chord.of("Cm nondominant -5 +6 +7 +9");

    assertEquals("C m nondominant -5 +6 +7 +9", chord.getFullDescription());
    assertEquals("Eb m nondominant -5 +6 +7 +9", chord.transpose(3).getFullDescription());
  }

  @Test
  public void TestTranspose_officialDescription() {
    Chord chord = Chord.of("Cm nondominant -5 +6 +7 +9");

    assertEquals("C Basic NonDominant Minor Triad Omit Fifth Add Sixth Add Seventh Add Ninth", chord.officialDescription());
  }

  @Test
  public void getForms_setForms() {
    Chord source = Chord.of("Cm nondominant -5 +6 +7 +9");
    Chord result = new Chord();
    result.setForms(source.getForms());

    assertEquals(7, result.getForms().size());
  }

  @Test
  public void formString() {
    Chord chord = Chord.of("Cm nondominant -5 +6 +7 +9");

    assertEquals("Basic NonDominant Minor Triad Omit Fifth Add Sixth Add Seventh Add Ninth", chord.formString());
  }

  /**
   [#154985948] Architect wants to determine tonal similarity (% of shared pitch classes) between two Chords, in order to perform fuzzy matching operations.
   */
  @Test
  public void similarity() {
    assertEquals(1.0000,
      Chord.of("Cm nondominant -5 +6 +7 +9").similarity(
        Chord.of("Cm nondominant -5 +6 +7 +9")), 0.001);

    assertEquals(0.2499,
      Chord.of("Cm nondominant -5 +6 +7 +9").similarity(
        Chord.of("Gm nondominant -5 +6 +7 +9")), 0.001);

    assertEquals(0.3125,
      Chord.of("Cm nondominant -5 +6 +7 +9").similarity(
        Chord.of("D7 minor")), 0.001);

    assertEquals(0.1042,
      Chord.of("Cm nondominant -5 +6 +7 +9").similarity(
        Chord.of("C major")), 0.001);

    assertEquals(0.6250,
      Chord.of("C minor 7").similarity(
        Chord.of("C major 7")), 0.001);

    assertEquals(0.7792,
      Chord.of("C minor 7").similarity(
        Chord.of("C major m7")), 0.001);

  }

  @Test
  public void isNull() {
    assertFalse(Chord.of("C#m7").isNoChord());
    assertTrue(Chord.of("NC").isNoChord());
    assertEquals("NC", Chord.NO_CHORD_NAME);
  }

  /**
   [#176728338] XJ understands the root of a slash chord
   */
  @Test
  public void getSlashRoot() {
    assertEquals(PitchClass.C, Chord.of("Cm7").getSlashRoot());
    assertEquals(PitchClass.Cs, Chord.of("C#m7").getSlashRoot());
    assertEquals(PitchClass.G, Chord.of("Cm7/G").getSlashRoot());
    assertEquals(PitchClass.Gs, Chord.of("C#m7/G#").getSlashRoot());
  }
}
