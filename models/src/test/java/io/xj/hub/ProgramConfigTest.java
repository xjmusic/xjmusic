// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import io.xj.hub.pojos.Program;
import io.xj.hub.util.ValueException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 Artist saves Program config, validate & combine with defaults. https://github.com/xjmusic/workstation/issues/206
 */
public class ProgramConfigTest {

  @Test
  public void setFromProgram() throws ValueException {
    var program = new Program();
    program.setConfig("doPatternRestartOnChord = true");

    var subject = new ProgramConfig(program);

    assertTrue(subject.doPatternRestartOnChord());
  }

  @Test
  public void setFromDefaults() throws ValueException {
    var subject = new ProgramConfig(ProgramConfig.DEFAULT);

    assertFalse(subject.doPatternRestartOnChord());
  }

  @Test
  public void getCutoffMinimumBars() throws ValueException {
    var subject = new ProgramConfig(ProgramConfig.DEFAULT);

    assertEquals(2, subject.getCutoffMinimumBars());
  }

  @Test
  public void getBarBeats() throws ValueException {
    var subject = new ProgramConfig(ProgramConfig.DEFAULT);

    assertEquals(4, subject.getBarBeats());
  }

  @Test
  public void defaultsToString() throws ValueException {
    var subject = new ProgramConfig(ProgramConfig.DEFAULT);

    assertArrayEquals(ProgramConfig.DEFAULT.split("\n"), subject.toString().split(System.lineSeparator()));
  }

}
