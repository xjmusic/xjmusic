// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.lib.core.exception.CoreException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static io.xj.lib.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;

public class InstrumentAudioEventTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new InstrumentAudioEvent()
      .setInstrumentId(UUID.randomUUID())
      .setInstrumentAudioId(UUID.randomUUID())
      .setPosition(0.75)
      .setDuration(3.45)
      .setName("SMACK")
      .setNote("D6")
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutInstrumentId() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Instrument ID is required");

    new InstrumentAudioEvent()
      .setInstrumentAudioId(UUID.randomUUID())
      .setPosition(0.75)
      .setDuration(3.45)
      .setName("SMACK")
      .setNote("D6")
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutDuration() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Duration is required");

    new InstrumentAudioEvent()
      .setInstrumentId(UUID.randomUUID())
      .setInstrumentAudioId(UUID.randomUUID())
      .setPosition(0.75)
      .setName("SMACK")
      .setNote("D6")
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutAudioID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Audio ID is required");

    new InstrumentAudioEvent()
      .setInstrumentId(UUID.randomUUID())
      .setPosition(0.75)
      .setDuration(3.45)
      .setName("SMACK")
      .setNote("D6")
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Position is required");

    new InstrumentAudioEvent()
      .setInstrumentId(UUID.randomUUID())
      .setInstrumentAudioId(UUID.randomUUID())
      .setDuration(3.45)
      .setName("SMACK")
      .setNote("D6")
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    new InstrumentAudioEvent()
      .setInstrumentId(UUID.randomUUID())
      .setInstrumentAudioId(UUID.randomUUID())
      .setPosition(0.75)
      .setDuration(3.45)
      .setNote("D6")
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutNote() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Note is required");

    new InstrumentAudioEvent()
      .setInstrumentId(UUID.randomUUID())
      .setInstrumentAudioId(UUID.randomUUID())
      .setPosition(0.75)
      .setDuration(3.45)
      .setName("SMACK")
      .setVelocity(0.9)
      .validate();
  }

  @Test
  public void validate_failsWithoutVelocity() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Velocity is required");

    new InstrumentAudioEvent()
      .setInstrumentId(UUID.randomUUID())
      .setInstrumentAudioId(UUID.randomUUID())
      .setPosition(0.75)
      .setDuration(3.45)
      .setName("SMACK")
      .setNote("D6")
      .validate();
  }

  /**
   [#154976066] Architect wants to limit the floating point precision of chord and event position, in order to limit obsession over the position of things.
   */
  @Test
  public void position_rounded() {
    assertEquals(1.25, new InstrumentAudioEvent().setPosition(1.25179957).getPosition(), 0.0000001);
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("duration", "name", "note", "position", "velocity"), new InstrumentAudioEvent().getResourceAttributeNames());
  }

}
