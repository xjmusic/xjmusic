// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.service.hub.entity;

import com.google.common.collect.ImmutableList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserRoleTest {

  @Test
  public void typesFromCsv() {
    assertEquals(ImmutableList.of(UserRoleType.User, UserRoleType.Engineer),
      UserRole.typesOf(ImmutableList.of(
        UserRole.create(User.create(), "User"),
        UserRole.create(User.create(), "Engineer")
      )));
  }
}
