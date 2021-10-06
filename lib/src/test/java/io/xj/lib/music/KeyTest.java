// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.music;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

import static org.junit.Assert.*;

public class KeyTest {

  private static final String EXPECTED_KEYS_YAML = "/music/expect_key.yaml";
  private static final String KEY_KEYS = "keys";
  private static final String KEY_ROOT_PITCH_CLASS = "root";
  private static final Object KEY_MODE = "mode";

  @Test
  public void KeyExpectationsTest() {
    Yaml yaml = new Yaml();

    Map<?, ?> wrapper = (Map<?, ?>) yaml.load(getClass().getResourceAsStream(EXPECTED_KEYS_YAML));
    assertNotNull(wrapper);

    Map<?, ?> keys = (Map<?, ?>) wrapper.get(KEY_KEYS);
    assertNotNull(keys);

    keys.keySet().forEach((keyName) -> {
      Map<?, ?> key = (Map<?, ?>) keys.get(keyName);
      PitchClass expectRootPitchClass = PitchClass.of(String.valueOf(key.get(KEY_ROOT_PITCH_CLASS)));
      assertNotNull(expectRootPitchClass);

      KeyMode expectMode = KeyMode.of(String.valueOf(key.get(KEY_MODE)));
      assertNotNull(expectMode);

      assertKeyExpectations(expectRootPitchClass, expectMode, Key.of(String.valueOf(keyName)));
    });
  }

  private void assertKeyExpectations(PitchClass expectRootPitchClass, KeyMode expectMode, Key key) {
    System.out.println(
      "Expect pitch classes " + expectMode + " for " +
        "Key '" + key.getName() + "'");
    assertEquals(expectRootPitchClass, key.getRoot());
    assertEquals(expectMode, key.getMode());
  }

  @Test
  public void isMatchingMode() {
    assertTrue(Key.isSameMode("C minor", "G minor"));
  }

  @Test
  public void delta() {
    assertEquals(Integer.valueOf(2), Key.delta("C", "D", 0));
    assertEquals(Integer.valueOf(-5), Key.delta("C", "G", 0));
    assertEquals(Integer.valueOf(-3), Key.delta("C", "G", 2));
  }

  @Test
  public void OfInvalidTest() {
    Key key = Key.of("P-funk");
    assertEquals(PitchClass.None, key.getRoot());
  }


  @Test
  public void PitchClassDiffTest() {
    assertEquals(5, Key.of("C#").delta(Key.of("F#")));
    assertEquals(-5, Key.of("F#").delta(Key.of("C#")));
    assertEquals(2, Key.of("Gb").delta(Key.of("Ab")));
    assertEquals(-3, Key.of("C").delta(Key.of("A")));
    assertEquals(4, Key.of("D").delta(Key.of("F#")));
    assertEquals(-6, Key.of("F").delta(Key.of("B")));
  }

  @Test
  public void RelativeMajorTest() {
    Key key = Key.of("A minor").relativeMajor();
    assertEquals(PitchClass.C, key.getRoot());
    assertEquals(KeyMode.Major, key.getMode());
  }

  @Test
  public void RelativeMinorTest() {
    Key key = Key.of("C major").relativeMinor();
    assertEquals(PitchClass.A, key.getRoot());
    assertEquals(KeyMode.Minor, key.getMode());
  }

  @Test
  public void ModeStringTest() {
    assertEquals("Major", KeyMode.Major.toString());
    assertEquals("Minor", KeyMode.Minor.toString());
    assertEquals("None", KeyMode.None.toString());
  }

  @Test
  public void ModeOfTest() {
    assertEquals(KeyMode.Major, KeyMode.of("Major"));
    assertEquals(KeyMode.Major, KeyMode.of("M"));
    assertEquals(KeyMode.Major, KeyMode.of("major"));

    assertEquals(KeyMode.Minor, KeyMode.of("minor"));
    assertEquals(KeyMode.Minor, KeyMode.of("min"));
    assertEquals(KeyMode.Minor, KeyMode.of("m"));

    assertEquals(KeyMode.Major, KeyMode.of("joe"));
  }
}
