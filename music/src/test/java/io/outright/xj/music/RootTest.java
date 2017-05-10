// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.music;

import org.junit.Test;

import static io.outright.xj.music.PitchClass.As;
import static io.outright.xj.music.PitchClass.C;
import static io.outright.xj.music.PitchClass.Cs;
import static io.outright.xj.music.PitchClass.None;
import static org.junit.Assert.assertEquals;

public class RootTest {

  @Test
  public void RootOfTest() throws Exception {
    assertRoot("C", C, "");
    assertRoot("Cmaj", C, "maj");
    assertRoot("B♭min", As, "min");
    assertRoot("C#dim", Cs, "dim");
    assertRoot("JAMS", None, "JAMS");
  }

  /**
   Assert the pitch class and remaining text of a Root object

   @param text                to get root from
   @param expectPitchClass    expected
   @param expectRemainingText expected
   @throws Exception on failure
   */
  private void assertRoot(String text, PitchClass expectPitchClass, String expectRemainingText) throws Exception {
    Root root = Root.of(text);
    assertEquals(expectPitchClass, root.getPitchClass());
    assertEquals(expectRemainingText, root.getRemainingText());
  }

}

