package io.xj.lib.entity.common;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import io.xj.api.Template;
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
  private static final String KEY_PREFIX = "template.";

  private final int outputChannels;
  private final String KEY_OUTPUT_CHANNELS = "outputChannels";

  private final String outputContainer;
  private final String KEY_OUTPUT_CONTAINER = "outputContainer";

  private final AudioFormat.Encoding outputEncoding;
  private final String KEY_OUTPUT_ENCODING = "outputEncoding";

  private final double outputEncodingQuality;
  private final String KEY_OUTPUT_ENCODING_QUALITY = "outputEncodingQuality";

  private final int outputFrameRate;
  private final String KEY_OUTPUT_FRAME_RATE = "outputFrameRate";

  private final int outputSampleBits;
  private final String KEY_OUTPUT_SAMPLE_BITS = "outputSampleBits";

  private final double mixerCompressAheadSeconds;
  private final String KEY_MIXER_COMPRESS_AHEAD_SECONDS = "mixerCompressAheadSeconds";

  private final double mixerCompressDecaySeconds;
  private final String KEY_MIXER_COMPRESS_DECAY_SECONDS = "mixerCompressDecaySeconds";

  private final double mixerCompressRatioMax;
  private final String KEY_MIXER_COMPRESS_RATIO_MAX = "mixerCompressRatioMax";

  private final double mixerCompressRatioMin;
  private final String KEY_MIXER_COMPRESS_RATIO_MIN = "mixerCompressRatioMin";

  private final double mixerCompressToAmplitude;
  private final String KEY_MIXER_COMPRESS_TO_AMPLITUDE = "mixerCompressToAmplitude";

  private final int mixerDspBufferSize;
  private final String KEY_MIXER_DSP_BUFFER_SIZE = "mixerDspBufferSize";

  private final double mixerHighpassThresholdHz;
  private final String KEY_MIXER_HIGHPASS_THRESHOLD_HZ = "mixerHighpassThresholdHz";

  private final double mixerLowpassThresholdHz;
  private final String KEY_MIXER_LOWPASS_THRESHOLD_HZ = "mixerLowpassThresholdHz";

  private final double mixerNormalizationCeiling;
  private final String KEY_MIXER_NORMALIZATION_MAX = "mixerNormalizationCeiling";

  private final double mixerNormalizationBoostThreshold;
  private final String KEY_MIXER_NORMALIZATION_BOOST_THRESHOLD = "mixerNormalizationBoostThreshold";

  private final int mainProgramLengthMaxDelta;
  private final String KEY_MAIN_PROGRAM_LENGTH_MAX_DELTA = "mainProgramLengthMaxDelta";

  private final boolean choiceDeltaEnabled;
  private final String KEY_CHOICE_DELTA_ENABLED = "choiceDeltaEnabled";

  private final double dubMasterVolumeInstrumentTypePercussive;
  private final String KEY_DUB_MASTER_VOLUME_INSTRUMENT_TYPE_PERCUSSIVE = "dubMasterVolumeInstrumentTypePercussive";

  private final double dubMasterVolumeInstrumentTypeBass;
  private final String KEY_DUB_MASTER_VOLUME_INSTRUMENT_TYPE_BASS = "dubMasterVolumeInstrumentTypeBass";

  private final double dubMasterVolumeInstrumentTypePad;
  private final String KEY_DUB_MASTER_VOLUME_INSTRUMENT_TYPE_PAD = "dubMasterVolumeInstrumentTypePad";

  private final double dubMasterVolumeInstrumentTypeSticky;
  private final String KEY_DUB_MASTER_VOLUME_INSTRUMENT_TYPE_STICKY = "dubMasterVolumeInstrumentTypeSticky";

  private final double dubMasterVolumeInstrumentTypeStripe;
  private final String KEY_DUB_MASTER_VOLUME_INSTRUMENT_TYPE_STRIPE = "dubMasterVolumeInstrumentTypeStripe";

  private final double dubMasterVolumeInstrumentTypeStab;
  private final String KEY_DUB_MASTER_VOLUME_INSTRUMENT_TYPE_STAB = "dubMasterVolumeInstrumentTypeStab";

  /**
   Get a template config from only the default config

   @param config from which to get template config
   @throws ValueException on failure
   */
  public TemplateConfig(Config config) throws ValueException {
    this("", config);
  }

  /**
   Instantiate a Template configuration from a string of typesafe config.
   Said string will be embedded in a `template{...}` block such that
   provided simple Key=Value pairs will be understood as members of `template`
   e.g. will override values from the `template{...}` block of the top-level **default.conf**

   @param template to get Config from
   */
  public TemplateConfig(Template template, Config defaultConfig) throws ValueException {
   this(template.getConfig(), defaultConfig);
  }

  /**
   Instantiate a Template configuration from a string of typesafe config.
   Said string will be embedded in a `template{...}` block such that
   provided simple Key=Value pairs will be understood as members of `template`
   e.g. will override values from the `template{...}` block of the top-level **default.conf**
   */
  public TemplateConfig(String configText, Config defaultConfig) throws ValueException {
    try {
      Config config = Strings.isNullOrEmpty(configText) ?
        defaultConfig :
        ConfigFactory.parseString(String.format("template {\n%s\n}", configText))
          .withFallback(defaultConfig);
      choiceDeltaEnabled = config.getBoolean(chPfx(KEY_CHOICE_DELTA_ENABLED));
      dubMasterVolumeInstrumentTypeBass = config.getDouble(chPfx(KEY_DUB_MASTER_VOLUME_INSTRUMENT_TYPE_BASS));
      dubMasterVolumeInstrumentTypePad = config.getDouble(chPfx(KEY_DUB_MASTER_VOLUME_INSTRUMENT_TYPE_PAD));
      dubMasterVolumeInstrumentTypePercussive = config.getDouble(chPfx(KEY_DUB_MASTER_VOLUME_INSTRUMENT_TYPE_PERCUSSIVE));
      dubMasterVolumeInstrumentTypeStab = config.getDouble(chPfx(KEY_DUB_MASTER_VOLUME_INSTRUMENT_TYPE_STAB));
      dubMasterVolumeInstrumentTypeSticky = config.getDouble(chPfx(KEY_DUB_MASTER_VOLUME_INSTRUMENT_TYPE_STICKY));
      dubMasterVolumeInstrumentTypeStripe = config.getDouble(chPfx(KEY_DUB_MASTER_VOLUME_INSTRUMENT_TYPE_STRIPE));
      mainProgramLengthMaxDelta = config.getInt(chPfx(KEY_MAIN_PROGRAM_LENGTH_MAX_DELTA));
      mixerCompressAheadSeconds = config.getDouble(chPfx(KEY_MIXER_COMPRESS_AHEAD_SECONDS));
      mixerCompressDecaySeconds = config.getDouble(chPfx(KEY_MIXER_COMPRESS_DECAY_SECONDS));
      mixerCompressRatioMax = config.getDouble(chPfx(KEY_MIXER_COMPRESS_RATIO_MAX));
      mixerCompressRatioMin = config.getDouble(chPfx(KEY_MIXER_COMPRESS_RATIO_MIN));
      mixerCompressToAmplitude = config.getDouble(chPfx(KEY_MIXER_COMPRESS_TO_AMPLITUDE));
      mixerDspBufferSize = config.getInt(chPfx(KEY_MIXER_DSP_BUFFER_SIZE));
      mixerHighpassThresholdHz = config.getDouble(chPfx(KEY_MIXER_HIGHPASS_THRESHOLD_HZ));
      mixerLowpassThresholdHz = config.getDouble(chPfx(KEY_MIXER_LOWPASS_THRESHOLD_HZ));
      mixerNormalizationCeiling = config.getDouble(chPfx(KEY_MIXER_NORMALIZATION_MAX));
      mixerNormalizationBoostThreshold = config.getDouble(chPfx(KEY_MIXER_NORMALIZATION_BOOST_THRESHOLD));
      outputChannels = config.getInt(chPfx(KEY_OUTPUT_CHANNELS));
      outputContainer = config.getString(chPfx(KEY_OUTPUT_CONTAINER));
      outputEncoding = new AudioFormat.Encoding(config.getString(chPfx(KEY_OUTPUT_ENCODING)));
      outputEncodingQuality = config.getDouble(chPfx(KEY_OUTPUT_ENCODING_QUALITY));
      outputFrameRate = config.getInt(chPfx(KEY_OUTPUT_FRAME_RATE));
      outputSampleBits = config.getInt(chPfx(KEY_OUTPUT_SAMPLE_BITS));

    } catch (ConfigException e) {
      throw new ValueException(e.getMessage());
    }
  }

  /**
   Template-prefixed version of a key

   @param key to prefix
   @return template-prefixed key
   */
  private String chPfx(String key) {
    return String.format("%s%s", KEY_PREFIX, key);
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public String toString() {
    Map<String, String> config = Maps.newHashMap();
    config.put(KEY_CHOICE_DELTA_ENABLED, String.valueOf(choiceDeltaEnabled));
    config.put(KEY_DUB_MASTER_VOLUME_INSTRUMENT_TYPE_BASS, String.valueOf(dubMasterVolumeInstrumentTypeBass));
    config.put(KEY_DUB_MASTER_VOLUME_INSTRUMENT_TYPE_PAD, String.valueOf(dubMasterVolumeInstrumentTypePad));
    config.put(KEY_DUB_MASTER_VOLUME_INSTRUMENT_TYPE_PERCUSSIVE, String.valueOf(dubMasterVolumeInstrumentTypePercussive));
    config.put(KEY_DUB_MASTER_VOLUME_INSTRUMENT_TYPE_STAB, String.valueOf(dubMasterVolumeInstrumentTypeStab));
    config.put(KEY_DUB_MASTER_VOLUME_INSTRUMENT_TYPE_STICKY, String.valueOf(dubMasterVolumeInstrumentTypeSticky));
    config.put(KEY_DUB_MASTER_VOLUME_INSTRUMENT_TYPE_STRIPE, String.valueOf(dubMasterVolumeInstrumentTypeStripe));
    config.put(KEY_MAIN_PROGRAM_LENGTH_MAX_DELTA, String.valueOf(mainProgramLengthMaxDelta));
    config.put(KEY_MIXER_COMPRESS_AHEAD_SECONDS, String.valueOf(mixerCompressAheadSeconds));
    config.put(KEY_MIXER_COMPRESS_DECAY_SECONDS, String.valueOf(mixerCompressDecaySeconds));
    config.put(KEY_MIXER_COMPRESS_RATIO_MAX, String.valueOf(mixerCompressRatioMax));
    config.put(KEY_MIXER_COMPRESS_RATIO_MIN, String.valueOf(mixerCompressRatioMin));
    config.put(KEY_MIXER_COMPRESS_TO_AMPLITUDE, String.valueOf(mixerCompressToAmplitude));
    config.put(KEY_MIXER_DSP_BUFFER_SIZE, String.valueOf(mixerDspBufferSize));
    config.put(KEY_MIXER_HIGHPASS_THRESHOLD_HZ, String.valueOf(mixerHighpassThresholdHz));
    config.put(KEY_MIXER_LOWPASS_THRESHOLD_HZ, String.valueOf(mixerLowpassThresholdHz));
    config.put(KEY_MIXER_NORMALIZATION_MAX, String.valueOf(mixerNormalizationCeiling));
    config.put(KEY_MIXER_NORMALIZATION_BOOST_THRESHOLD, String.valueOf(mixerNormalizationBoostThreshold));
    config.put(KEY_OUTPUT_CHANNELS, String.valueOf(outputChannels));
    config.put(KEY_OUTPUT_CONTAINER, Text.doubleQuoted(outputContainer));
    config.put(KEY_OUTPUT_ENCODING, Text.doubleQuoted(outputEncoding.toString()));
    config.put(KEY_OUTPUT_ENCODING_QUALITY, String.valueOf(outputEncodingQuality));
    config.put(KEY_OUTPUT_FRAME_RATE, String.valueOf(outputFrameRate));
    config.put(KEY_OUTPUT_SAMPLE_BITS, String.valueOf(outputSampleBits));
    return Text.formatMultiline(config.entrySet().stream()
      .sorted(Map.Entry.comparingByKey())
      .map(pair -> String.format("%s = %s", pair.getKey(), pair.getValue()))
      .toArray());
  }

  /**
   @return Output Sample Bits
   */
  public int getOutputSampleBits() {
    return outputSampleBits;
  }

  /**
   @return Output Frame Rate (Hz)
   */
  public int getOutputFrameRate() {
    return outputFrameRate;
  }

  /**
   @return Output Encoding Quality (ratio from 0 to 1)
   */
  public double getOutputEncodingQuality() {
    return outputEncodingQuality;
  }

  /**
   @return Output Encoding
   */
  public AudioFormat.Encoding getOutputEncoding() {
    return outputEncoding;
  }

  /**
   @return Output Container
   */
  public String getOutputContainer() {
    return outputContainer;
  }

  /**
   @return # of Output Channels
   */
  public int getOutputChannels() {
    return outputChannels;
  }

  /**
   @return ratio of amplitude to dub audio for Percussive-type instruments
   */
  public double getDubMasterVolumeInstrumentTypePercussive() {
    return dubMasterVolumeInstrumentTypePercussive;
  }

  /**
   @return ratio of amplitude to dub audio for Bass-type instruments
   */
  public double getDubMasterVolumeInstrumentTypeBass() {
    return dubMasterVolumeInstrumentTypeBass;
  }

  /**
   @return ratio of amplitude to dub audio for Pad-type instruments
   */
  public double getDubMasterVolumeInstrumentTypePad() {
    return dubMasterVolumeInstrumentTypePad;
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
   @return ratio of amplitude to dub audio for Stab-type instruments
   */
  public double getDubMasterVolumeInstrumentTypeStab() {
    return dubMasterVolumeInstrumentTypeStab;
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
   @return max length (delta) for a main program to run
   */
  public int getMainProgramLengthMaxDelta() {
    return mainProgramLengthMaxDelta;
  }

  /**
   @return true if choice delta is enabled
   */
  public boolean isChoiceDeltaEnabled() {
    return choiceDeltaEnabled;
  }
}
