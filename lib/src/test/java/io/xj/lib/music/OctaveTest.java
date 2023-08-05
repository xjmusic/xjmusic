// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.music;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class OctaveTest {

  @Test
  public void of() throws Exception {
    assertEquals(Integer.valueOf(0), Octave.of("F"));
    assertEquals(Integer.valueOf(2), Octave.of("Bb2"));
    assertEquals(Integer.valueOf(3), Octave.of("D#3"));
    assertEquals(Integer.valueOf(5), Octave.of("D5"));
    assertEquals(Integer.valueOf(-2), Octave.of("D-2"));
    assertEquals(Integer.valueOf(-2), Octave.of("D--2"));
  }

}
