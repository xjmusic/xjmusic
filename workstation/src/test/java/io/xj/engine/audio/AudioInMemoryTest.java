package io.xj.engine.audio;

import io.xj.model.pojos.InstrumentAudio;
import org.junit.Before;
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

@ExtendWith(MockitoExtension.class)
class AudioInMemoryTest {
  private final String pathToAudioFile;
  private InstrumentAudio audio;
  private AudioInMemory subject;

  public AudioInMemoryTest() throws URISyntaxException {
    String baseDir = new File(Objects.requireNonNull(getClass().getClassLoader().getResource("project")).toURI()).getAbsolutePath();
    pathToAudioFile = baseDir + File.separator + "test-audio.wav";
  }

  @BeforeEach
  void setUp() throws UnsupportedAudioFileException, IOException {
    audio = new InstrumentAudio();
    audio.setId(UUID.randomUUID());
    audio.setInstrumentId(UUID.randomUUID());
    audio.setWaveformKey("test-audio.wav");
    AudioLoader audioLoader = new AudioLoaderImpl();
    subject = audioLoader.load(audio, pathToAudioFile);
  }

  @Test
  void getId() {
    assertEquals(audio.getId(), subject.getId());
  }

  @Test
  void getWaveformKey() {
    assertEquals(audio.getWaveformKey(), subject.getWaveformKey());
  }

  @Test
  void lengthSeconds() {
    var result = subject.lengthSeconds();

    assertEquals(0.361750, result, 0.0000001);
  }

  @Test
  void isDifferent() {
    assertFalse(subject.isDifferent(audio));
  }

  @Test
  void audio() {
    assertEquals(audio, subject.audio());
  }

  @Test
  void format() {
    assertEquals(2, subject.format().getChannels());
    assertEquals(48000, subject.format().getSampleRate());
    assertEquals(32, subject.format().getSampleSizeInBits());
    assertEquals(8, subject.format().getFrameSize());
    assertEquals(48000, subject.format().getFrameRate());
  }

  @Test
  void pathToAudioFile() {
    assertEquals(pathToAudioFile, subject.pathToAudioFile());
  }

  @Test
  void data() {
    assertEquals(17364, subject.data().length);
    assertEquals(2, subject.data()[0].length);
  }
}