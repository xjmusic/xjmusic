// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.lib.mixer;


import io.xj.hub.util.InternalResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class SourceImplTest {
  MixerFactory mixerFactory;

  Source F32LSB_48kHz_Stereo;
  Source S16LSB_44100Hz_Mono;
  Source empty;

  final UUID audioId_F32LSB_48kHz_6ch = UUID.randomUUID();
  final UUID audioId_F32LSB_48kHz_Stereo = UUID.randomUUID();
  final UUID audioId_S16LSB_44100Hz_Mono = UUID.randomUUID();

  @BeforeEach
  public void setUp() throws Exception {
    EnvelopeProvider envelopeProvider = new EnvelopeProviderImpl();
    mixerFactory = new MixerFactoryImpl(envelopeProvider);


    F32LSB_48kHz_Stereo = mixerFactory.createSource(
      audioId_F32LSB_48kHz_Stereo,
      new InternalResource("test_audio/F32LSB_48kHz_Stereo.wav").getFile().getAbsolutePath(), "test audio");

    S16LSB_44100Hz_Mono = mixerFactory.createSource(
      audioId_S16LSB_44100Hz_Mono,
      new InternalResource("test_audio/S16LSB_44100Hz_Mono.wav").getFile().getAbsolutePath(), "test audio");

    empty = mixerFactory.createSource(UUID.randomUUID(), "/does/not/exists", "will fail surely");
  }

  @Test
  public void unsupported_over2channels() throws IOException, SourceException, FormatException {
    mixerFactory.createSource(
      audioId_F32LSB_48kHz_6ch,
      new InternalResource("test_audio/F32LSB_48kHz_6ch.wav").getFile().getAbsolutePath(), "test audio");
  }

  @Test
  public void load24BitSourceAudio() throws Exception {
    assertNotNull(mixerFactory.createSource(
      audioId_F32LSB_48kHz_Stereo,
      new InternalResource("test_audio/S24LSB_44100Hz_Stereo.wav").getFile().getAbsolutePath(), "test audio"));
  }

  @Test
  public void lengthMicros_F32LSB_48kHz_Stereo() {
    long lengthMicros = F32LSB_48kHz_Stereo.getLengthMicros();
    assertEquals(361750, lengthMicros);
  }

  @Test
  public void lengthMicros_S16LSB_44100Hz_Mono() {
    long lengthMicros = S16LSB_44100Hz_Mono.getLengthMicros();
    assertEquals(865306, lengthMicros);
  }

  @Test
  public void getInputFormat_F32LSB_48kHz_Stereo() {
    AudioFormat audioFormat = F32LSB_48kHz_Stereo.getAudioFormat().orElseThrow();
    assertEquals(2, audioFormat.getChannels());
    assertEquals(48000, audioFormat.getSampleRate(), 0);
    assertEquals(48000, audioFormat.getFrameRate(), 0);
    assertEquals(32, audioFormat.getSampleSizeInBits());
    assertEquals(8, audioFormat.getFrameSize());
    assertFalse(audioFormat.isBigEndian());
  }

  @Test
  public void getInputFormat_S16LSB_44100Hz_Mono() {
    AudioFormat audioFormat = S16LSB_44100Hz_Mono.getAudioFormat().orElseThrow();
    assertEquals(1, audioFormat.getChannels());
    assertEquals(44100, audioFormat.getSampleRate(), 0);
    assertEquals(44100, audioFormat.getFrameRate(), 0);
    assertEquals(16, audioFormat.getSampleSizeInBits());
    assertEquals(2, audioFormat.getFrameSize());
    assertFalse(audioFormat.isBigEndian());
  }

  @Test
  public void getSourceId() {
    assertEquals(audioId_F32LSB_48kHz_Stereo, F32LSB_48kHz_Stereo.getAudioId());
    assertEquals(audioId_S16LSB_44100Hz_Mono, S16LSB_44100Hz_Mono.getAudioId());
  }

  @Test
  public void getFrameRate() {
    assertEquals(48000, F32LSB_48kHz_Stereo.getFrameRate(), 0);
    assertEquals(44100, S16LSB_44100Hz_Mono.getFrameRate(), 0);
  }

  @Test
  public void lengthMicros() {
    assertEquals(361750, F32LSB_48kHz_Stereo.getLengthMicros());
    assertEquals(865306, S16LSB_44100Hz_Mono.getLengthMicros());
  }

  @Test
  public void getInputFormat() {
    assertEquals(AudioFormat.Encoding.PCM_FLOAT, F32LSB_48kHz_Stereo.getAudioFormat().orElseThrow().getEncoding());
    assertEquals(AudioFormat.Encoding.PCM_SIGNED, S16LSB_44100Hz_Mono.getAudioFormat().orElseThrow().getEncoding());
  }

  @Test
  public void getLengthSeconds() {
    assertEquals(0.3617500066757202, F32LSB_48kHz_Stereo.getLengthSeconds(), .01);
    assertEquals(0.8653061389923096, S16LSB_44100Hz_Mono.getLengthSeconds(), .01);
  }

  /**
   Fabrication should not completely fail because of one bad source audio https://www.pivotaltracker.com/story/show/182575665
   */
  @Test
  public void empty() {
    assertTrue(empty.getAudioFormat().isEmpty());
  }
}
