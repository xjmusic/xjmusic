// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.util.ValueException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

public class InstrumentMemeTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new InstrumentMeme()
      .setName("MicrophoneCheckOneTwoOneTwo")
      .setInstrumentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutInstrumentID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Instrument ID is required");

    new InstrumentMeme()
      .setName("MicrophoneCheckOneTwoOneTwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Name is required");

    new InstrumentMeme()
      .setInstrumentId(UUID.randomUUID())
      .validate();
  }

}
