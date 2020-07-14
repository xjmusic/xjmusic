// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.entity;

import io.xj.lib.util.ValueException;
import io.xj.service.nexus.NexusException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

public class SegmentMemeTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new SegmentMeme()
      .setSegmentId(UUID.randomUUID())
      .setName("MicrophoneCheckOneTwoOneTwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutSegmentID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Segment ID is required");

    new SegmentMeme()
      .setName("MicrophoneCheckOneTwoOneTwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Name is required");

    new SegmentMeme()
      .setSegmentId(UUID.randomUUID())
      .validate();
  }


}

