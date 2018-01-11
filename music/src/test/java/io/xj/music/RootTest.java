// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.music;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RootTest {

  @Test
  public void RootOfTest() throws Exception {
    assertRoot("C", PitchClass.C, "");
    assertRoot("Cmaj", PitchClass.C, "maj");
    assertRoot("B♭min", PitchClass.As, "min");
    assertRoot("C#dim", PitchClass.Cs, "dim");
    assertRoot("JAMS", PitchClass.None, "JAMS");
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

