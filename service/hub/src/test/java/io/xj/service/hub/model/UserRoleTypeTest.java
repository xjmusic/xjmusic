// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.ValueException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class UserRoleTypeTest {

  @Rule
  public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    assertEquals(UserRoleType.Admin, UserRoleType.validate("Admin"));
    assertEquals(UserRoleType.Engineer, UserRoleType.validate("Engineer"));
    assertEquals(UserRoleType.Artist, UserRoleType.validate("Artist"));
    assertEquals(UserRoleType.User, UserRoleType.validate("User"));
    assertEquals(UserRoleType.Banned, UserRoleType.validate("Banned"));
  }

  @Test(expected = ValueException.class)
  public void validate_not() throws Exception {
    UserRoleType.validate("garbage");
  }

}
