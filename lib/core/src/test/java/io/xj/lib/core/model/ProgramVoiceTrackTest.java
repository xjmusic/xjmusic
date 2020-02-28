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

public class ProgramVoiceTrackTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    ProgramVoiceTrack subject = new ProgramVoiceTrack()
      .setProgramId(UUID.randomUUID())
      .setProgramVoiceId(UUID.randomUUID())
      .setName("Mic Check One Two");
    subject.validate();

    assertEquals("MICCHECKONETWO", subject.getName());
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
