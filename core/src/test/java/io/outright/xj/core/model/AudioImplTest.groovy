// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model

class AudioImplTest extends GroovyTestCase {
  Audio audio

  void setUp() {
    super.setUp()
    audio = new AudioImpl(
      "main/percussion/classic/808/kick.wav",
      (float) 1.4,
      (float) 0.0115,
      (float) 90,
      (float) 110
    )
  }

  void tearDown() {
    audio = null
  }

  void testWaveform() {
    assert audio.Waveform() == "main/percussion/classic/808/kick.wav"
  }

  void testLength() {
    assert audio.Length() == (float) 1.4
  }

  void testStart() {
    assert audio.Start() == (float) 0.0115
  }

  void testTempo() {
    assert audio.Tempo() == (float) 90
  }

  void testPitch() {
    assert audio.Pitch() == (float) 110
  }

  void testChords() {
    assert audio.Chords().length == 0
  }

  void testEvents() {
    assert audio.Events().length == 0
  }
}
