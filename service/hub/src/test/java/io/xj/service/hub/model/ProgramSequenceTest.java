// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.ValueException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class ProgramSequenceTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();
  private ProgramSequence subject;

  @Before
  public void setUp() {
    subject = new ProgramSequence()
    ;
  }

  @Test
  public void validate() throws Exception {
    subject
      .setProgramId(UUID.randomUUID())
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutProgram() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Program ID is required");

    subject
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Name is required");

    subject
      .setProgramId(UUID.randomUUID())
      .setDensity(0.75)
      .setKey("D# major 7")
      .setTempo(120.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutKey() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Key is required");

    subject
      .setProgramId(UUID.randomUUID())
      .setDensity(0.75)
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .validate();
  }

  @Test
  public void validate_failsWithoutDensity() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Density is required");

    subject
      .setProgramId(UUID.randomUUID())
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .setTempo(120.0)
      .validate();
  }

  @Test
  public void validate_totalDefaultsToZero() throws Exception {
    subject
      .setProgramId(UUID.randomUUID())
      .setDensity(0.75)
      .setTempo(100.0)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .validate();

    assertEquals(Integer.valueOf(0), subject.getTotal());
  }

  @Test
  public void validate_failsWithoutTempo() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Tempo is required");

    subject
      .setProgramId(UUID.randomUUID())
      .setDensity(0.75)
      .setKey("D# major 7")
      .setName("Mic Check One Two")
      .validate();
  }

}
