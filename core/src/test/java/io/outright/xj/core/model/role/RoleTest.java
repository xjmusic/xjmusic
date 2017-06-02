// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.model.role;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertFalse;

public class RoleTest {

  @Rule public ExpectedException failure = ExpectedException.none();

  @Test
  public void validate() throws Exception {
    assert (Role.isValid(Role.ADMIN));
    assert (Role.isValid(Role.ENGINEER));
    assert (Role.isValid(Role.ARTIST));
    assert (Role.isValid(Role.USER));
    assert (Role.isValid(Role.BANNED));
  }

  @Test
  public void validate_internalIsNot() throws Exception {
    assertFalse(Role.isValid(Role.INTERNAL));
  }

  @Test
  public void validate_not() throws Exception {
    assertFalse(Role.isValid("garbage"));
  }

}
