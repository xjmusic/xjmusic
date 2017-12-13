// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.access;

import io.xj.core.model.role.Role;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RoleTest {
  @Test
  public void isValid() throws Exception {
    assertFalse(Role.isValid("manuts"));
    assertTrue(Role.isValid("user"));
  }

}
