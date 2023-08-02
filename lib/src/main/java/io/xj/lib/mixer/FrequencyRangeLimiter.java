// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.mixer;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.filters.HighPass;
import be.tarsos.dsp.filters.LowPassFS;
import be.tarsos.dsp.io.TarsosDSPAudioFloatConverter;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.io.jvm.JVMAudioInputStream;

import javax.sound.sampled.AudioInputStream;
import java.io.ByteArrayInputStream;
import java.util.Objects;

/**
 * Engineer wants high-pass and low-pass filters with gradual thresholds, in order to be optimally heard but not listened to. https://www.pivotaltracker.com/story/show/161670248
 * FrequencyRangeLimiter utility performs DSP filter turn-key
 * <p>
 * Stores values in buffer[channel][frame] in order to be able to treat each channel as a single buffer for frequency range limiter
 */
public class FrequencyRangeLimiter {
  final AudioDispatcher audioDispatcher;
  final FloatBufferCatcher floatBufferCatcher;
  final float[] floatBuffer;

  /**
   * Construct a new frequency range limiter from a buffer of complex values@param buffer buffer[channel][frame] in order to be able to treat each channel as a single buffer for frequency range limiter to filter
   *
   * @param sampleRate          of buffer, for computation of pitch
   * @param audioBufferSize     The size of the buffer defines how much samples are processed in one step. Common values are 1024,2048.
   * @param highpassThresholdHz frequency in Hz, above which to allow audio to pass
   * @param lowpassThresholdHz  frequency in Hz, below which to allow audio to pass
   */
  FrequencyRangeLimiter(float[] floatBuffer, float sampleRate, int audioBufferSize, float highpassThresholdHz, float lowpassThresholdHz) {
    this.floatBuffer = floatBuffer;
    int frames = floatBuffer.length;
    TarsosDSPAudioFormat audioFormat = new TarsosDSPAudioFormat(sampleRate, 16, 1, true, false);
    TarsosDSPAudioFloatConverter floatConverter = TarsosDSPAudioFloatConverter.getConverter(audioFormat);
    byte[] byteBuffer = new byte[frames * audioFormat.getFrameSize()];
    floatConverter.toByteArray(floatBuffer, byteBuffer);
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteBuffer);
    AudioInputStream inputStream = new AudioInputStream(byteArrayInputStream, JVMAudioInputStream.toAudioFormat(audioFormat),
      frames);
    JVMAudioInputStream jvmAudioInputStream = new JVMAudioInputStream(inputStream);
    audioDispatcher = new AudioDispatcher(jvmAudioInputStream, audioBufferSize, audioBufferSize / 2);
    audioDispatcher.addAudioProcessor(new LowPassFS(lowpassThresholdHz, sampleRate));
    audioDispatcher.addAudioProcessor(new HighPass(highpassThresholdHz, sampleRate));
    floatBufferCatcher = new FloatBufferCatcher(floatBuffer.length);
    audioDispatcher.addAudioProcessor(floatBufferCatcher);
  }

  /**
   * Filter a buffer of input audio
   * The stored buffer of complex values is buffer[channel][frame] in order to be able to treat each channel as a single buffer for frequency range limiter
   * The final result is pivoted and written back as double values to the original array
   *
   * @param buffer              to filter
   * @param sampleRate          of buffer, for computation of pitch
   * @param audioBufferSize     The size of the buffer defines how much samples are processed in one step. Common values are 1024,2048.
   * @param highpassThresholdHz frequency in Hz, above which to allow audio to pass
   * @param lowpassThresholdHz  frequency in Hz, below which to allow audio to pass
   */
  public static void filter(double[][] buffer, float sampleRate, int audioBufferSize, float highpassThresholdHz, float lowpassThresholdHz) throws MixerException {
    if (0 == buffer.length) return;
    int channels = buffer[0].length;
    for (int c = 0; c < channels; c++) {
      int frames = buffer.length;
      float[] inputAudio = new float[frames];
      copyToSingle(buffer, inputAudio, c);
      float[] outputAudio = filter(inputAudio, sampleRate, audioBufferSize, highpassThresholdHz, lowpassThresholdHz);
      copyToMulti(outputAudio, buffer, c);
    }
  }

  /**
   * Copy from an input multi-channel buffer of double values to a single-channel buffer of float values
   *
   * @param fromBuffer to source channel from
   * @param toBuffer   to write values onto
   * @param channel    # of channel to extract
   */
  static void copyToMulti(float[] fromBuffer, double[][] toBuffer, int channel) {
    int frames = fromBuffer.length;
    for (int frame = 0; frame < frames; frame++)
      toBuffer[frame][channel] = fromBuffer[frame];
  }

  /**
   * Copy from an input multi-channel buffer of double values to a single-channel buffer of float values
   *
   * @param fromBuffer to extract channel from
   * @param toBuffer   to write values onto
   * @param channel    # of channel to extract
   */
  static void copyToSingle(double[][] fromBuffer, float[] toBuffer, int channel) {
    int frames = fromBuffer.length;
    for (int frame = 0; frame < frames; frame++)
      toBuffer[frame] = (float) fromBuffer[frame][channel];
  }

  /**
   * Filter a single channel buffer of input audio
   *
   * @param sampleRate          of buffer, for computation of pitch
   * @param audioBufferSize     The size of the buffer defines how much samples are processed in one step. Common values are 1024,2048.
   * @param highpassThresholdHz frequency in Hz, above which to allow audio to pass
   * @param lowpassThresholdHz  frequency in Hz, below which to allow audio to pass
   * @return samples[frame]
   */
  static float[] filter(float[] buffer, float sampleRate, int audioBufferSize, float highpassThresholdHz, float lowpassThresholdHz) throws MixerException {
    FrequencyRangeLimiter filter = new FrequencyRangeLimiter(buffer, sampleRate, audioBufferSize, highpassThresholdHz, lowpassThresholdHz);
    filter.process();
    return filter.getBuffer();
  }

  /**
   * Get the current buffer from the filter, probably for using the results of the frequency range limiter operation downstream
   *
   * @return buffer of channel-
   * interlaced samples
   */
  float[] getBuffer() {
    return floatBuffer;
  }

  /**
   * Process the filter.
   */
  void process() throws MixerException {
    audioDispatcher.run();
    if (!Objects.equals(floatBufferCatcher.getCursor(), floatBuffer.length))
      throw new MixerException("FrequencyRangeLimiter filter resulted in output buffer of unexpected length!");
    System.arraycopy(floatBufferCatcher.getFloatBuffer(), 0, floatBuffer, 0, floatBuffer.length);
  }
}
