// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.model;

import com.google.common.collect.ImmutableList;
import io.xj.lib.core.exception.CoreException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static io.xj.lib.core.testing.Assert.assertSameItems;

public class InstrumentMemeTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new InstrumentMeme()
      .setName("MicrophoneCheckOneTwoOneTwo")
      .setInstrumentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutInstrumentID() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Instrument ID is required");

    new InstrumentMeme()
      .setName("MicrophoneCheckOneTwoOneTwo")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Name is required");

    new InstrumentMeme()
      .setInstrumentId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void getPayloadAttributeNames() {
    assertSameItems(ImmutableList.of("name"), new InstrumentMeme().getResourceAttributeNames());
  }

}
