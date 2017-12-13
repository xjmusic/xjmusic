// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.music;

import io.xj.music.schema.KeyMode;

import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
    Assert.assertEquals(expectRootPitchClass, key.getRootPitchClass());
    Assert.assertEquals(expectMode, key.getMode());
  }

  @Test
  public void isMatchingMode() throws Exception {
    assertTrue(Key.isSameMode("C minor", "G minor"));
  }

  @Test
  public void delta() throws Exception {
    assertEquals(Integer.valueOf(2), Key.delta("C", "D", 0));
    assertEquals(Integer.valueOf(-5), Key.delta("C", "G", 0));
    assertEquals(Integer.valueOf(-3), Key.delta("C", "G", 2));
  }

  @Test
  public void OfInvalidTest() {
    Key key = Key.of("P-funk");
    Assert.assertEquals(PitchClass.None, key.getRootPitchClass());
  }


  @Test
  public void PitchClassDiffTest() {
    Assert.assertEquals(5, Key.of("C#").delta(Key.of("F#")));
    Assert.assertEquals(-5, Key.of("F#").delta(Key.of("C#")));
    Assert.assertEquals(2, Key.of("Gb").delta(Key.of("Ab")));
    Assert.assertEquals(-3, Key.of("C").delta(Key.of("A")));
    Assert.assertEquals(4, Key.of("D").delta(Key.of("F#")));
    Assert.assertEquals(-6, Key.of("F").delta(Key.of("B")));
  }

  @Test
  public void RelativeMajorTest() {
    Key key = Key.of("A minor").relativeMajor();
    Assert.assertEquals(PitchClass.C, key.getRootPitchClass());
    Assert.assertEquals(KeyMode.Major, key.getMode());
  }

  @Test
  public void RelativeMinorTest() {
    Key key = Key.of("C major").relativeMinor();
    Assert.assertEquals(PitchClass.A, key.getRootPitchClass());
    Assert.assertEquals(KeyMode.Minor, key.getMode());
  }

  @Test
  public void ModeStringTest() {
    Assert.assertEquals("Major", KeyMode.Major.toString());
    Assert.assertEquals("Minor", KeyMode.Minor.toString());
    assertEquals("None", KeyMode.None.toString());
  }

  @Test
  public void ModeOfTest() {
    Assert.assertEquals(KeyMode.Major, KeyMode.of("Major"));
    Assert.assertEquals(KeyMode.Major, KeyMode.of("M"));
    Assert.assertEquals(KeyMode.Major, KeyMode.of("major"));

    Assert.assertEquals(KeyMode.Minor, KeyMode.of("minor"));
    Assert.assertEquals(KeyMode.Minor, KeyMode.of("min"));
    Assert.assertEquals(KeyMode.Minor, KeyMode.of("m"));

    Assert.assertEquals(KeyMode.Major, KeyMode.of("joe"));
  }
}
