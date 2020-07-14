// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import com.google.common.collect.ImmutableList;
import io.xj.lib.util.ValueException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import static org.junit.Assert.assertEquals;

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

  @Test
  public void accountIdsFromAccountUsers() {
    assertEquals(
      ImmutableList.of(
        UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e"),
        UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a")
      ),
      AccountUser.accountIdsFromAccountUsers(ImmutableList.of(
        AccountUser.create().setAccountId(UUID.fromString("4872f737-3526-4532-bb9f-358e3503db7e")),
        AccountUser.create().setAccountId(UUID.fromString("333d6284-d7b9-4654-b79c-cafaf9330b6a"))
      )));
  }
}
