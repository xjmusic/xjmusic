//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer;

import io.xj.mixer.impl.exception.MixerException;
import io.xj.mixer.util.MathUtil;

import javax.sound.sampled.AudioFormat;
import java.time.Duration;

/**
 Configuration for mixer
 <p>
 [#154112129] Engineer wants final Segment audio mastered with a lookahead-attack compressor
 */
public class MixerConfig {
  private AudioFormat outputFormat;
  private Duration outputLength;
  private Double compressToAmplitude = 5.0;
  private Double compressAheadSeconds = 0.1;
  private Double compressDecaySeconds = 0.01;
  private Double compressRatioMax = 10.0;
  private Double compressRatioMin = 0.5;
  private Double normalizationMax = 1.0;
  private Integer dspBufferSize = 1024; // DSP buffer size must be a power of 2
  private double lowpassThresholdHz = 12000;
  private double highpassThresholdHz = 20.0;

  /**
   Instantiate a new mixer configuration with format and length (and default compression settings)

   @param outputFormat set
   @param outputLength set
   */
  public MixerConfig(AudioFormat outputFormat, Duration outputLength) {
    this.outputFormat = outputFormat;
    this.outputLength = outputLength;
  }

  /**
   Get output format

   @return output format
   */
  public AudioFormat getOutputFormat() {
    return outputFormat;
  }

  /**
   Set output format

   @param outputFormat to set
   @return MixerConfig to chain setters
   */
  public MixerConfig setOutputFormat(AudioFormat outputFormat) {
    this.outputFormat = outputFormat;
    return this;
  }

  /**
   Get output length

   @return output length
   */
  public Duration getOutputLength() {
    return outputLength;
  }

  /**
   Set output length

   @param outputLength to set
   @return MixerConfig to chain setters
   */
  public MixerConfig setOutputLength(Duration outputLength) {
    this.outputLength = outputLength;
    return this;
  }

  /**
   Get compress to amplitude

   @return compress to amplitude
   */
  public Double getCompressToAmplitude() {
    return compressToAmplitude;
  }

  /**
   Set compress to amplitude

   @param compressToAmplitude to set
   @return MixerConfig to chain setters
   */
  public MixerConfig setCompressToAmplitude(Double compressToAmplitude) {
    this.compressToAmplitude = compressToAmplitude;
    return this;
  }

  /**
   Get compress ahead seconds

   @return compress ahead seconds
   */
  public Double getCompressAheadSeconds() {
    return compressAheadSeconds;
  }

  /**
   Set compress ahead seconds

   @param compressAheadSeconds to set
   @return MixerConfig to chain setters
   */
  public MixerConfig setCompressAheadSeconds(Double compressAheadSeconds) {
    this.compressAheadSeconds = compressAheadSeconds;
    return this;
  }

  /**
   Get compress decay seconds

   @return compress decay seconds
   */
  public Double getCompressDecaySeconds() {
    return compressDecaySeconds;
  }

  /**
   Set compress decay seconds

   @param compressDecaySeconds to set
   @return MixerConfig to chain setters
   */
  public MixerConfig setCompressDecaySeconds(Double compressDecaySeconds) {
    this.compressDecaySeconds = compressDecaySeconds;
    return this;
  }

  /**
   Get # of frames to look ahead during compression

   @return # frames ahead
   */
  public int getCompressAheadFrames() {
    return (int) Math.round(outputFormat.getFrameRate() * compressAheadSeconds);
  }

  /**
   Get # of frames to decay during compression

   @return # frames decay
   */
  public int getCompressDecayFrames() {
    return (int) Math.round(outputFormat.getFrameRate() * compressDecaySeconds);
  }

  /**
   Get max ratio to multiply by amplitude during compression

   @return max ratio for compression
   */
  public Double getCompressRatioMax() {
    return compressRatioMax;
  }

  /**
   Set max ratio to multiply by amplitude during compression

   @param compressRatioMax max ratio for compression
   @return MixerConfig to chain setters
   */
  public MixerConfig setCompressRatioMax(Double compressRatioMax) {
    this.compressRatioMax = compressRatioMax;
    return this;
  }

  /**
   Get min ratio to multiply by amplitude during compression

   @return min ratio for compression
   */
  public Double getCompressRatioMin() {
    return compressRatioMin;
  }

  /**
   Set min ratio to multiply by amplitude during compression

   @param compressRatioMin min ratio for compression
   @return MixerConfig to chain setters
   */
  public MixerConfig setCompressRatioMin(Double compressRatioMin) {
    this.compressRatioMin = compressRatioMin;
    return this;
  }

  /**
   Get normalization max value

   @return normalization max value
   */
  public Double getNormalizationMax() {
    return normalizationMax;
  }

  /**
   Set normalization max value

   @param normalizationMax value
   @return MixerConfig to chain setters
   */
  public MixerConfig setNormalizationMax(Double normalizationMax) {
    this.normalizationMax = normalizationMax;
    return this;
  }

  /**
   Get how frequently compression will be recalculated, in terms of # of frames per cycle
   Note: MUST be a power of 2

   @return # seconds per compression cycle
   */
  public Integer getDSPBufferSize() {
    return dspBufferSize;
  }

  /**
   Set how frequently compression will be recalculated, in terms of # of seconds per cycle

   @param dspBufferSize value
   @return MixerConfig to chain setters
   */
  public MixerConfig setDSPBufferSize(Integer dspBufferSize) throws MixerException {
    if (!MathUtil.isPowerOfTwo(dspBufferSize)) {
      throw new MixerException("Compressor resolution frames must be a power of 2");
    }

    this.dspBufferSize = dspBufferSize;
    return this;
  }


  /**
   @return compressor highpass threshold in Hz
   */
  public double getHighpassThresholdHz() {
    return highpassThresholdHz;
  }

  /**
   @param highpassThresholdHz to set
   @return this, for chaining setters
   */
  public MixerConfig setHighpassThresholdHz(double highpassThresholdHz) {
    this.highpassThresholdHz = highpassThresholdHz;
    return this;
  }

  /**
   @return compressor lowpass threshold in Hz
   */
  public double getLowpassThresholdHz() {
    return lowpassThresholdHz;
  }

  /**
   @param lowpassThresholdHz to set
   @return this, for chaining setters
   */
  public MixerConfig setLowpassThresholdHz(double lowpassThresholdHz) {
    this.lowpassThresholdHz = lowpassThresholdHz;
    return this;
  }

}
