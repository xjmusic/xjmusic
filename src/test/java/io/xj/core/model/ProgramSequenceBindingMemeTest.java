// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ProgramSequenceBindingMemeTest  {

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
  public void validate_failsWithoutProgram() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Program ID is required");

    new ProgramSequenceBindingMeme()
      .setProgramSequenceBindingId(UUID.randomUUID())
      .setName("MicrophoneCheckOneTwoOneTwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutSequenceBindingId() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Sequence Binding ID is required");

    new ProgramSequenceBindingMeme()
      .setProgramId(UUID.randomUUID())
      .setName("MicrophoneCheckOneTwoOneTwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    new ProgramSequenceBindingMeme()
      .setProgramId(UUID.randomUUID())
      .setProgramSequenceBindingId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("name"), new ProgramSequenceBindingMeme().getResourceAttributeNames());
  }

  @Test
  public void belongsTo() {
    ProgramSequence sequence = ProgramSequence.create(Program.create(), 4, "Test", 0.5, "C", 120.0);
    ProgramSequenceBinding sequenceBinding0 = ProgramSequenceBinding.create(sequence, 25);
    ProgramSequenceBinding sequenceBinding1 = ProgramSequenceBinding.create(sequence, 25);
    ProgramSequenceBindingMeme sequenceBindingMeme0 = ProgramSequenceBindingMeme.create(sequenceBinding0, "Apple");
    ProgramSequenceBindingMeme sequenceBindingMeme1 = ProgramSequenceBindingMeme.create(sequenceBinding1, "Bananas");

    assertTrue(sequenceBindingMeme0.belongsTo(sequenceBinding0));
    assertTrue(sequenceBindingMeme1.belongsTo(sequenceBinding1));
    assertTrue(sequenceBinding0.belongsTo(sequence));
    assertTrue(sequenceBinding1.belongsTo(sequence));
    assertFalse(sequenceBindingMeme0.belongsTo(sequenceBinding1));
    assertFalse(sequenceBindingMeme1.belongsTo(sequenceBinding0));
    assertFalse(sequenceBinding0.belongsTo(sequenceBinding1));
    assertFalse(sequenceBindingMeme1.belongsTo(sequenceBinding0));
  }

}
