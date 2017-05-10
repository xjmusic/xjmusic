// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.music;

import io.outright.xj.music.schema.KeyMode;

import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

import static io.outright.xj.music.schema.KeyMode.Major;
import static io.outright.xj.music.schema.KeyMode.Minor;
import static io.outright.xj.music.PitchClass.A;
import static io.outright.xj.music.PitchClass.C;
import static io.outright.xj.music.PitchClass.None;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class KeyTest {

  private static final String EXPECTED_KEYS_YAML = "/expect_key.yaml";
  private static final String KEY_KEYS = "keys";
  private static final String KEY_ROOT_PITCH_CLASS = "root";
  private static final Object KEY_MODE = "mode";

  @Test
  public void KeyExpectationsTest() throws Exception {
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
    assertEquals(expectRootPitchClass, key.getRootPitchClass());
    assertEquals(expectMode, key.getMode());
  }


  @Test
  public void OfInvalidTest() {
    Key key = Key.of("P-funk");
    assertEquals(None, key.getRootPitchClass());
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
    assertEquals(C, key.getRootPitchClass());
    assertEquals(Major, key.getMode());
  }

  @Test
  public void RelativeMinorTest() {
    Key key = Key.of("C major").relativeMinor();
    assertEquals(A, key.getRootPitchClass());
    assertEquals(Minor, key.getMode());
  }

  @Test
  public void ModeStringTest() {
    assertEquals("Major", Major.toString());
    assertEquals("Minor", Minor.toString());
    assertEquals("None", KeyMode.None.toString());
  }

  @Test
  public void ModeOfTest() {
    assertEquals(Major, KeyMode.of("Major"));
    assertEquals(Major, KeyMode.of("M"));
    assertEquals(Major, KeyMode.of("major"));

    assertEquals(Minor, KeyMode.of("minor"));
    assertEquals(Minor, KeyMode.of("min"));
    assertEquals(Minor, KeyMode.of("m"));

    assertEquals(Major, KeyMode.of("joe"));
  }
}
