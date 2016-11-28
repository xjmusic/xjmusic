// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.meme

class MemeImplTest extends GroovyTestCase {
  Meme meme

  void setUp() {
    super.setUp()
    meme = new MemeImpl(
      "Funky",
      2
    )
  }

  void tearDown() {
    meme = null
  }

  void testName() {
    assert meme.Name() == "Funky"
  }

  void testOrder() {
    assert meme.Order() == 2
  }
}
