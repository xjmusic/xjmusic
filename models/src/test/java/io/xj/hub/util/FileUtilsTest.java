package io.xj.hub.util;

import io.xj.hub.pojos.InstrumentAudio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileUtilsTest {

  @Test
  void computeWaveformKey() {
    var audio = new InstrumentAudio();
    audio.setName("Wubby Bass C♭4");
    audio.setTones("C♭4");

    var waveformKey = FileUtils.computeWaveformKey(
      "Slaps",
      "LoFi Bass Instruments",
      "Wubby 5",
      audio,
      "wav"
    );

    assertEquals("Slaps-LoFi-Bass-Instruments-Wubby-5-Wubby-Bass-Cb4-Cb4.wav", waveformKey);
  }

  @Test
  void computeWaveformKey_emptyPieces() {
    var audio = new InstrumentAudio();
    audio.setName("Wubby Bass C♭4");
    audio.setTones("");

    var waveformKey = FileUtils.computeWaveformKey(
      "Slaps",
      "LoFi Bass Instruments",
      "Wubby 5",
      audio,
      "wav"
    );

    assertEquals("Slaps-LoFi-Bass-Instruments-Wubby-5-Wubby-Bass-Cb4.wav", waveformKey);
  }
}
