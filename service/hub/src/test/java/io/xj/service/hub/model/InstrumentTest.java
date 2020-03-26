// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.ValueException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

public class InstrumentTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Instrument()
      .setUserId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID())
      .setType("Percussive")
      .setName("TR-808")
      .setState("Published")
      .validate();
  }

  @Test
  public void validate_failsWithoutType() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Type is required");

    new Instrument()
      .setUserId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID())
      .setName("TR-808")
      .validate();
  }

  @Test
  public void validate_failsWithoutState() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("State is required");

    new Instrument()
      .setTypeEnum(InstrumentType.Percussive)
      .setUserId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID())
      .setName("TR-808")
      .validate();
  }

  @Test
  public void validate_failsWithoutLibraryID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Library ID is required");

    new Instrument()
      .setUserId(UUID.randomUUID())
      .setType("Percussive")
      .setName("TR-808")
      .validate();
  }

  @Test
  public void validate_failsWithoutUserID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("User ID is required");

    new Instrument()
      .setLibraryId(UUID.randomUUID())
      .setType("Percussive")
      .setName("TR-808")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Name is required");

    new Instrument()
      .setUserId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID())
      .setState("Published")
      .setType("Percussive")
      .validate();
  }

  @Test
  public void validate_failsWithInvalidType() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("'butt' is not a valid type");

    new Instrument()
      .setUserId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID())
      .setStateEnum(InstrumentState.Published)
      .setType("butt")
      .setName("TR-808")
      .validate();
  }

  @Test
  public void validate_failsWithInvalidState() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("'butt' is not a valid state");

    new Instrument()
      .setTypeEnum(InstrumentType.Percussive)
      .setUserId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID())
      .setState("butt")
      .setName("TR-808")
      .validate();
  }
}
