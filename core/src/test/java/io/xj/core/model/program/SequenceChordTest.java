//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.program;

import com.google.common.collect.ImmutableList;
import io.xj.core.CoreTest;
import io.xj.core.exception.CoreException;
import io.xj.core.model.program.sub.SequenceChord;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.UUID;

import static io.xj.core.testing.Assert.assertSameItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class SequenceChordTest extends CoreTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new SequenceChord()
      .setProgramId(BigInteger.valueOf(357L))
      .setName("C# minor")
      .setSequenceId(UUID.randomUUID())
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutProgram() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Program ID is required");

    new SequenceChord()
      .setName("C# minor")
      .setSequenceId(UUID.randomUUID())
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    new SequenceChord()
      .setProgramId(BigInteger.valueOf(357L))
      .setSequenceId(UUID.randomUUID())
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutSequenceUUID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Sequence ID is required");

    new SequenceChord()
      .setProgramId(BigInteger.valueOf(357L))
      .setName("C# minor")
      .setPosition(7.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutPosition() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Position is required");

    new SequenceChord()
      .setProgramId(BigInteger.valueOf(357L))
      .setName("C# minor")
      .setSequenceId(UUID.randomUUID())
      .validate();
  }

  /**
   [#154976066] Architect wants to limit the floating point precision of chord and chord position, in order to limit obsession over the position of things.
   */
  @Test
  public void position_rounded() {
    assertEquals(1.25, new SequenceChord().setPosition(1.25179957).getPosition(), 0.0000001);
  }

  @Test
  public void isNoChord() {
    assertFalse(new SequenceChord().setName("C#m7").isNoChord());
    assertTrue(new SequenceChord().setName("NC").isNoChord());
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("position", "name"), new SequenceChord().getResourceAttributeNames());
  }

}
