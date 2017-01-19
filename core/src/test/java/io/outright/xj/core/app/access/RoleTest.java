package io.outright.xj.core.app.access;

import io.outright.xj.core.model.role.Role;

import org.junit.Test;

import static org.junit.Assert.*;

public class RoleTest {
  @Test
  public void isValid() throws Exception {
    assertFalse(Role.isValid("manuts"));
    assertTrue(Role.isValid("user"));
  }

}
