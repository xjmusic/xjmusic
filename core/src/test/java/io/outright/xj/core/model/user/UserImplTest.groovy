// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.user

class UserImplTest extends GroovyTestCase {
  User user

  void setUp() {
    super.setUp()
    user = new UserImpl()
  }

  void tearDown() {
    user = null
  }

  void testUser() {
    assert user != null
  }
}
