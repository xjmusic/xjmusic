// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;

import javax.sound.sampled.AudioFormat;
import java.util.Map;

/**
 Parse a TypeSafe `config` value for a Template's configuration, overriding values from top-level default.conf--
 e.g.
 if the `config` value contains only `previewLengthMaxHours = 8`
 <p>
 [#177355683] Artist saves Template config, validate & combine with defaults.
 */
public class TemplateConfig {
  public static final String DEFAULT =
    """
      backgroundLayerMax = 3
      backgroundLayerMin = 0
      bufferAheadSeconds = 180
      bufferBeforeSeconds = 5
      deltaArcDetailLayersIncoming = 1
      deltaArcDetailPlateauRatio = 0.62
      deltaArcEnabled = true
      deltaArcRhythmLayersIncoming = 1
      deltaArcRhythmLayersToPrioritize = kick
      deltaArcRhythmPlateauRatio = 0.38
      densityCeiling = 0.9
      densityFloor = 0.1
      dubMasterVolumeInstrumentTypeBass = 1.0
      dubMasterVolumeInstrumentTypeDrum = 1.0
      dubMasterVolumeInstrumentTypePad = 1.0
      dubMasterVolumeInstrumentTypePercLoop = 1.0
      dubMasterVolumeInstrumentTypeStab = 1.0
      dubMasterVolumeInstrumentTypeSticky = 1.0
      dubMasterVolumeInstrumentTypeStripe = 1.0
      mainProgramLengthMaxDelta = 280
      metaSource = ""
      metaTitle = ""
      mixerCompressAheadSeconds = 0.05
      mixerCompressDecaySeconds = 0.125
      mixerCompressRatioMax = 1.0
      mixerCompressRatioMin = 0.3
      mixerCompressToAmplitude = 1.0
      mixerDspBufferSize = 1024
      mixerHighpassThresholdHz = 30
      mixerLowpassThresholdHz = 15000
      mixerNormalizationBoostThreshold = 1.0
      mixerNormalizationCeiling = 0.999
      outputChannels = 2
      outputContainer = "OGG"
      outputContentType = "audio/ogg"
      outputEncoding = "PCM_DOUBLE"
      outputEncodingQuality = 0.618
      outputFrameRate = 48000
      outputSampleBits = 16
      percLoopLayerMax = 8
      percLoopLayerMin = 0
      transitionLayerMax = 3
      transitionLayerMin = 0
      """;
  private static final int BITS_PER_BYTE = 8;
  private final AudioFormat.Encoding outputEncoding;
  private final String deltaArcRhythmLayersToPrioritize;
  private final String metaSource;
  private final String metaTitle;
  private final String outputContainer;
  private final String outputContentType;
  private final boolean deltaArcEnabled;
  private final double deltaArcDetailPlateauRatio;
  private final double deltaArcRhythmPlateauRatio;
  private final double densityCeiling;
  private final double densityFloor;
  private final double dubMasterVolumeInstrumentTypeBass;
  private final double dubMasterVolumeInstrumentTypeDrum;
  private final double dubMasterVolumeInstrumentTypePad;
  private final double dubMasterVolumeInstrumentTypePercLoop;
  private final double dubMasterVolumeInstrumentTypeStab;
  private final double dubMasterVolumeInstrumentTypeSticky;
  private final double dubMasterVolumeInstrumentTypeStripe;
  private final double mixerCompressAheadSeconds;
  private final double mixerCompressDecaySeconds;
  private final double mixerCompressRatioMax;
  private final double mixerCompressRatioMin;
  private final double mixerCompressToAmplitude;
  private final double mixerNormalizationBoostThreshold;
  private final double mixerNormalizationCeiling;
  private final double outputEncodingQuality;
  private final int backgroundLayerMax;
  private final int backgroundLayerMin;
  private final int bufferAheadSeconds;
  private final int bufferBeforeSeconds;
  private final int deltaArcDetailLayersIncoming;
  private final int deltaArcRhythmLayersIncoming;
  private final int mainProgramLengthMaxDelta;
  private final int mixerDspBufferSize;
  private final int mixerHighpassThresholdHz;
  private final int mixerLowpassThresholdHz;
  private final int outputChannels;
  private final int outputFrameRate;
  private final int outputSampleBits;
  private final int percLoopLayerMax;
  private final int percLoopLayerMin;
  private final int transitionLayerMax;
  private final int transitionLayerMin;

