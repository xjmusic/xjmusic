package io.outright.xj.core.primitive.velocity

import io.outright.xj.core.exception.CeilingException
import io.outright.xj.core.exception.FloorException

// Copyright (c) 2016, Outright Mental Inc. (http://outright.io) All Rights Reserved.
class VelocityTest extends GroovyTestCase {

  void testValue() {
    assert new Velocity((float) 0.72).Value() == (float) 0.72
  }

  void testValue_CannotBeLessThanZero() {
    shouldFail FloorException, {
      new Velocity((float) -0.1).Value()
    }
  }

  void testValue_CannotBeMoreThanOne() {
    shouldFail CeilingException, {
      new Velocity((float) 1.1).Value()
    }
  }
}
