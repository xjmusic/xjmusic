package io.xj.hub.util;

import io.xj.hub.pojos.Instrument;
import io.xj.hub.pojos.InstrumentAudio;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LocalFileUtilsTest {

  @Test
  void computeWaveformKey() {
    var instrument = new Instrument();
    instrument.setName("Wubby 5");
    var audio = new InstrumentAudio();
    audio.setName("Wubby Bass C♭4");
    audio.setTones("C♭4");

    var waveformKey = LocalFileUtils.computeWaveformKey(instrument, audio, "wav");

    assertEquals("Wubby-5-Wubby-Bass-Cb4-Cb4.wav", waveformKey);
  }

  @Test
  void computeWaveformKey_emptyPieces() {
    var instrument = new Instrument();
    instrument.setName("Wubby 5");
    var audio = new InstrumentAudio();
    audio.setName("Wubby Bass C♭4");
    audio.setTones("");

    var waveformKey = LocalFileUtils.computeWaveformKey(instrument, audio, "wav");

    assertEquals("Wubby-5-Wubby-Bass-Cb4.wav", waveformKey);
  }
}
