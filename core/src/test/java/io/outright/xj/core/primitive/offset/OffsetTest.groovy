package io.outright.xj.core.primitive.offset

// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
class OffsetTest extends GroovyTestCase {
  private Offset offset

  void setUp() {
    super.setUp()
    offset = new Offset(72)
  }

  void tearDown() {
    offset = null
  }

  void testValue() {
    assert offset.Value() == 72
  }
}