  /**
   Get a template config from only the default config

   @throws ValueException on failure
   */
  public TemplateConfig() throws ValueException {
    this("");
  }

  /**
   Instantiate a Template configuration from a string of typesafe config.
   Said string will be embedded in a `template{...}` block such that
   provided simple Key=Value pairs will be understood as members of `template`
   e.g. will override values from the `template{...}` block of the top-level **default.conf**

   @param template to get config from
   */
  public TemplateConfig(Template template) throws ValueException {
    this(template.getConfig());
  }

  /**
   Instantiate a Template configuration from a string of typesafe config.
   Said string will be embedded in a `template{...}` block such that
   provided simple Key=Value pairs will be understood as members of `template`
   e.g. will override values from the `template{...}` block of the top-level **default.conf**
   */
  public TemplateConfig(String configText) throws ValueException {
    try {
      Config config = Strings.isNullOrEmpty(configText) ?
        ConfigFactory.parseString(DEFAULT) :
        ConfigFactory.parseString(configText).withFallback(ConfigFactory.parseString(DEFAULT));
      backgroundLayerMax = config.getInt("backgroundLayerMax");
      backgroundLayerMin = config.getInt("backgroundLayerMin");
      bufferAheadSeconds = config.getInt("bufferAheadSeconds");
      bufferBeforeSeconds = config.getInt("bufferBeforeSeconds");
      deltaArcDetailLayersIncoming = config.getInt("deltaArcDetailLayersIncoming");
      deltaArcDetailPlateauRatio = config.getDouble("deltaArcDetailPlateauRatio");
      deltaArcEnabled = config.getBoolean("deltaArcEnabled");
      deltaArcRhythmLayersIncoming = config.getInt("deltaArcRhythmLayersIncoming");
      deltaArcRhythmLayersToPrioritize = config.getString("deltaArcRhythmLayersToPrioritize");
      deltaArcRhythmPlateauRatio = config.getDouble("deltaArcRhythmPlateauRatio");
      densityCeiling = config.getDouble("densityCeiling");
      densityFloor = config.getDouble("densityFloor");
      dubMasterVolumeInstrumentTypeBass = config.getDouble("dubMasterVolumeInstrumentTypeBass");
      dubMasterVolumeInstrumentTypeDrum = config.getDouble("dubMasterVolumeInstrumentTypeDrum");
      dubMasterVolumeInstrumentTypePad = config.getDouble("dubMasterVolumeInstrumentTypePad");
      dubMasterVolumeInstrumentTypePercLoop = config.getDouble("dubMasterVolumeInstrumentTypePercLoop");
      dubMasterVolumeInstrumentTypeStab = config.getDouble("dubMasterVolumeInstrumentTypeStab");
      dubMasterVolumeInstrumentTypeSticky = config.getDouble("dubMasterVolumeInstrumentTypeSticky");
      dubMasterVolumeInstrumentTypeStripe = config.getDouble("dubMasterVolumeInstrumentTypeStripe");
      mainProgramLengthMaxDelta = config.getInt("mainProgramLengthMaxDelta");
      metaSource = config.getString("metaSource");
      metaTitle = config.getString("metaTitle");
      mixerCompressAheadSeconds = config.getDouble("mixerCompressAheadSeconds");
      mixerCompressDecaySeconds = config.getDouble("mixerCompressDecaySeconds");
      mixerCompressRatioMax = config.getDouble("mixerCompressRatioMax");
      mixerCompressRatioMin = config.getDouble("mixerCompressRatioMin");
      mixerCompressToAmplitude = config.getDouble("mixerCompressToAmplitude");
      mixerDspBufferSize = config.getInt("mixerDspBufferSize");
      mixerHighpassThresholdHz = config.getInt("mixerHighpassThresholdHz");
      mixerLowpassThresholdHz = config.getInt("mixerLowpassThresholdHz");
      mixerNormalizationBoostThreshold = config.getDouble("mixerNormalizationBoostThreshold");
      mixerNormalizationCeiling = config.getDouble("mixerNormalizationCeiling");
      outputChannels = config.getInt("outputChannels");
      outputContainer = config.getString("outputContainer");
      outputContentType = config.getString("outputContentType");
      outputEncoding = new AudioFormat.Encoding(config.getString("outputEncoding"));
      outputEncodingQuality = config.getDouble("outputEncodingQuality");
      outputFrameRate = config.getInt("outputFrameRate");
      outputSampleBits = config.getInt("outputSampleBits");
      percLoopLayerMax = config.getInt("percLoopLayerMax");
      percLoopLayerMin = config.getInt("percLoopLayerMin");
      transitionLayerMax = config.getInt("transitionLayerMax");
      transitionLayerMin = config.getInt("transitionLayerMin");
    } catch (ConfigException e) {
      throw new ValueException(e.getMessage());
    }
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public String toString() {
    Map<String, String> config = Maps.newHashMap();
    config.put("backgroundLayerMax", String.valueOf(backgroundLayerMax));
    config.put("backgroundLayerMin", String.valueOf(backgroundLayerMin));
    config.put("bufferAheadSeconds", String.valueOf(bufferAheadSeconds));
    config.put("bufferBeforeSeconds", String.valueOf(bufferBeforeSeconds));
    config.put("deltaArcDetailLayersIncoming", String.valueOf(deltaArcDetailLayersIncoming));
    config.put("deltaArcDetailPlateauRatio", String.valueOf(deltaArcDetailPlateauRatio));
    config.put("deltaArcEnabled", String.valueOf(deltaArcEnabled));
    config.put("deltaArcRhythmLayersIncoming", String.valueOf(deltaArcRhythmLayersIncoming));
    config.put("deltaArcRhythmLayersToPrioritize", String.valueOf(deltaArcRhythmLayersToPrioritize));
    config.put("deltaArcRhythmPlateauRatio", String.valueOf(deltaArcRhythmPlateauRatio));
    config.put("densityCeiling", String.valueOf(densityCeiling));
    config.put("densityFloor", String.valueOf(densityFloor));
    config.put("dubMasterVolumeInstrumentTypeBass", String.valueOf(dubMasterVolumeInstrumentTypeBass));
    config.put("dubMasterVolumeInstrumentTypeDrum", String.valueOf(dubMasterVolumeInstrumentTypeDrum));
    config.put("dubMasterVolumeInstrumentTypePad", String.valueOf(dubMasterVolumeInstrumentTypePad));
    config.put("dubMasterVolumeInstrumentTypePercLoop", String.valueOf(dubMasterVolumeInstrumentTypePercLoop));
    config.put("dubMasterVolumeInstrumentTypeStab", String.valueOf(dubMasterVolumeInstrumentTypeStab));
    config.put("dubMasterVolumeInstrumentTypeSticky", String.valueOf(dubMasterVolumeInstrumentTypeSticky));
    config.put("dubMasterVolumeInstrumentTypeStripe", String.valueOf(dubMasterVolumeInstrumentTypeStripe));
    config.put("mainProgramLengthMaxDelta", String.valueOf(mainProgramLengthMaxDelta));
    config.put("metaSource", Text.orEmptyQuotes(metaSource));
    config.put("metaTitle", Text.orEmptyQuotes(metaTitle));
    config.put("mixerCompressAheadSeconds", String.valueOf(mixerCompressAheadSeconds));
    config.put("mixerCompressDecaySeconds", String.valueOf(mixerCompressDecaySeconds));
    config.put("mixerCompressRatioMax", String.valueOf(mixerCompressRatioMax));
    config.put("mixerCompressRatioMin", String.valueOf(mixerCompressRatioMin));
    config.put("mixerCompressToAmplitude", String.valueOf(mixerCompressToAmplitude));
    config.put("mixerDspBufferSize", String.valueOf(mixerDspBufferSize));
    config.put("mixerHighpassThresholdHz", String.valueOf(mixerHighpassThresholdHz));
    config.put("mixerLowpassThresholdHz", String.valueOf(mixerLowpassThresholdHz));
    config.put("mixerNormalizationBoostThreshold", String.valueOf(mixerNormalizationBoostThreshold));
    config.put("mixerNormalizationCeiling", String.valueOf(mixerNormalizationCeiling));
    config.put("outputChannels", String.valueOf(outputChannels));
    config.put("outputContainer", Text.doubleQuoted(outputContainer));
    config.put("outputContentType", Text.doubleQuoted(outputContentType));
    config.put("outputEncoding", Text.doubleQuoted(outputEncoding.toString()));
    config.put("outputEncodingQuality", String.valueOf(outputEncodingQuality));
    config.put("outputFrameRate", String.valueOf(outputFrameRate));
    config.put("outputSampleBits", String.valueOf(outputSampleBits));
    config.put("percLoopLayerMax", String.valueOf(percLoopLayerMax));
    config.put("percLoopLayerMin", String.valueOf(percLoopLayerMin));
    config.put("transitionLayerMax", String.valueOf(transitionLayerMax));
    config.put("transitionLayerMin", String.valueOf(transitionLayerMin));
    return Text.formatMultiline(config.entrySet().stream()
      .sorted(Map.Entry.comparingByKey())
      .map(pair -> String.format("%s = %s", pair.getKey(), pair.getValue()))
      .toArray());
  }

  /**
   @return background layer min
   */
  public int getBackgroundLayerMin() {
    return backgroundLayerMin;
  }

  /**
   @return background layer max
   */
  public int getBackgroundLayerMax() {
    return backgroundLayerMax;
  }

  /**
   @return the work buffer ahead seconds
   */
  public int getBufferAheadSeconds() {
    return bufferAheadSeconds;
  }

  /**
   @return the work buffer before seconds
   */
  public int getBufferBeforeSeconds() {
    return bufferBeforeSeconds;
  }

  /**
   @return true if choice delta is enabled
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean isDeltaArcEnabled() {
    return deltaArcEnabled;
  }

  /**
   @return the plateau ratio of detail-layer detail arcs
   */
  public double getDeltaArcDetailPlateauRatio() {
    return deltaArcDetailPlateauRatio;
  }

  /**
   @return the number of delta arc detail layers incoming each segment
   */
  public int getDeltaArcDetailLayersIncoming() {
    return deltaArcDetailLayersIncoming;
  }

  /**
   @return the plateau ratio of rhythm-layer detail arcs
   */
  public double getDeltaArcRhythmPlateauRatio() {
    return deltaArcRhythmPlateauRatio;
  }

  /**
   @return the number of delta arc rhythm layers incoming each segment
   */
  public int getDeltaArcRhythmLayersIncoming() {
    return deltaArcRhythmLayersIncoming;
  }

  /**
   @return delta arc rhythm layer prioritization regexp
   */
  public String getDeltaArcRhythmLayersToPrioritize() {
    return deltaArcRhythmLayersToPrioritize;
  }

  /**
   @return the density floor
   */
  public double getDensityFloor() {
    return densityFloor;
  }

  /**
   @return the density ceiling
   */
  public double getDensityCeiling() {
    return densityCeiling;
  }

  /**
   @return ratio of amplitude to dub audio for Bass-type instruments
   */
  public double getDubMasterVolumeInstrumentTypeBass() {
    return dubMasterVolumeInstrumentTypeBass;
  }

  /**
   @return ratio of amplitude to dub audio for Drum-type instruments
   */
  public double getDubMasterVolumeInstrumentTypeDrum() {
    return dubMasterVolumeInstrumentTypeDrum;
  }

  /**
   @return ratio of amplitude to dub audio for Pad-type instruments
   */
  public double getDubMasterVolumeInstrumentTypePad() {
    return dubMasterVolumeInstrumentTypePad;
  }

  /**
   @return ratio of amplitude to dub audio for PercLoop-type instruments
   */
  public double getDubMasterVolumeInstrumentTypePercLoop() {
    return dubMasterVolumeInstrumentTypePercLoop;
  }

  /**
   @return ratio of amplitude to dub audio for Stab-type instruments
   */
  public double getDubMasterVolumeInstrumentTypeStab() {
    return dubMasterVolumeInstrumentTypeStab;
  }

  /**
   @return ratio of amplitude to dub audio for Stick-type instruments
   */
  public double getDubMasterVolumeInstrumentTypeSticky() {
    return dubMasterVolumeInstrumentTypeSticky;
  }

  /**
   @return ratio of amplitude to dub audio for Strip-type instruments
   */
  public double getDubMasterVolumeInstrumentTypeStripe() {
    return dubMasterVolumeInstrumentTypeStripe;
  }

  /**
   @return max length (delta) for a main program to run
   */
  public int getMainProgramLengthMaxDelta() {
    return mainProgramLengthMaxDelta;
  }

  /**
   @return the meta source
   */
  public String getMetaSource() {
    return metaSource;
  }

  /**
   @return the meta title
   */
  public String getMetaTitle() {
    return metaTitle;
  }

  /**
   @return mixer Compress Ahead Seconds
   */
  public double getMixerCompressAheadSeconds() {
    return mixerCompressAheadSeconds;
  }

  /**
   @return mixer Compress Decay Seconds
   */
  public double getMixerCompressDecaySeconds() {
    return mixerCompressDecaySeconds;
  }

  /**
   @return mixer Compress Ratio Max
   */
  public double getMixerCompressRatioMax() {
    return mixerCompressRatioMax;
  }

  /**
   @return mixer Compress Ratio Min
   */
  public double getMixerCompressRatioMin() {
    return mixerCompressRatioMin;
  }

  /**
   @return mixer Compress To Amplitude
   */
  public double getMixerCompressToAmplitude() {
    return mixerCompressToAmplitude;
  }

  /**
   @return mixer Dsp Buffer Size
   */
  public int getMixerDspBufferSize() {
    return mixerDspBufferSize;
  }

  /**
   @return mixer Highpass Threshold Hz
   */
  public double getMixerHighpassThresholdHz() {
    return mixerHighpassThresholdHz;
  }

  /**
   @return mixer Lowpass Threshold Hz
   */
  public double getMixerLowpassThresholdHz() {
    return mixerLowpassThresholdHz;
  }

  /**
   @return mixer Normalization Max
   */
  public double getMixerNormalizationCeiling() {
    return mixerNormalizationCeiling;
  }

  /**
   @return mixer limit of how much to boost during normalization
   */
  public double getMixerNormalizationBoostThreshold() {
    return mixerNormalizationBoostThreshold;
  }

  /**
   @return # of Output Channels
   */
  public int getOutputChannels() {
    return outputChannels;
  }

  /**
   @return Output Container
   */
  public String getOutputContainer() {
    return outputContainer;
  }

  /**
   @return Output content-type
   */
  public String getOutputContentType() {
    return outputContentType;
  }

  /**
   @return Output Encoding
   */
  public AudioFormat.Encoding getOutputEncoding() {
    return outputEncoding;
  }

  /**
   @return Output Encoding Quality (ratio from 0 to 1)
   */
  public double getOutputEncodingQuality() {
    return outputEncodingQuality;
  }

  /**
   @return Output Frame Rate (Hz)
   */
  public int getOutputFrameRate() {
    return outputFrameRate;
  }

  /**
   @return the output frame size (bytes per frame including all channels)
   */
  public int getOutputFrameSize() {
    return outputSampleBits * outputChannels / BITS_PER_BYTE;
  }

  /**
   @return Output Sample Bits
   */
  public int getOutputSampleBits() {
    return outputSampleBits;
  }

  /**
   @return the maximum # of layers of percussive loops
   */
  public int getPercLoopLayerMax() {
    return percLoopLayerMax;
  }

  /**
   @return the minimum # of layers of percussive loops
   */
  public int getPercLoopLayerMin() {
    return percLoopLayerMin;
  }

  /**
   @return transition layer min
   */
  public int getTransitionLayerMin() {
    return transitionLayerMin;
  }

  /**
   @return transition layer max
   */
  public int getTransitionLayerMax() {
    return transitionLayerMax;
  }

}
