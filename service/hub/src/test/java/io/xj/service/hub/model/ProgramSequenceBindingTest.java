// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.ValueException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

public class ProgramSequenceBindingTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ProgramSequenceBinding()
      .setProgramSequenceId(UUID.randomUUID())
      .setOffset(14L)
      .setProgramId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutSequenceUUID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Sequence ID is required");

    new ProgramSequenceBinding()
      .setOffset(14L)
      .setProgramId(UUID.randomUUID())
      .validate();
  }


  @Test
  public void validate_failsWithoutProgramID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Program ID is required");

    new ProgramSequenceBinding()
      .setProgramSequenceId(UUID.randomUUID())
      .setOffset(14L)
      .validate();
  }

  @Test
  public void validate_failsWithoutOffset() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Offset is required");

    new ProgramSequenceBinding()
      .setProgramSequenceId(UUID.randomUUID())
      .setProgramId(UUID.randomUUID())
      .validate();
  }
}
