// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.model.voice

class VoiceImplTest extends GroovyTestCase {
  Voice voice

  void setUp() {
    super.setUp()
    voice = new VoiceImpl(
      Type.PERCUSSIVE,
      "808"
    )
  }

  void tearDown() {
    voice = null
  }

  void testType() {
    assert voice.Type() == Type.PERCUSSIVE
  }

  void testDescription() {
    assert voice.Description() == "808"
  }

  void testEvents() {
    assert voice.Events().length == 0
  }
}
