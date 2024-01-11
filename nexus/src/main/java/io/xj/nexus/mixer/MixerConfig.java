// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.nexus.mixer;

import javax.sound.sampled.AudioFormat;

/**
 Configuration for mixer
 <p>
 Engineer wants final Segment audio mastered with a lookahead-attack compressor https://www.pivotaltracker.com/story/show/154112129
 */
public class MixerConfig {
  int totalBuses = 8;
  float totalSeconds = 6;
  AudioFormat outputFormat;
  float compressAheadSeconds = 0.1f;
  float compressDecaySeconds = 0.01f;
  float compressRatioMax = 1.38f;
  float compressRatioMin = 0.62f;
  float compressToAmplitude = 1.0f;
  float normalizationCeiling = 1.0f;
  Integer dspBufferSize = 1024; // DSP buffer size must be a power of 2
  String logPrefix = "";
  float highpassThresholdHz = 20.0f;
  float lowpassThresholdHz = 12000;

  float normalizationBoostThreshold = 1.38f;

  /**
   Instantiate a new mixer configuration with format and length (and default compression settings)

   @param outputFormat set
   */
  public MixerConfig(AudioFormat outputFormat) {
    this.outputFormat = outputFormat;
  }

  /**
   Get Log Prefix

   @return log prefix
   */
  public String getLogPrefix() {
    return logPrefix;
  }

  /**
   Set log prefix

   @param logPrefix to set
   @return this Mixer Config (for chaining methods)
   */
  public MixerConfig setLogPrefix(String logPrefix) {
    this.logPrefix = logPrefix;
    return this;
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
   Get compress to amplitude

   @return compress to amplitude
   */
  public float getCompressToAmplitude() {
    return compressToAmplitude;
  }

  /**
   Set compress to amplitude

   @param compressToAmplitude to set
   @return MixerConfig to chain setters
   */
  public MixerConfig setCompressToAmplitude(float compressToAmplitude) {
    this.compressToAmplitude = compressToAmplitude;
    return this;
  }

  /**
   Get compress ahead seconds

   @return compress ahead seconds
   */
  public float getCompressAheadSeconds() {
    return compressAheadSeconds;
  }

  /**
   Set compress ahead seconds

   @param compressAheadSeconds to set
   @return MixerConfig to chain setters
   */
  public MixerConfig setCompressAheadSeconds(float compressAheadSeconds) {
    this.compressAheadSeconds = compressAheadSeconds;
    return this;
  }

  /**
   Get compress decay seconds

   @return compress decay seconds
   */
  public float getCompressDecaySeconds() {
    return compressDecaySeconds;
  }

  /**
   Set compress decay seconds

   @param compressDecaySeconds to set
   @return MixerConfig to chain setters
   */
  public MixerConfig setCompressDecaySeconds(float compressDecaySeconds) {
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
  public float getCompressRatioMax() {
    return compressRatioMax;
  }

  /**
   Set max ratio to multiply by amplitude during compression

   @param compressRatioMax max ratio for compression
   @return MixerConfig to chain setters
   */
  public MixerConfig setCompressRatioMax(float compressRatioMax) {
    this.compressRatioMax = compressRatioMax;
    return this;
  }

  /**
   Get min ratio to multiply by amplitude during compression

   @return min ratio for compression
   */
  public float getCompressRatioMin() {
    return compressRatioMin;
  }

  /**
   Set min ratio to multiply by amplitude during compression

   @param compressRatioMin min ratio for compression
   @return MixerConfig to chain setters
   */
  public MixerConfig setCompressRatioMin(float compressRatioMin) {
    this.compressRatioMin = compressRatioMin;
    return this;
  }

  /**
   Get normalization max value

   @return normalization max value
   */
  public float getNormalizationCeiling() {
    return normalizationCeiling;
  }

  /**
   Set normalization max value

   @param normalizationCeiling value
   @return MixerConfig to chain setters
   */
  public MixerConfig setNormalizationCeiling(float normalizationCeiling) {
    this.normalizationCeiling = normalizationCeiling;
    return this;
  }

  /**
   @return normalization boost threshold
   */
  public float getNormalizationBoostThreshold() {
    return normalizationBoostThreshold;
  }

  /**
   Set the normalization boost threshold

   @param normalizationBoostThreshold limit of how much to boost during normalization
   */
  public MixerConfig setNormalizationBoostThreshold(float normalizationBoostThreshold) {
    this.normalizationBoostThreshold = normalizationBoostThreshold;
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
  public float getHighpassThresholdHz() {
    return highpassThresholdHz;
  }

  /**
   @param highpassThresholdHz to set
   @return this, for chaining setters
   */
  public MixerConfig setHighpassThresholdHz(float highpassThresholdHz) {
    this.highpassThresholdHz = highpassThresholdHz;
    return this;
  }

  /**
   @return compressor lowpass threshold in Hz
   */
  public float getLowpassThresholdHz() {
    return lowpassThresholdHz;
  }

  /**
   @param lowpassThresholdHz to set
   @return this, for chaining setters
   */
  public MixerConfig setLowpassThresholdHz(float lowpassThresholdHz) {
    this.lowpassThresholdHz = lowpassThresholdHz;
    return this;
  }


  /**
   @return the total number of buses
   */
  public int getTotalBuses() {
    return totalBuses;
  }

  /**
   set the total number of buses

   @param totalBuses to set
   @return MixerConfig to chain setters
   */
  public MixerConfig setTotalBuses(int totalBuses) {
    this.totalBuses = totalBuses;
    return this;
  }

  /**
   @return the total number of seconds
   */
  public float getTotalSeconds() {
    return totalSeconds;
  }

  /**
   set the total number of seconds

   @param totalSeconds to set
   @return MixerConfig to chain setters
   */
  public MixerConfig setTotalSeconds(float totalSeconds) {
    this.totalSeconds = totalSeconds;
    return this;
  }

  public String toString() {
    return "MixerConfig{" +
      "outputFormat=" + outputFormat +
      ", compressToAmplitude=" + compressToAmplitude +
      ", compressAheadSeconds=" + compressAheadSeconds +
      ", compressDecaySeconds=" + compressDecaySeconds +
      ", compressRatioMax=" + compressRatioMax +
      ", compressRatioMin=" + compressRatioMin +
      ", normalizationCeiling=" + normalizationCeiling +
      ", normalizationBoostThreshold=" + normalizationBoostThreshold +
      ", dspBufferSize=" + dspBufferSize +
      ", highpassThresholdHz=" + highpassThresholdHz +
      ", lowpassThresholdHz=" + lowpassThresholdHz +
      ", logPrefix='" + logPrefix + '\'' +
      '}';
  }
}
