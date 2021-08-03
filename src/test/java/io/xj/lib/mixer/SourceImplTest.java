// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import com.google.inject.Guice;
import org.junit.Before;
import org.junit.Test;

import javax.sound.sampled.AudioFormat;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class SourceImplTest {
  private final MixerFactory mixerFactory = Guice.createInjector(new MixerModule()).getInstance(MixerFactory.class);

  private Source F32LSB_48kHz_Stereo;
  private Source S16LSB_44100Hz_Mono;

  @Before
  public void setUp() throws Exception {
    F32LSB_48kHz_Stereo = mixerFactory.createSource(
      "F32LSB_48kHz_Stereo",
      new BufferedInputStream(
        new FileInputStream(
          new InternalResource(
            "test_audio/F32LSB_48kHz_Stereo.wav"
          ).getFile())));

    S16LSB_44100Hz_Mono = mixerFactory.createSource(
      "S16LSB_44100Hz_Mono",
      new BufferedInputStream(
        new FileInputStream(
          new InternalResource(
            "test_audio/S16LSB_44100Hz_Mono.wav"
          ).getFile())));
  }

  @Test(expected = SourceException.class)
  public void unsupported_over2channels() throws Exception {
    assertNotNull(mixerFactory.createSource(
      "F32LSB_48kHz_6ch",
      new BufferedInputStream(
        new FileInputStream(
          new InternalResource(
            "test_audio/F32LSB_48kHz_6ch.wav"
          ).getFile()))));
  }

  @Test
  public void load24BitSourceAudio() throws Exception {
    assertNotNull(mixerFactory.createSource(
      "S24LSB_44100Hz_Stereo",
      new BufferedInputStream(
        new FileInputStream(
          new InternalResource(
            "test_audio/S24LSB_44100Hz_Stereo.wav"
          ).getFile()))));
  }

  @Test
  public void frameAt_F32LSB_48kHz_Stereo() {
    assertEquals(-0.0022547978442162275, F32LSB_48kHz_Stereo.getValue(243, 0), 0.00000001);
    assertEquals(-0.0014804069651290774, F32LSB_48kHz_Stereo.getValue(243, 1), 0.00000001);
  }

  @Test
  public void frameAt_S16LSB_44100Hz_Mono() {
    assertEquals(0.001220703125, S16LSB_44100Hz_Mono.getValue(125, 0), 0.00000001);
    assertEquals(0.001220703125, S16LSB_44100Hz_Mono.getValue(125, 1), 0.00000001);
  }

  @Test
  public void lengthMicros_F32LSB_48kHz_Stereo() {
    long lengthMicros = F32LSB_48kHz_Stereo.lengthMicros();
    assertEquals(361750, lengthMicros);
  }

  @Test
  public void lengthMicros_S16LSB_44100Hz_Mono() {
    long lengthMicros = S16LSB_44100Hz_Mono.lengthMicros();
    assertEquals(865306, lengthMicros);
  }

  @Test
  public void getInputFormat_F32LSB_48kHz_Stereo() {
    AudioFormat audioFormat = F32LSB_48kHz_Stereo.getInputFormat();
    assertEquals(2, audioFormat.getChannels());
    assertEquals(48000, audioFormat.getSampleRate(), 0);
    assertEquals(48000, audioFormat.getFrameRate(), 0);
    assertEquals(32, audioFormat.getSampleSizeInBits());
    assertEquals(8, audioFormat.getFrameSize());
    assertFalse(audioFormat.isBigEndian());
  }

  @Test
  public void getInputFormat_S16LSB_44100Hz_Mono() {
    AudioFormat audioFormat = S16LSB_44100Hz_Mono.getInputFormat();
    assertEquals(1, audioFormat.getChannels());
    assertEquals(44100, audioFormat.getSampleRate(), 0);
    assertEquals(44100, audioFormat.getFrameRate(), 0);
    assertEquals(16, audioFormat.getSampleSizeInBits());
    assertEquals(2, audioFormat.getFrameSize());
    assertFalse(audioFormat.isBigEndian());
  }

  @Test
  public void getState() {
    assertEquals(Source.READY, F32LSB_48kHz_Stereo.getState());
    assertEquals(Source.READY, S16LSB_44100Hz_Mono.getState());
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

}
