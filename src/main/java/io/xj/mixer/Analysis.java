// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer;

import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.TarsosDSPAudioFormat;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import com.google.common.collect.Lists;

import java.util.List;

/**
 Equalizer utility performs DSP analysis turn-key
 <p>
 Stores complex values in buffer[channel][frame] in order to be able to treat each channel as a single buffer for analysis
 */
public class Analysis {
  private final float[][] buffer; // buffer[channel][frame]
  private final int channels;
  private final PitchProcessor pitchProcessor;
  private final TarsosDSPAudioFormat audioFormat;
  private final List<Float> detectedPitches = Lists.newLinkedList();

  /**
   Construct a new analysis from a buffer of complex values@param buffer buffer[channel][frame] in order to be able to treat each channel as a single buffer for analysis

   @param sampleRate of buffer, for computation of pitch
   */
  private Analysis(float[][] buffer, float sampleRate) {
    this.buffer = buffer;
    channels = buffer.length;
    pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, sampleRate, buffer[0].length,
      (PitchDetectionResult result, AudioEvent event) -> detectedPitches.add(result.getPitch()));
    audioFormat = new TarsosDSPAudioFormat(sampleRate, 16, channels, true, false);
  }

  /**
   Analyze a buffer on input audio
   The stored buffer of complex values is buffer[channel][frame] in order to be able to treat each channel as a single buffer for analysis

   @param buf        to analyze
   @param iFr        from frame
   @param iTo        to frame
   @param sampleRate of buffer, for computation of pitch
   @return Analysis ready to be performed on buffer
   */
  public static Analysis of(double[][] buf, int iFr, int iTo, float sampleRate) {
    return new Analysis(pivotedSubsetFloat(buf, iFr, iTo), sampleRate);
  }

  /**
   subset of a 2-D array of double values pivoted and converted into float values

   @param in     buffer of values to convert, buffer[frame][channel] which is the internal XJ mixer default
   @param inFrom index to copy from within buffer
   @param inTo   index to copy from within buffer
   @return 2-D array of complex values, buffer[channel][frame] in order to be able to treat each channel as a single buffer for analysis
   */
  static float[][] pivotedSubsetFloat(double[][] in, int inFrom, int inTo) {
    int iF = Math.max(inFrom, 0);
    int iT = Math.min(inTo, in.length);
    int breadth = in[0].length;
    int length = inTo - inFrom;
    float[][] out = new float[breadth][length];
    for (int inFrame = iF; inFrame < iT; inFrame++) {
      for (int channel = 0; channel < breadth; channel++)
        out[channel][inFrame - inFrom] = (float) in[inFrame][channel];
    }
    return out;
  }

  /**
   Compute the ratio that high frequencies are present

   @param highThresholdHz maximum frequency in Hz
   @return ratio between 0 and 1
   */
  public double ratioOfHighSpectrum(double highThresholdHz) {
    for (int channel = 0; channel < channels; channel++) {
      pitchProcessor.process(createAudioEvent(buffer[channel]));
    }
    float avgPitch = MathUtil.avg(detectedPitches);
    return Math.min(Math.max(0, avgPitch / highThresholdHz), 1);
  }

  /**
   Create a new audio event from a float buffer

   @param buffer to of audio event of
   @return new audio event
   */
  private AudioEvent createAudioEvent(float[] buffer) {
    AudioEvent audioEvent = new AudioEvent(audioFormat);
    audioEvent.setFloatBuffer(buffer);
    audioEvent.setOverlap(0);
    return audioEvent;
  }

  /**
   @return 2-D matrix buffer[channel][frame] in order to be able to treat each channel as a single buffer for analysis
   */
  public float[][] getBuffer() {
    return buffer;
  }

}
