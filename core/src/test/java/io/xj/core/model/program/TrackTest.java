//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.program;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import io.xj.core.model.program.sub.Track;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;
import java.util.UUID;

import static io.xj.core.testing.Assert.assertSameItems;

public class TrackTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new Track()
      .setProgramId(BigInteger.valueOf(9812L))
      .setVoiceId(UUID.randomUUID())
      .setName("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutVoiceId() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Voice ID is required");

    new Track()
      .setProgramId(BigInteger.valueOf(9812L))
      .setName("Thing")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    new Track()
      .setProgramId(BigInteger.valueOf(9812L))
      .setVoiceId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutProgramId() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Program ID is required");

    new Track()
      .setVoiceId(UUID.randomUUID())
      .setName("Mic Check One Two")
      .validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("name"), new Track().getResourceAttributeNames());
  }

}
