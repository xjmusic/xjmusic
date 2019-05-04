// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.audio;

import io.xj.core.exception.CoreException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class AudioTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Audio()
      .setInstrumentId(BigInteger.valueOf(53L))
      .setLength(3.4)
      .setName("Mic Check One Two")
      .setPitch(57.4)
      .setStart(0.212)
      .setTempo(120.0)
      .setWaveformKey("instument-audio-1234543")
      .validate();
  }

  @Test
  public void validate_failsWithoutInstrumentID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Instrument ID is required");

    new Audio()
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
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    new Audio()
      .setInstrumentId(BigInteger.valueOf(53L))
      .setLength(3.4)
      .setPitch(57.4)
      .setStart(0.212)
      .setTempo(120.0)
      .setWaveformKey("instument-audio-1234543")
      .validate();
  }

  @Test
  public void validate_failsWithoutTempo() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Tempo is required");

    new Audio()
      .setInstrumentId(BigInteger.valueOf(53L))
      .setLength(3.4)
      .setName("Mic Check One Two")
      .setPitch(57.4)
      .setStart(0.212)
      .setWaveformKey("instument-audio-1234543")
      .validate();
  }

  @Test
  public void validate_failsWithoutRootPitch() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Root Pitch is required");

    new Audio()
      .setInstrumentId(BigInteger.valueOf(53L))
      .setLength(3.4)
      .setName("Mic Check One Two")
      .setStart(0.212)
      .setTempo(120.0)
      .setWaveformKey("instument-audio-1234543")
      .validate();
  }

}
