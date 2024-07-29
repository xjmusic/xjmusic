package io.xj.engine.audio;

import io.xj.model.pojos.InstrumentAudio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AudioLoaderImplTest {
  private final String pathToAudioFile;
  private InstrumentAudio audio;
  private AudioLoader subject;

  public AudioLoaderImplTest() throws URISyntaxException {
    String baseDir = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("project")).toURI()).getAbsolutePath();
    pathToAudioFile = baseDir + File.separator + "test-audio.wav";
  }

  @BeforeEach
  void setUp() {
    audio = new InstrumentAudio();
    audio.setId(UUID.randomUUID());
    audio.setInstrumentId(UUID.randomUUID());
    audio.setWaveformKey("test-audio.wav");
    subject = new AudioLoaderImpl();
  }

  @Test
  void load() throws UnsupportedAudioFileException, IOException {
    var result = subject.load(audio, pathToAudioFile);

    // Assert successful loading
    assertNotNull(result);
    assertEquals(audio.getId(), result.getId());
    assertEquals(audio.getWaveformKey(), result.getWaveformKey());
    assertEquals(pathToAudioFile, result.pathToAudioFile());
    assertEquals(2, result.format().getChannels());
    assertEquals(48000, result.format().getSampleRate());
    assertEquals(32, result.format().getSampleSizeInBits());
    assertEquals(8, result.format().getFrameSize());
    assertEquals(48000, result.format().getFrameRate());
    assertFalse(result.format().isBigEndian());
    assertEquals(17364, result.data().length);
    assertEquals(2, result.data()[0].length);
    assertFalse(result.isDifferent(audio));

    // Assert isDifferent() method when id and waveform key are changed
    var audio_differentIdAndWaveformKey = new InstrumentAudio();
    audio_differentIdAndWaveformKey.setId(UUID.randomUUID());
    audio_differentIdAndWaveformKey.setWaveformKey("test-audio2.wav");
    assertTrue(result.isDifferent(audio_differentIdAndWaveformKey));

    // Assert isDifferent() method when only waveform key is changed
    var audio_differentWaveformKey = new InstrumentAudio();
    audio_differentWaveformKey.setId(audio.getId());
    audio_differentWaveformKey.setWaveformKey("test-audio2.wav");
    assertTrue(result.isDifferent(audio_differentWaveformKey));
  }
}