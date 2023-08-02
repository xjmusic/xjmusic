// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import io.xj.hub.enums.InstrumentType;
import io.xj.hub.tables.pojos.Template;
import io.xj.lib.meme.MemeTaxonomy;
import io.xj.lib.util.StringUtils;
import io.xj.lib.util.ValueException;

import javax.sound.sampled.AudioFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Parse a TypeSafe `config` value for a Template's configuration, overriding values from top-level default.conf--
 * e.g.
 * if the `config` value contains only `previewLengthMaxHours = 8`
 * <p>
 * Artist saves Template config, validate & combine with defaults. https://www.pivotaltracker.com/story/show/177355683
 */
public class TemplateConfig {
  public static final String DEFAULT = """
    backgroundLayerMax = 3
    backgroundLayerMin = 0
    bufferAheadSeconds = 180
    bufferBeforeSeconds = 5
    choiceMuteProbabilityInstrumentTypeBass = 0.0
    choiceMuteProbabilityInstrumentTypeDrum = 0.0
    choiceMuteProbabilityInstrumentTypeHook = 0.0
    choiceMuteProbabilityInstrumentTypeNoise = 0.0
    choiceMuteProbabilityInstrumentTypePad = 0.0
    choiceMuteProbabilityInstrumentTypePercussion = 0.0
    choiceMuteProbabilityInstrumentTypeStab = 0.0
    choiceMuteProbabilityInstrumentTypeSticky = 0.0
    choiceMuteProbabilityInstrumentTypeStripe = 0.0
    choiceMuteProbabilityInstrumentTypeSweep = 0.0
    deltaArcBeatLayersIncoming = 1
    deltaArcBeatLayersToPrioritize = kick
    deltaArcDetailLayersIncoming = 1
    deltaArcEnabled = true
    densityCeiling = 0.9
    densityFloor = 0.1
    detailLayerOrder = ["Bass","Stripe","Pad","Sticky","Stab"]
    dubMasterVolumeInstrumentTypeBass = 1.0
    dubMasterVolumeInstrumentTypeDrum = 1.0
    dubMasterVolumeInstrumentTypeHook = 1.0
    dubMasterVolumeInstrumentTypeNoise = 1.0
    dubMasterVolumeInstrumentTypePad = 1.0
    dubMasterVolumeInstrumentTypePercussion = 1.0
    dubMasterVolumeInstrumentTypeStab = 1.0
    dubMasterVolumeInstrumentTypeSticky = 1.0
    dubMasterVolumeInstrumentTypeStripe = 1.0
    dubMasterVolumeInstrumentTypeSweep = 1.0
    eventNamesLarge = ["LARGE","BIG","HIGH","PRIMARY"]
    eventNamesMedium = ["MEDIUM","REGULAR","MIDDLE","SECONDARY"]
    eventNamesSmall = ["SMALL","LITTLE","LOW"]
    instrumentTypesForAudioLengthFinalization = ["Bass","Pad","Stab","Sticky","Stripe"]
    instrumentTypesForInversionSeeking = ["Pad","Stab","Sticky","Stripe"]
    mainProgramLengthMaxDelta = 280
    memeTaxonomy = [
        {
          "memes":["RED","GREEN","BLUE"],
          "name":"COLOR"
        },
        {
          "memes":["WINTER","SPRING","SUMMER","FALL"],
          "name":"SEASON"
        }
      ]
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
    outputEncoding = "PCM_DOUBLE"
    outputFrameRate = 48000
    outputSampleBits = 16
    percLoopLayerMax = 8
    percLoopLayerMin = 0
    stickyBunEnabled = true
    transitionLayerMax = 3
    transitionLayerMin = 0
    vmResourceLimitCpu = 2.0
    vmResourceLimitMemoryGb = 6.0
    """;
  final AudioFormat.Encoding outputEncoding;
  final List<InstrumentType> detailLayerOrder;
  final List<InstrumentType> instrumentTypesForAudioLengthFinalization;
  final List<InstrumentType> instrumentTypesForInversionSeeking;
  final List<String> eventNamesLarge;
  final List<String> eventNamesMedium;
  final List<String> eventNamesSmall;
  final MemeTaxonomy memeTaxonomy;
  final String deltaArcBeatLayersToPrioritize;
  final boolean deltaArcEnabled;
  final boolean stickyBunEnabled;
  final double choiceMuteProbabilityInstrumentTypeBass;
  final double choiceMuteProbabilityInstrumentTypeDrum;
  final double choiceMuteProbabilityInstrumentTypeHook;
  final double choiceMuteProbabilityInstrumentTypeNoise;
  final double choiceMuteProbabilityInstrumentTypePad;
  final double choiceMuteProbabilityInstrumentTypePercussion;
  final double choiceMuteProbabilityInstrumentTypeStab;
  final double choiceMuteProbabilityInstrumentTypeSticky;
  final double choiceMuteProbabilityInstrumentTypeStripe;
  final double choiceMuteProbabilityInstrumentTypeSweep;
  final double densityCeiling;
  final double densityFloor;
  final double dubMasterVolumeInstrumentTypeBass;
  final double dubMasterVolumeInstrumentTypeDrum;
  final double dubMasterVolumeInstrumentTypeHook;
  final double dubMasterVolumeInstrumentTypeNoise;
  final double dubMasterVolumeInstrumentTypePad;
  final double dubMasterVolumeInstrumentTypePercussion;
  final double dubMasterVolumeInstrumentTypeStab;
  final double dubMasterVolumeInstrumentTypeSticky;
  final double dubMasterVolumeInstrumentTypeStripe;
  final double dubMasterVolumeInstrumentTypeSweep;
  final double mixerCompressAheadSeconds;
  final double mixerCompressDecaySeconds;
  final double mixerCompressRatioMax;
  final double mixerCompressRatioMin;
  final double mixerCompressToAmplitude;
  final double mixerNormalizationBoostThreshold;
  final double mixerNormalizationCeiling;
  final double vmResourceLimitCpu;
  final double vmResourceLimitMemoryGb;
  final int backgroundLayerMax;
  final int backgroundLayerMin;
  final int bufferAheadSeconds;
  final int bufferBeforeSeconds;
  final int deltaArcBeatLayersIncoming;
  final int deltaArcDetailLayersIncoming;
  final int mainProgramLengthMaxDelta;
  final int mixerDspBufferSize;
  final int mixerHighpassThresholdHz;
  final int mixerLowpassThresholdHz;
  final int outputChannels;
  final int outputFrameRate;
  final int outputSampleBits;
  final int percLoopLayerMax;
  final int percLoopLayerMin;
  final int transitionLayerMax;
  final int transitionLayerMin;

