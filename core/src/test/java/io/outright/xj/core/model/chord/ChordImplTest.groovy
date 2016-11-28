// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.chord

class ChordImplTest extends GroovyTestCase {
  Chord chord

  void setUp() {
    super.setUp()
    chord = new ChordImpl("C Major", (float) 5.25)
  }

  void tearDown() {
    chord = null
  }

  void testName() {
    assert chord.Name() == "C Major"
  }

  void testPosition() {
    assert chord.Position() == (float) 5.25
  }
}
