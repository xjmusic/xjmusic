//  Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.mixer;

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
  private Double compressToAmplitude = 0.618;
  private Double compressAheadSeconds = 0.25;
  private Double compressDecaySeconds = 0.5;
  private Double compressRatioMax = 3.0;
  private Double normalizationMax = 1.0;
  private Double compressResolutionRate = 100.0;

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

   @return # frames per compression cycle
   */
  public int getCompressResolutionFrames() {
    return (int) Math.round(outputFormat.getFrameRate() / compressResolutionRate);
  }


  /**
   Get how frequently compression will be recalculated, in terms of # of seconds per cycle

   @return # seconds per compression cycle
   */
  public Double getCompressResolutionRate() {
    return compressResolutionRate;
  }

  /**
   Set how frequently compression will be recalculated, in terms of # of seconds per cycle

   @param compressResolutionRate value
   @return MixerConfig to chain setters
   */
  public MixerConfig setCompressResolutionRate(Double compressResolutionRate) {
    this.compressResolutionRate = compressResolutionRate;
    return this;
  }
}
