// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.app.access;

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
