// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.util.ValueException;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.junit.Assert.assertFalse;

public class ProgramConfigTest {
  private ProgramConfig subject;

  @Before
  public void setUp() throws ValueException {
    Program program1 = Program.create()
      .setUserId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID())
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setState("Published")
      .setType("Main");
    subject = new ProgramConfig(program1, HubTestConfiguration.getDefault());
  }

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void doPatternRestartOnChord() {
    assertFalse(subject.doPatternRestartOnChord());
  }

  /**
   [#175548549] Program and Instrument parameters to turn off transposition and tonality.
   */
  @Test
  public void doTranspose() {
    assertFalse(subject.doTranspose());
  }

}
