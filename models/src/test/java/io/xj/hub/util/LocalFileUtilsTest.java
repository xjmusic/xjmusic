package io.xj.hub.util;

import io.xj.hub.pojos.InstrumentAudio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LocalFileUtilsTest {

  @Test
  void computeWaveformKey() {
    var audio = new InstrumentAudio();
    audio.setName("Wubby Bass C♭4");
    audio.setTones("C♭4");

    var waveformKey = LocalFileUtils.computeWaveformKey(
      "Wubby 5",
      audio,
      "wav"
    );

    assertEquals("Wubby-5-Wubby-Bass-Cb4-Cb4.wav", waveformKey);
  }

  @Test
  void computeWaveformKey_emptyPieces() {
    var audio = new InstrumentAudio();
    audio.setName("Wubby Bass C♭4");
    audio.setTones("");

    var waveformKey = LocalFileUtils.computeWaveformKey(
      "Wubby 5",
      audio,
      "wav"
    );

    assertEquals("Wubby-5-Wubby-Bass-Cb4.wav", waveformKey);
  }
}
