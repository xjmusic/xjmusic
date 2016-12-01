// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.link

class LinkImplTest extends GroovyTestCase {
  Link link;

  void setUp() {
    super.setUp()
    link = new LinkImpl(
      7,
      State.COLLECTED,
      (float) 7.741935488,
      (float) 15.483870976,
      (float) 16.0,
      (float) 0.73,
      "G minor",
      (float) 124.0
    );
  }

  void tearDown() {
    link = null
  }

  void testLink() {
    assert link != null
  }
}
