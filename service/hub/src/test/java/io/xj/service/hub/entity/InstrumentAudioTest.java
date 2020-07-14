// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.util.ValueException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

public class InstrumentAudioTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new InstrumentAudio()
      .setLength(3.4)
      .setName("Mic Check One Two")
      .setPitch(57.4)
      .setStart(0.212)
      .setTempo(120.0)
      .setWaveformKey("instument-audio-1234543")
      .setInstrumentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutInstrumentID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Instrument ID is required");

    new InstrumentAudio()
      .setLength(3.4)
      .setName("Mic Check One Two")
      .setPitch(57.4)
      .setStart(0.212)
      .setTempo(120.0)
      .setWaveformKey("instument-audio-1234543")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Name is required");

    new InstrumentAudio()
      .setLength(3.4)
      .setPitch(57.4)
      .setStart(0.212)
      .setTempo(120.0)
      .setWaveformKey("instument-audio-1234543")
      .setInstrumentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutTempo() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Tempo is required");

    new InstrumentAudio()
      .setLength(3.4)
      .setName("Mic Check One Two")
      .setPitch(57.4)
      .setStart(0.212)
      .setWaveformKey("instument-audio-1234543")
      .setInstrumentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutRootPitch() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Root Pitch is required");

    new InstrumentAudio()
      .setLength(3.4)
      .setName("Mic Check One Two")
      .setStart(0.212)
      .setTempo(120.0)
      .setWaveformKey("instument-audio-1234543")
      .setInstrumentId(UUID.randomUUID())
      .validate();
  }
}
