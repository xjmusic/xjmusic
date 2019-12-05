// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.core.exception.CoreException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static io.xj.core.testing.Assert.assertSameItems;

public class ProgramVoiceTrackTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new ProgramVoiceTrack()
      .setProgramId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID())
      .setName("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutVoiceId() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Voice ID is required");

    new ProgramVoiceTrack()
      .setProgramId(UUID.randomUUID())
      .setName("Thing")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    new ProgramVoiceTrack()
      .setProgramId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutProgramId() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Program ID is required");

    new ProgramVoiceTrack()
      .setProgramVoiceId(UUID.randomUUID())
      .setName("Mic Check One Two")
      .validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("name"), new ProgramVoiceTrack().getResourceAttributeNames());
  }

}
