// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.ValueException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ProgramSequenceBindingMemeTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ProgramSequenceBindingMeme()
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceBindingId(UUID.randomUUID())
      .setName("MicrophoneCheckOneTwoOneTwo")
      .validate();
  }

  @Test
  public void validate_modifiesNameToUpperSlug() throws ValueException {
    ProgramSequenceBindingMeme subject = new ProgramSequenceBindingMeme()
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceBindingId(UUID.randomUUID());

    subject.setName("Microphone Check! One Two One   Two").validate();
    assertEquals("MICROPHONECHECKONETWOONETWO", subject.getName());
  }

  @Test
  public void validate_failsWithoutProgram() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Program ID is required");

    new ProgramSequenceBindingMeme()
      .setProgramSequenceBindingId(UUID.randomUUID())
      .setName("MicrophoneCheckOneTwoOneTwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutSequenceBindingId() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Sequence Binding ID is required");

    new ProgramSequenceBindingMeme()
      .setProgramId(UUID.randomUUID())
      .setName("MicrophoneCheckOneTwoOneTwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Name is required");

    new ProgramSequenceBindingMeme()
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceBindingId(UUID.randomUUID())
      .validate();
  }

}
