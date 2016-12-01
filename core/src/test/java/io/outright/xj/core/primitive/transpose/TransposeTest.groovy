package io.outright.xj.core.primitive.transpose

// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
class TransposeTest extends GroovyTestCase {

  void testValue_Up() {
    assert new Transpose(4).Value() == 4
  }

  void testValue_Down() {
    assert new Transpose(-3).Value() == -3
  }

}
