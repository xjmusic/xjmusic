// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.credit

class CreditImplTest extends GroovyTestCase {
  Credit credit

  void setUp() {
    super.setUp()
    credit = new CreditImpl()
  }

  void tearDown() {
    credit = null
  }

  void testUser() {
    assert credit.User() == null
  }
}
