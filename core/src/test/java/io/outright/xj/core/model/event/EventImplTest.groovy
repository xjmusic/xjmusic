// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.event

class EventImplTest extends GroovyTestCase {
  Event event

  void setUp() {
    super.setUp()
    event = new EventImpl(
      (float) 0.95,
      (float) 0.24,
      "KICK",
      (float) 1.25,
      (float) 0.75,
      ""
    )
  }

  void tearDown() {
    event = null
  }

  void testVelocity() {
    assert event.Velocity() == (float) 0.95
  }

  void testTonality() {
    assert event.Tonality() == (float) 0.24
  }

  void testInflection() {
    assert event.Inflection() == "KICK"
  }

  void testPosition() {
    assert event.Position() == (float) 1.25
  }

  void testDuration() {
    assert event.Duration() == (float) 0.75
  }

  void testNote() {
    assert event.Note() == ""
  }
}
