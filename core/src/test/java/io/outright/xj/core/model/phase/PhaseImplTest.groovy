// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.phase

class PhaseImplTest extends GroovyTestCase {
  Phase phase

  void setUp() {
    super.setUp()
    phase = new PhaseImpl(
      "Phase Q",
      2,
      (float) 8,
      (float) 0.43,
      "Dm",
      (float) 86
    )
  }

  void tearDown() {
    phase = null
  }

  void testName() {
    assert phase.Name() == "Phase Q"
  }

  void testOffset() {
    assert phase.Offset() == 2
  }

  void testTotal() {
    assert phase.Total() == (float) 8
  }

  void testDensity() {
    assert phase.Density() == (float) 0.43
  }

  void testKey() {
    assert phase.Key() == "Dm"
  }

  void testTempo() {
    assert phase.Tempo() == (float) 86
  }

  void testMemes() {
    assert phase.Memes().length == 0
  }

  void testChords() {
    assert phase.Chords().length == 0
  }

  void testVoices() {
    assert phase.Voices().length == 0
  }
}
