// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.util.ValueException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AccountTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();
  private Account subject;

  @Before
  public void setUp() {
    subject = new Account();
  }

  @Test
  public void validate() throws Exception {
    subject
      .setName("Mic Check One Two")
      .validate();
  }

  @Test
  public void validate_failsWithoutName() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Account name is required");

    subject
      .validate();
  }
}
