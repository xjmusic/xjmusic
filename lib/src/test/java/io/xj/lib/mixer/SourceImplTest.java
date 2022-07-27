// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.util.Modules;
import io.xj.lib.notification.NotificationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SourceImplTest {

  @Mock
  private NotificationProvider mockNotificationProvider;

  private MixerFactory mixerFactory;

  private Source F32LSB_48kHz_Stereo;
  private Source S16LSB_44100Hz_Mono;
  private Source empty;

  @Before
  public void setUp() throws Exception {
    mixerFactory = Guice.createInjector(Modules.override(new MixerModule()).with(new AbstractModule() {
      @Override
      protected void configure() {
        bind(NotificationProvider.class).toInstance(mockNotificationProvider);
      }
    })).getInstance(MixerFactory.class);

    F32LSB_48kHz_Stereo = mixerFactory.createSource(
      "F32LSB_48kHz_Stereo",
      new InternalResource("test_audio/F32LSB_48kHz_Stereo.wav").getFile().getAbsolutePath(), "test audio");

    S16LSB_44100Hz_Mono = mixerFactory.createSource(
      "S16LSB_44100Hz_Mono",
      new InternalResource("test_audio/S16LSB_44100Hz_Mono.wav").getFile().getAbsolutePath(), "test audio");

    empty = mixerFactory.createSource("x123", "/does/not/exists", "will fail surely");
  }

  @Test
  public void unsupported_over2channels() throws IOException, SourceException, FormatException {
    mixerFactory.createSource(
      "F32LSB_48kHz_6ch",
      new InternalResource("test_audio/F32LSB_48kHz_6ch.wav").getFile().getAbsolutePath(), "test audio");

    verify(mockNotificationProvider).publish(eq("Production-Chain Mix Source Failure"),eq("Failed to load source for Audio[F32LSB_48kHz_6ch] \"test audio\" because more than 2 input audio channels not allowed"));
  }

  @Test
  public void load24BitSourceAudio() throws Exception {
    assertNotNull(mixerFactory.createSource(
      "S24LSB_44100Hz_Stereo",
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
    verify(mockNotificationProvider).publish(eq("Production-Chain Mix Source Failure"),eq("Failed to load source for Audio[x123] \"will fail surely\" because /does/not/exists (No such file or directory)"));
  }
}
