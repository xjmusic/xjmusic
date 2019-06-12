//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.program;

import com.google.common.collect.ImmutableList;
import io.xj.core.CoreTest;
import io.xj.core.exception.CoreException;
import io.xj.core.model.program.sub.Sequence;
import io.xj.core.model.program.sub.SequenceBinding;
import io.xj.core.model.program.sub.SequenceBindingMeme;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.UUID;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SequenceBindingMemeTest extends CoreTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new SequenceBindingMeme()
      .setProgramId(BigInteger.valueOf(9812L))
      .setSequenceBindingId(UUID.randomUUID())
      .setName("MicrophoneCheckOneTwoOneTwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutProgram() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Program ID is required");

    new SequenceBindingMeme()
      .setSequenceBindingId(UUID.randomUUID())
      .setName("MicrophoneCheckOneTwoOneTwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutSequenceBindingId() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Sequence Binding ID is required");

    new SequenceBindingMeme()
      .setProgramId(BigInteger.valueOf(9812L))
      .setName("MicrophoneCheckOneTwoOneTwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    new SequenceBindingMeme()
      .setProgramId(BigInteger.valueOf(9812L))
      .setSequenceBindingId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("name"), new SequenceBindingMeme().getResourceAttributeNames());
  }

  @Test
  public void belongsTo() {
    Program program = newProgram(12, 101, 3, ProgramType.Main, ProgramState.Published, "Earth to Fire", "Ebm", 121.0, now());
    Sequence sequence = program.add(newSequence(5, "Test", 0.5, "C", 120.0));
    SequenceBinding sequenceBinding0 = program.add(newSequenceBinding(sequence,25));
    SequenceBinding sequenceBinding1 = program.add(newSequenceBinding(sequence,25));
    SequenceBindingMeme sequenceBindingMeme0 = program.add(newSequenceBindingMeme(sequenceBinding0, "Apple"));
    SequenceBindingMeme sequenceBindingMeme1 = program.add(newSequenceBindingMeme(sequenceBinding1, "Bananas"));

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
