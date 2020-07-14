// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.entity;

import io.xj.lib.util.ValueException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

public class ProgramTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate_FailsWithoutLibraryID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Library ID is required");

    Program.create()
      .setUserId(UUID.randomUUID())
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setState("Published")
      .setType("Main")
      .validate();
  }

  @Test
  public void validate_FailsWithoutUserID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("User ID is required");

    Program.create()
      .setLibraryId(UUID.randomUUID())
      .setKey("G minor 7")
      .setName("cannons")
      .setTempo(129.4)
      .setState("Published")
      .setType("Main")
      .validate();
  }

  @Test
  public void update_FailsWithoutName() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Name is required");

    Program.create()
      .setState("Published")
      .setType("Main")
      .setUserId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID())
      .validate();
  }
}

