// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.link

class LinkImplTest extends GroovyTestCase {
  Link link;

  void setUp() {
    super.setUp()
    link = new LinkImpl()
  }

  void tearDown() {
    link = null
  }

  void testLink() {
    assert link != null
  }
}
