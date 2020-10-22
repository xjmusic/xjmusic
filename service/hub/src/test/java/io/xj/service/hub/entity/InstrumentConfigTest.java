// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.util.ValueException;
import io.xj.service.hub.testing.HubTestConfiguration;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.junit.Assert.assertFalse;

public class InstrumentConfigTest {
  private InstrumentConfig subject;

  @Before
  public void setUp() throws ValueException {
    Instrument instrument1 = new Instrument()
      .setUserId(UUID.randomUUID())
      .setLibraryId(UUID.randomUUID())
      .setType("Percussive")
      .setName("TR-808")
      .setState("Published");
    subject = new InstrumentConfig(instrument1, HubTestConfiguration.getDefault());
  }

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void getMultiPhonic() {
    assertFalse(subject.getMultiPhonic());
  }

}
