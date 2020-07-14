// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.util.ValueException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

public class LibraryTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();
  private Library subject;

  @Before
  public void setUp() {
    subject = new Library();
  }

  @Test
  public void validate() throws Exception {
    subject
      .setAccountId(UUID.randomUUID())
      .setName("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutAccountID() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Account ID is required");

    subject
      .setName("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Name is required");

    subject
      .setAccountId(UUID.randomUUID())
      .validate();
  }
}
