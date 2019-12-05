// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import io.xj.core.exception.CoreException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class UserTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    new User()
      .setRoles("User,Artist")
      .setEmail("charneykaye@gmail.com")
      .setAvatarUrl("http://www.google.com/images/charneykaye.jpg")
      .setName("Charney Kaye")
      .validate();
  }

  @Test
  public void validate_withMinimalAttributes() throws Exception {
    new User()
      .setRoles("User,Artist")
      .validate();
  }

  @Test
  public void validate_failsWithoutRoles() throws Exception {
    failure.expect(CoreException.class);
    failure.expectMessage("User roles is required");

    new User()
      .validate();
  }

}
