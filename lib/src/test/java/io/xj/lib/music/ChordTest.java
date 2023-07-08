// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.music;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.xj.lib.music.PitchClass.None;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ChordTest {
  static final String EXPECTED_CHORDS_YAML = "/music/expect_chord.yaml";
  final Logger LOG = LoggerFactory.getLogger(ChordTest.class);

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
  public void of() {
    assertEquals("C 6/9", Chord.of("CM6add9").getName());
    assertEquals("C 7b9/13", Chord.of("C dom7b9/13").getName());
    assertEquals("C aug maj7", Chord.of("C+∆").getName());
  }

  @Test
  public void TestOf_Invalid() {
    Chord chord = Chord.of("P-funk");
    assertEquals(None, chord.getRoot());
  }

  @Test
  public void isNull() {
    assertFalse(Chord.of("C#m7").isNoChord());
    assertTrue(Chord.of("NC").isNoChord());
    assertEquals("NC", Chord.NO_CHORD_NAME);
  }

  /**
   XJ understands the root of a slash chord https://www.pivotaltracker.com/story/show/176728338
   Slash Chord Fluency https://www.pivotaltracker.com/story/show/182885209
   */
  @Test
  public void getSlashRoot() {
    assertEquals(PitchClass.C, Chord.of("Cm7").getSlashRoot());
    assertEquals(PitchClass.Cs, Chord.of("C#m7").getSlashRoot());
    assertEquals(PitchClass.G, Chord.of("Cm7/G").getSlashRoot());
    assertEquals(PitchClass.Gs, Chord.of("C#m7/G#").getSlashRoot());
    assertEquals(PitchClass.A, Chord.of("Gsus4/A").getSlashRoot());
    assertEquals(PitchClass.A, Chord.of("G/A").getSlashRoot());
  }

  /**
   XJ should correctly choose chords with tensions notated via slash and not confuse them with slash chords
   https://www.pivotaltracker.com/story/show/183738228
   */
  @Test
  public void getDescription_dontConfuseTensionWithSlash() {
    assertEquals("-", Chord.of("G#m/B").getDescription());
    assertEquals("maj7/9", Chord.of("Gmaj7/9").getDescription());
    assertEquals("maj7", Chord.of("Gmaj7/E").getDescription());
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


  /**
   Chord mode Instruments should recognize enharmonic equivalents https://www.pivotaltracker.com/story/show/183558424
   */
  @Test
  public void isSame_eharmonicEquivalent() {
    assertTrue(Chord.of("  G# major     ").isSame(Chord.of(" Ab     major ")));
    assertTrue(Chord.of("G#").isSame(Chord.of("Ab")));
    assertFalse(Chord.of("G#").isSame(Chord.of("Bb")));
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

  /**
   XJ should correctly choose chords with tensions notated via slash and not confuse them with slash chords
   https://www.pivotaltracker.com/story/show/183738228
   */
  @Test
  public void isAcceptable_dontConfuseTensionWithSlash() {
    assertFalse(Chord.of("Fmaj7/9").isAcceptable(Chord.of("Fmaj7/G")));
  }

  /**
   Synonym of base chord should be accepted for slash chord https://www.pivotaltracker.com/story/show/183553280
   */
  @Test
  public void isAcceptable_sameBaseDifferentSlash() {
    var c1 = Chord.of("C/G");
    var c2 = Chord.of("C/E");
    assertEquals(PitchClass.G, c1.getSlashRoot());
    assertEquals(PitchClass.E, c2.getSlashRoot());
    assertEquals(PitchClass.C, c1.getRoot());
    assertEquals(PitchClass.C, c2.getRoot());
    assertEquals("", c1.getDescription());
    assertEquals("", c2.getDescription());
    assertTrue(Chord.of("C/G").isAcceptable(Chord.of("C/E")));
  }

  /**
   Chord mode Instruments should recognize enharmonic equivalents https://www.pivotaltracker.com/story/show/183558424
   */
  @Test
  public void isAcceptable_eharmonicEquivalent() {
    assertTrue(Chord.of("  G# major     ").isAcceptable(Chord.of(" Ab     major ")));
    assertTrue(Chord.of("G#m").isAcceptable(Chord.of("Abm")));
    assertFalse(Chord.of("G#m").isAcceptable(Chord.of("Bbm")));
    assertTrue(Chord.of("G#m").isAcceptable(Chord.of("Abm/Bb")));
    assertTrue(Chord.of("G#m/C").isAcceptable(Chord.of("Abm")));
    assertFalse(Chord.of("G#m/C").isAcceptable(Chord.of("Bbm")));
  }

  @Test
  public void compareTo() {
    var source = List.of(
      Chord.of("Db minor"),
      Chord.of("C major"),
      Chord.of("C"),
      Chord.of("C minor"),
      Chord.of("Cs major")
    );

    var sorted = source.stream().sorted().toList();

    assertEquals("C, C, C -, C s major, Db -", sorted.stream().map(Chord::getName).collect(Collectors.joining(", ")));
  }
}
