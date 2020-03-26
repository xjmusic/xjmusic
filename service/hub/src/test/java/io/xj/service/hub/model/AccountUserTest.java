// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.ValueException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

public class AccountUserTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();
  private AccountUser subject;

  @Before
  public void setUp() {
    subject = new AccountUser();
  }

  @Test
  public void validate() throws Exception {
    subject
      .setUserId(UUID.randomUUID())
      .setAccountId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutAccountId() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("Account ID is required");

    subject
      .setUserId(UUID.randomUUID())
      .validate();
  }

  @Test
  public void validate_failsWithoutUserId() throws Exception {
    failure.expect(ValueException.class);
    failure.expectMessage("User ID is required");

    subject
      .setAccountId(UUID.randomUUID())
      .validate();
  }
}