  /**
   * Get a template config from only the default config
   *
   * @throws ValueException on failure
   */
  public TemplateConfig() throws ValueException {
    this("");
  }

  /**
   * Instantiate a Template configuration from a string of typesafe config.
   * Said string will be embedded in a `template{...}` block such that
   * provided simple Key=Value pairs will be understood as members of `template`
   * e.g. will override values from the `template{...}` block of the top-level **default.conf**
   *
   * @param template to get config from
   */
  public TemplateConfig(Template template) throws ValueException {
    this(template.getConfig());
  }

  /**
   * Instantiate a Template configuration from a string of typesafe config.
   * Said string will be embedded in a `template{...}` block such that
   * provided simple Key=Value pairs will be understood as members of `template`
   * e.g. will override values from the `template{...}` block of the top-level **default.conf**
   */
  public TemplateConfig(String configText) throws ValueException {
    try {
      Config config = StringUtils.isNullOrEmpty(configText) ? ConfigFactory.parseString(DEFAULT) : ConfigFactory.parseString(configText).withFallback(ConfigFactory.parseString(DEFAULT));
      backgroundLayerMax = config.getInt("backgroundLayerMax");
      backgroundLayerMin = config.getInt("backgroundLayerMin");
      bufferAheadSeconds = config.getInt("bufferAheadSeconds");
      bufferBeforeSeconds = config.getInt("bufferBeforeSeconds");
      choiceMuteProbabilityInstrumentTypeBass = config.getDouble("choiceMuteProbabilityInstrumentTypeBass");
      choiceMuteProbabilityInstrumentTypeDrum = config.getDouble("choiceMuteProbabilityInstrumentTypeDrum");
      choiceMuteProbabilityInstrumentTypeHook = config.getDouble("choiceMuteProbabilityInstrumentTypeHook");
      choiceMuteProbabilityInstrumentTypeNoise = config.getDouble("choiceMuteProbabilityInstrumentTypeNoise");
      choiceMuteProbabilityInstrumentTypePad = config.getDouble("choiceMuteProbabilityInstrumentTypePad");
      choiceMuteProbabilityInstrumentTypePercussion = config.getDouble("choiceMuteProbabilityInstrumentTypePercussion");
      choiceMuteProbabilityInstrumentTypeStab = config.getDouble("choiceMuteProbabilityInstrumentTypeStab");
      choiceMuteProbabilityInstrumentTypeSticky = config.getDouble("choiceMuteProbabilityInstrumentTypeSticky");
      choiceMuteProbabilityInstrumentTypeStripe = config.getDouble("choiceMuteProbabilityInstrumentTypeStripe");
      choiceMuteProbabilityInstrumentTypeSweep = config.getDouble("choiceMuteProbabilityInstrumentTypeSweep");
      deltaArcBeatLayersIncoming = config.getInt("deltaArcBeatLayersIncoming");
      deltaArcBeatLayersToPrioritize = config.getString("deltaArcBeatLayersToPrioritize");
      deltaArcDetailLayersIncoming = config.getInt("deltaArcDetailLayersIncoming");
      deltaArcEnabled = config.getBoolean("deltaArcEnabled");
      densityCeiling = config.getDouble("densityCeiling");
      densityFloor = config.getDouble("densityFloor");
      detailLayerOrder = requireAtLeastOne("detailLayerOrder", config.getStringList("detailLayerOrder").stream().map(InstrumentType::valueOf).toList());
      dubMasterVolumeInstrumentTypeBass = config.getDouble("dubMasterVolumeInstrumentTypeBass");
      dubMasterVolumeInstrumentTypeDrum = config.getDouble("dubMasterVolumeInstrumentTypeDrum");
      dubMasterVolumeInstrumentTypeHook = config.getDouble("dubMasterVolumeInstrumentTypeHook");
      dubMasterVolumeInstrumentTypeNoise = config.getDouble("dubMasterVolumeInstrumentTypeNoise");
      dubMasterVolumeInstrumentTypePad = config.getDouble("dubMasterVolumeInstrumentTypePad");
      dubMasterVolumeInstrumentTypePercussion = config.getDouble("dubMasterVolumeInstrumentTypePercussion");
      dubMasterVolumeInstrumentTypeStab = config.getDouble("dubMasterVolumeInstrumentTypeStab");
      dubMasterVolumeInstrumentTypeSticky = config.getDouble("dubMasterVolumeInstrumentTypeSticky");
      dubMasterVolumeInstrumentTypeStripe = config.getDouble("dubMasterVolumeInstrumentTypeStripe");
      dubMasterVolumeInstrumentTypeSweep = config.getDouble("dubMasterVolumeInstrumentTypeSweep");
      eventNamesLarge = requireAtLeastOne("eventNamesLarge", config.getStringList("eventNamesLarge").stream().map(StringUtils::toMeme).toList());
      eventNamesMedium = requireAtLeastOne("eventNamesMedium", config.getStringList("eventNamesMedium").stream().map(StringUtils::toMeme).toList());
      eventNamesSmall = requireAtLeastOne("eventNamesSmall", config.getStringList("eventNamesSmall").stream().map(StringUtils::toMeme).toList());
      instrumentTypesForAudioLengthFinalization = requireAtLeastOne("instrumentTypesForAudioLengthFinalization", config.getStringList("instrumentTypesForAudioLengthFinalization").stream().map(InstrumentType::valueOf).toList());
      instrumentTypesForInversionSeeking = requireAtLeastOne("instrumentTypesForInversionSeeking", config.getStringList("instrumentTypesForInversionSeeking").stream().map(InstrumentType::valueOf).toList());
      mainProgramLengthMaxDelta = config.getInt("mainProgramLengthMaxDelta");
      memeTaxonomy = MemeTaxonomy.fromList(config.getList("memeTaxonomy").stream().map(ConfigValue::unwrapped).toList());
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
      outputEncoding = new AudioFormat.Encoding(config.getString("outputEncoding"));
      outputFrameRate = config.getInt("outputFrameRate");
      outputSampleBits = config.getInt("outputSampleBits");
      percLoopLayerMax = config.getInt("percLoopLayerMax");
      percLoopLayerMin = config.getInt("percLoopLayerMin");
      stickyBunEnabled = config.getBoolean("stickyBunEnabled");
      transitionLayerMax = config.getInt("transitionLayerMax");
      transitionLayerMin = config.getInt("transitionLayerMin");
      vmResourceLimitCpu = Math.floor(config.getDouble("vmResourceLimitCpu"));
      vmResourceLimitMemoryGb = config.getDouble("vmResourceLimitMemoryGb");
    } catch (ConfigException e) {
      throw new ValueException(e.getMessage());
    }
  }

