// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.ValueException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class InstrumentInstrumentAudioChordTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new InstrumentAudioChord()
      .setName("C# minor")
      .setInstrumentId(UUID.randomUUID())
      .setInstrumentAudioId(UUID.randomUUID())
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Name is required");

    new InstrumentAudioChord()
      .setInstrumentId(UUID.randomUUID())
      .setInstrumentAudioId(UUID.randomUUID())
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutInstrumentID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Instrument ID is required");

    new InstrumentAudioChord()
      .setInstrumentAudioId(UUID.randomUUID())
      .setName("G major")
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutInstrumentAudioID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Audio ID is required");

    new InstrumentAudioChord()
      .setInstrumentId(UUID.randomUUID())
      .setName("C# minor")
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Position is required");

    new InstrumentAudioChord()
      .setInstrumentId(UUID.randomUUID())
      .setName("C# minor")
      .setInstrumentAudioId(UUID.randomUUID())
      .validate();
  }

  /**
   [#154976066] Architect wants to limit the floating point precision of chord and event position, in order to limit obsession over the position of things.
   */
  @Test
  public void position_rounded() {
    assertEquals(1.25, new InstrumentAudioChord().setPosition(1.25179957).getPosition(), 0.0000001);
  }

}
