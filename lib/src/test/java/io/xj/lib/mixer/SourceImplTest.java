// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import com.google.inject.Guice;
import org.junit.Before;
import org.junit.Test;

import javax.sound.sampled.AudioFormat;

import static org.junit.Assert.*;

public class SourceImplTest {
  private final MixerFactory mixerFactory = Guice.createInjector(new MixerModule()).getInstance(MixerFactory.class);

  private Source F32LSB_48kHz_Stereo;
  private Source S16LSB_44100Hz_Mono;

  @Before
  public void setUp() throws Exception {
    F32LSB_48kHz_Stereo = mixerFactory.createSource(
      "F32LSB_48kHz_Stereo",
      new InternalResource("test_audio/F32LSB_48kHz_Stereo.wav").getFile().getAbsolutePath());

    S16LSB_44100Hz_Mono = mixerFactory.createSource(
      "S16LSB_44100Hz_Mono",
      new InternalResource("test_audio/S16LSB_44100Hz_Mono.wav").getFile().getAbsolutePath());
  }

  @Test(expected = SourceException.class)
  public void unsupported_over2channels() throws Exception {
    assertNotNull(mixerFactory.createSource(
      "F32LSB_48kHz_6ch",
      new InternalResource("test_audio/F32LSB_48kHz_6ch.wav").getFile().getAbsolutePath()));
  }

  @Test
  public void load24BitSourceAudio() throws Exception {
    assertNotNull(mixerFactory.createSource(
      "S24LSB_44100Hz_Stereo",
      new InternalResource("test_audio/S24LSB_44100Hz_Stereo.wav").getFile().getAbsolutePath()));
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
    AudioFormat audioFormat = F32LSB_48kHz_Stereo.getAudioFormat();
    assertEquals(2, audioFormat.getChannels());
    assertEquals(48000, audioFormat.getSampleRate(), 0);
    assertEquals(48000, audioFormat.getFrameRate(), 0);
    assertEquals(32, audioFormat.getSampleSizeInBits());
    assertEquals(8, audioFormat.getFrameSize());
    assertFalse(audioFormat.isBigEndian());
  }

  @Test
  public void getInputFormat_S16LSB_44100Hz_Mono() {
    AudioFormat audioFormat = S16LSB_44100Hz_Mono.getAudioFormat();
    assertEquals(1, audioFormat.getChannels());
    assertEquals(44100, audioFormat.getSampleRate(), 0);
    assertEquals(44100, audioFormat.getFrameRate(), 0);
    assertEquals(16, audioFormat.getSampleSizeInBits());
    assertEquals(2, audioFormat.getFrameSize());
    assertFalse(audioFormat.isBigEndian());
  }

  @Test
  public void getSourceId() {
    assertEquals("F32LSB_48kHz_Stereo", F32LSB_48kHz_Stereo.getSourceId());
    assertEquals("S16LSB_44100Hz_Mono", S16LSB_44100Hz_Mono.getSourceId());
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
    assertEquals(AudioFormat.Encoding.PCM_FLOAT, F32LSB_48kHz_Stereo.getAudioFormat().getEncoding());
    assertEquals(AudioFormat.Encoding.PCM_SIGNED, S16LSB_44100Hz_Mono.getAudioFormat().getEncoding());
  }

  @Test
  public void getLengthSeconds() {
    assertEquals(0.3617500066757202, F32LSB_48kHz_Stereo.getLengthSeconds(), .01);
    assertEquals(0.8653061389923096, S16LSB_44100Hz_Mono.getLengthSeconds(), .01);
  }
}