  <N> List<N> requireAtLeastOne(String description, List<N> values) throws ValueException {
    if (values.isEmpty())
      throw new ValueException(String.format("Template Config requires non-empty list for %s", description));
    return values;
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public String toString() {
    Map<String, String> config = new HashMap<>();
    config.put("backgroundLayerMax", String.valueOf(backgroundLayerMax));
    config.put("backgroundLayerMin", String.valueOf(backgroundLayerMin));
    config.put("bufferAheadSeconds", String.valueOf(bufferAheadSeconds));
    config.put("bufferBeforeSeconds", String.valueOf(bufferBeforeSeconds));
    config.put("choiceMuteProbabilityInstrumentTypeBass", String.valueOf(choiceMuteProbabilityInstrumentTypeBass));
    config.put("choiceMuteProbabilityInstrumentTypeDrum", String.valueOf(choiceMuteProbabilityInstrumentTypeDrum));
    config.put("choiceMuteProbabilityInstrumentTypeHook", String.valueOf(choiceMuteProbabilityInstrumentTypeHook));
    config.put("choiceMuteProbabilityInstrumentTypeNoise", String.valueOf(choiceMuteProbabilityInstrumentTypeNoise));
    config.put("choiceMuteProbabilityInstrumentTypePad", String.valueOf(choiceMuteProbabilityInstrumentTypePad));
    config.put("choiceMuteProbabilityInstrumentTypePercussion", String.valueOf(choiceMuteProbabilityInstrumentTypePercussion));
    config.put("choiceMuteProbabilityInstrumentTypeStab", String.valueOf(choiceMuteProbabilityInstrumentTypeStab));
    config.put("choiceMuteProbabilityInstrumentTypeSticky", String.valueOf(choiceMuteProbabilityInstrumentTypeSticky));
    config.put("choiceMuteProbabilityInstrumentTypeStripe", String.valueOf(choiceMuteProbabilityInstrumentTypeStripe));
    config.put("choiceMuteProbabilityInstrumentTypeSweep", String.valueOf(choiceMuteProbabilityInstrumentTypeSweep));
    config.put("deltaArcBeatLayersIncoming", String.valueOf(deltaArcBeatLayersIncoming));
    config.put("deltaArcBeatLayersToPrioritize", String.valueOf(deltaArcBeatLayersToPrioritize));
    config.put("deltaArcDetailLayersIncoming", String.valueOf(deltaArcDetailLayersIncoming));
    config.put("deltaArcEnabled", String.valueOf(deltaArcEnabled));
    config.put("densityCeiling", String.valueOf(densityCeiling));
    config.put("densityFloor", String.valueOf(densityFloor));
    config.put("detailLayerOrder", formatTypesafeQuoted(detailLayerOrder));
    config.put("dubMasterVolumeInstrumentTypeBass", String.valueOf(dubMasterVolumeInstrumentTypeBass));
    config.put("dubMasterVolumeInstrumentTypeDrum", String.valueOf(dubMasterVolumeInstrumentTypeDrum));
    config.put("dubMasterVolumeInstrumentTypeHook", String.valueOf(dubMasterVolumeInstrumentTypeHook));
    config.put("dubMasterVolumeInstrumentTypeNoise", String.valueOf(dubMasterVolumeInstrumentTypeNoise));
    config.put("dubMasterVolumeInstrumentTypePad", String.valueOf(dubMasterVolumeInstrumentTypePad));
    config.put("dubMasterVolumeInstrumentTypePercussion", String.valueOf(dubMasterVolumeInstrumentTypePercussion));
    config.put("dubMasterVolumeInstrumentTypeStab", String.valueOf(dubMasterVolumeInstrumentTypeStab));
    config.put("dubMasterVolumeInstrumentTypeSticky", String.valueOf(dubMasterVolumeInstrumentTypeSticky));
    config.put("dubMasterVolumeInstrumentTypeStripe", String.valueOf(dubMasterVolumeInstrumentTypeStripe));
    config.put("dubMasterVolumeInstrumentTypeSweep", String.valueOf(dubMasterVolumeInstrumentTypeSweep));
    config.put("eventNamesLarge", formatTypesafeQuoted(eventNamesLarge));
    config.put("eventNamesMedium", formatTypesafeQuoted(eventNamesMedium));
    config.put("eventNamesSmall", formatTypesafeQuoted(eventNamesSmall));
    config.put("instrumentTypesForAudioLengthFinalization", formatTypesafeQuoted(instrumentTypesForAudioLengthFinalization));
    config.put("instrumentTypesForInversionSeeking", formatTypesafeQuoted(instrumentTypesForInversionSeeking));
    config.put("mainProgramLengthMaxDelta", String.valueOf(mainProgramLengthMaxDelta));
    config.put("memeTaxonomy", computeStringValueMemeTaxonomy());
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
    config.put("outputEncoding", StringUtils.doubleQuoted(outputEncoding.toString()));
    config.put("outputFrameRate", String.valueOf(outputFrameRate));
    config.put("outputSampleBits", String.valueOf(outputSampleBits));
    config.put("percLoopLayerMax", String.valueOf(percLoopLayerMax));
    config.put("percLoopLayerMin", String.valueOf(percLoopLayerMin));
    config.put("stickyBunEnabled", String.valueOf(stickyBunEnabled));
    config.put("transitionLayerMax", String.valueOf(transitionLayerMax));
    config.put("transitionLayerMin", String.valueOf(transitionLayerMin));
    config.put("vmResourceLimitCpu", String.valueOf(vmResourceLimitCpu));
    config.put("vmResourceLimitMemoryGb", String.valueOf(vmResourceLimitMemoryGb));
    return StringUtils.formatMultiline(config.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(pair -> String.format("%s = %s", pair.getKey(), pair.getValue())).toArray());
  }

  String computeStringValueMemeTaxonomy() {
    return String.format("[%s\n  ]", memeTaxonomy.getCategories().stream().map(category -> String.format("\n    {\n      \"memes\":[%s],\n      \"name\":%s\n    }", category.getMemes().stream().map(StringUtils::doubleQuoted).collect(Collectors.joining(",")), StringUtils.doubleQuoted(category.getName()))).collect(Collectors.joining(",")));
  }

  /**
   * Convert a list of objects in a quoted list of strings in a set of brackets, for inclusion in a typesafe config
   *
   * @param values to format
   * @return typesafe array of quoted values
   */
  <N> String formatTypesafeQuoted(List<N> values) {
    return String.format("[%s]", values.stream().map(N::toString).map(StringUtils::doubleQuoted).collect(Collectors.joining(",")));
  }

  /**
   * @return background layer min
   */
  public int getBackgroundLayerMin() {
    return backgroundLayerMin;
  }

  /**
   * @return background layer max
   */
  public int getBackgroundLayerMax() {
    return backgroundLayerMax;
  }

  /**
   * @return the work buffer ahead seconds
   */
  public int getBufferAheadSeconds() {
    return bufferAheadSeconds;
  }

  /**
   * @return the work buffer before seconds
   */
  public int getBufferBeforeSeconds() {
    return bufferBeforeSeconds;
  }

  /**
   * @return true if choice delta is enabled
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean isDeltaArcEnabled() {
    return deltaArcEnabled;
  }

  /**
   * @return the number of delta arc detail layers incoming each segment
   */
  public int getDeltaArcDetailLayersIncoming() {
    return deltaArcDetailLayersIncoming;
  }

  /**
   * @return the number of delta arc beat layers incoming each segment
   */
  public int getDeltaArcBeatLayersIncoming() {
    return deltaArcBeatLayersIncoming;
  }

  /**
   * @return delta arc beat layer prioritization regexp
   */
  public String getDeltaArcBeatLayersToPrioritize() {
    return deltaArcBeatLayersToPrioritize;
  }

  /**
   * @return the density floor
   */
  public double getDensityFloor() {
    return densityFloor;
  }

  /**
   * @return the density ceiling
   */
  public double getDensityCeiling() {
    return densityCeiling;
  }

  /**
   * @return instrument layers in the intended order of craft
   */
  public List<InstrumentType> getDetailLayerOrder() {
    return detailLayerOrder;
  }

  /**
   * @return probability of a given type of instrument choice being muted for any given segment
   */
  @SuppressWarnings("DuplicatedCode")
  public double getChoiceMuteProbability(InstrumentType instrumentType) {
    return switch (instrumentType) {
      case Bass -> choiceMuteProbabilityInstrumentTypeBass;
      case Drum -> choiceMuteProbabilityInstrumentTypeDrum;
      case Hook -> choiceMuteProbabilityInstrumentTypeHook;
      case Noise -> choiceMuteProbabilityInstrumentTypeNoise;
      case Pad -> choiceMuteProbabilityInstrumentTypePad;
      case Percussion -> choiceMuteProbabilityInstrumentTypePercussion;
      case Stab -> choiceMuteProbabilityInstrumentTypeStab;
      case Sticky -> choiceMuteProbabilityInstrumentTypeSticky;
      case Stripe -> choiceMuteProbabilityInstrumentTypeStripe;
      case Sweep -> choiceMuteProbabilityInstrumentTypeSweep;
    };
  }

  /**
   * @return ratio of amplitude to dub audio for a given type of instrument
   */
  @SuppressWarnings("DuplicatedCode")
  public double getDubMasterVolume(InstrumentType instrumentType) {
    return switch (instrumentType) {
      case Bass -> dubMasterVolumeInstrumentTypeBass;
      case Drum -> dubMasterVolumeInstrumentTypeDrum;
      case Hook -> dubMasterVolumeInstrumentTypeHook;
      case Noise -> dubMasterVolumeInstrumentTypeNoise;
      case Pad -> dubMasterVolumeInstrumentTypePad;
      case Percussion -> dubMasterVolumeInstrumentTypePercussion;
      case Stab -> dubMasterVolumeInstrumentTypeStab;
      case Sticky -> dubMasterVolumeInstrumentTypeSticky;
      case Stripe -> dubMasterVolumeInstrumentTypeStripe;
      case Sweep -> dubMasterVolumeInstrumentTypeSweep;
    };
  }

  /**
   * @return names of small transitions
   */
  public List<String> getEventNamesSmall() {
    return eventNamesSmall;
  }

  /**
   * @return names of medium transitions
   */
  public List<String> getEventNamesMedium() {
    return eventNamesMedium;
  }

  /**
   * @return names of large transitions
   */
  public List<String> getEventNamesLarge() {
    return eventNamesLarge;
  }

  /**
   * @return list of instrument types for which we'll finalize audio lengths, as in one-shot instrument audios
   */
  public List<InstrumentType> getInstrumentTypesForAudioLengthFinalization() {
    return instrumentTypesForAudioLengthFinalization;
  }

  /**
   * @return list of instrument types for which we'll seek inversions, as in note picking for chords
   */
  public List<InstrumentType> getInstrumentTypesForInversionSeeking() {
    return instrumentTypesForInversionSeeking;
  }

  /**
   * @return max length (delta) for a main program to run
   */
  public int getMainProgramLengthMaxDelta() {
    return mainProgramLengthMaxDelta;
  }

  /**
   * @return meme taxonomy, categories of implicitly separate memes
   */
  public MemeTaxonomy getMemeTaxonomy() {
    return memeTaxonomy;
  }

  /**
   * @return mixer Compress Ahead Seconds
   */
  public double getMixerCompressAheadSeconds() {
    return mixerCompressAheadSeconds;
  }

  /**
   * @return mixer Compress Decay Seconds
   */
  public double getMixerCompressDecaySeconds() {
    return mixerCompressDecaySeconds;
  }

  /**
   * @return mixer Compress Ratio Max
   */
  public double getMixerCompressRatioMax() {
    return mixerCompressRatioMax;
  }

  /**
   * @return mixer Compress Ratio Min
   */
  public double getMixerCompressRatioMin() {
    return mixerCompressRatioMin;
  }

  /**
   * @return mixer Compress To Amplitude
   */
  public double getMixerCompressToAmplitude() {
    return mixerCompressToAmplitude;
  }

  /**
   * @return mixer Dsp Buffer Size
   */
  public int getMixerDspBufferSize() {
    return mixerDspBufferSize;
  }

  /**
   * @return mixer Highpass Threshold Hz
   */
  public double getMixerHighpassThresholdHz() {
    return mixerHighpassThresholdHz;
  }

  /**
   * @return mixer Lowpass Threshold Hz
   */
  public double getMixerLowpassThresholdHz() {
    return mixerLowpassThresholdHz;
  }

  /**
   * @return mixer Normalization Max
   */
  public double getMixerNormalizationCeiling() {
    return mixerNormalizationCeiling;
  }

  /**
   * @return mixer limit of how much to boost during normalization
   */
  public double getMixerNormalizationBoostThreshold() {
    return mixerNormalizationBoostThreshold;
  }

  /**
   * @return # of Output Channels
   */
  public int getOutputChannels() {
    return outputChannels;
  }

  /**
   * @return Output Encoding
   */
  public AudioFormat.Encoding getOutputEncoding() {
    return outputEncoding;
  }

  /**
   * @return Output Frame Rate (Hz)
   */
  public int getOutputFrameRate() {
    return outputFrameRate;
  }

  /**
   * @return Output Sample Bits
   */
  public int getOutputSampleBits() {
    return outputSampleBits;
  }

  /**
   * @return the maximum # of layers of percussive loops
   */
  public int getPercLoopLayerMax() {
    return percLoopLayerMax;
  }

  /**
   * @return the minimum # of layers of percussive loops
   */
  public int getPercLoopLayerMin() {
    return percLoopLayerMin;
  }

  /**
   * @return true if sticky bun behavior is enabled
   */
  public boolean isStickyBunEnabled() {
    return stickyBunEnabled;
  }

  /**
   * @return transition layer min
   */
  public int getTransitionLayerMin() {
    return transitionLayerMin;
  }

  /**
   * @return transition layer max
   */
  public int getTransitionLayerMax() {
    return transitionLayerMax;
  }


  /**
   * @return virtual machine resource limit # of CPU
   */
  public double getVmResourceLimitCpu() {
    return vmResourceLimitCpu;
  }

  /**
   * @return virtual machine resource limit memory (gigabytes)
   */
  public double getVmResourceLimitMemoryGb() {
    return vmResourceLimitMemoryGb;
  }

}
