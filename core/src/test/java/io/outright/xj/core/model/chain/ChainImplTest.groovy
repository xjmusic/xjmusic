// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.chain

class ChainImplTest extends GroovyTestCase {
  Chain chain

  void setUp() {
    super.setUp()
    chain = new ChainImpl()
  }

  void tearDown() {
    chain = null
  }

  void testLinks() {
    assert chain.Links().length == 0
  }
}
