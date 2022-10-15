// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.music;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;
import java.util.Objects;

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
  private final Logger LOG = LoggerFactory.getLogger(ChordTest.class);

  @Test
  public void TestChordExpectations() {
    Yaml yaml = new Yaml();

    Map<String, String> chords = yaml.load(getClass().getResourceAsStream(EXPECTED_CHORDS_YAML));
    assertNotNull(chords);

    int fails = 0;
    String actual;
    for (Map.Entry<?, ?> entry : chords.entrySet()) {
      String input = entry.getKey().toString();
      String expect = entry.getValue().toString();
      actual = Chord.of(input).toString();
      if (!Objects.equals(actual, expect)) {
        LOG.warn("Expected \"{}\" to yield \"{}\" but actually was \"{}\"", input, expect, actual);
        fails++;
      }
    }
    assertEquals("no failures", 0, fails);
  }

  @Test
  public void TestOf_Invalid() {
    Chord chord = Chord.of("P-funk");
    assertEquals(None, chord.getRoot());
  }

  @Test
  public void TestTranspose_DescriptionChange() {
    Chord chord = Chord.of("Cm nondominant -5 +6 +7 +9");

    assertEquals("C m nondominant -5 +6 +7 +9", chord.getName());
    assertEquals("Eb m nondominant -5 +6 +7 +9", chord.transpose(3).getName());
  }

  @Test
  public void isNull() {
    assertFalse(Chord.of("C#m7").isNoChord());
    assertTrue(Chord.of("NC").isNoChord());
    assertEquals("NC", Chord.NO_CHORD_NAME);
  }

  /**
   https://www.pivotaltracker.com/story/show/176728338 XJ understands the root of a slash chord
   */
  @Test
  public void getSlashRoot() {
    assertEquals(PitchClass.C, Chord.of("Cm7").getSlashRoot());
    assertEquals(PitchClass.Cs, Chord.of("C#m7").getSlashRoot());
    assertEquals(PitchClass.G, Chord.of("Cm7/G").getSlashRoot());
    assertEquals(PitchClass.Gs, Chord.of("C#m7/G#").getSlashRoot());
    assertEquals(PitchClass.A, Chord.of("Gsus4/A").getSlashRoot());
  }

  @Test
  public void stripExtraSpacesFromName() {
    assertEquals("G", Chord.of("  G      ").getName());
  }

  @Test
  public void isSame() {
    assertTrue(Chord.of("  G major     ").isSame(Chord.of(" G     major ")));
    assertTrue(Chord.of("Gm").isSame(Chord.of("Gm")));
    assertFalse(Chord.of("Gm").isSame(Chord.of("Cm")));
  }

  @Test
  public void isAcceptable() {
    assertTrue(Chord.of("  G major     ").isAcceptable(Chord.of(" G     major ")));
    assertTrue(Chord.of("Gm").isAcceptable(Chord.of("Gm")));
    assertFalse(Chord.of("Gm").isAcceptable(Chord.of("Cm")));
    assertTrue(Chord.of("Gm").isAcceptable(Chord.of("Gm/Bb")));
    assertTrue(Chord.of("Gm/Bb").isAcceptable(Chord.of("Gm")));
    assertFalse(Chord.of("Gm/Bb").isAcceptable(Chord.of("Cm")));
  }
}
