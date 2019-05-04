// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.account_user;

import io.xj.core.exception.CoreException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.math.BigInteger;

public class AccountUserTest {
  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new AccountUser()
      .setUserId(BigInteger.valueOf(125434L))
      .setAccountId(BigInteger.valueOf(125434L))
      .validate();
  }

  @Test
  public void validate_failsWithoutAccountId() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("Account ID is required");

    new AccountUser()
      .setUserId(BigInteger.valueOf(125434L))
      .validate();
  }

  @Test
  public void validate_failsWithoutUserId() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("User ID is required");

    new AccountUser()
      .setAccountId(BigInteger.valueOf(125434L))
      .validate();
  }

}
