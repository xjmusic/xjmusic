// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.model.music;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class StepTest {
  @Test
  public void to() throws Exception {
    Step step = Step.to(PitchClass.B, -1);

    assertNotNull(step);
  }

  @Test
  public void getPitchClass() throws Exception {
    Step step = Step.to(PitchClass.B, -1);

    assertEquals(PitchClass.B, step.getPitchClass());
  }

  @Test
  public void getDeltaOctave() throws Exception {
    Step step = Step.to(PitchClass.B, -1);

    assertEquals(Integer.valueOf(-1), step.getDeltaOctave());
  }

}
