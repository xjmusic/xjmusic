// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.model;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValue;
import io.xj.model.enums.InstrumentType;
import io.xj.model.meme.MemeTaxonomy;
import io.xj.model.pojos.Template;
import io.xj.model.util.StringUtils;
import io.xj.model.util.ValueException;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 Parse a TypeSafe `config` value for a Template's configuration, overriding values from top-level default.conf--
 e.g.
 if the `config` value contains only `previewLengthMaxHours = 8`
 <p>
 Artist saves Template config, validate & combine with defaults. https://github.com/xjmusic/xjmusic/issues/206
 */
public class TemplateConfig {
  public static final String DEFAULT = """
    choiceMuteProbability = {
        Background = 0.0
        Bass = 0.0
        Drum = 0.0
        Hook = 0.0
        Pad = 0.0
        Percussion = 0.0
        Stab = 0.0
        Sticky = 0.0
        Stripe = 0.0
        Transition = 0.0
      }
    deltaArcBeatLayersIncoming = 1
    deltaArcBeatLayersToPrioritize = ["kick"]
    deltaArcEnabled = false
    detailLayerOrder = ["Bass","Stripe","Pad","Sticky","Stab"]
    dubMasterVolume = {
        Background = 1.0
        Bass = 1.0
        Drum = 1.0
        Hook = 1.0
        Pad = 1.0
        Percussion = 1.0
        Stab = 1.0
        Sticky = 1.0
        Stripe = 1.0
        Transition = 1.0
      }
    eventNamesLarge = ["LARGE","BIG","HIGH","PRIMARY"]
    eventNamesMedium = ["MEDIUM","REGULAR","MIDDLE","SECONDARY"]
    eventNamesSmall = ["SMALL","LITTLE","LOW"]
    instrumentTypesForAudioLengthFinalization = ["Bass","Pad","Stab","Sticky","Stripe"]
    instrumentTypesForInversionSeeking = ["Pad","Stab","Sticky","Stripe"]
    intensityAutoCrescendoEnabled = true
    intensityAutoCrescendoMaximum = 0.8
    intensityAutoCrescendoMinimum = 0.2
    intensityLayers = {
        Background = 3
        Bass = 1
        Drum = 1
        Hook = 3
        Pad = 3
        Percussion = 3
        Stab = 2
        Sticky = 2
        Stripe = 2
        Transition = 3
      }
    intensityThreshold = {
        Background = 0.5
        Bass = 0.5
        Drum = 0.5
        Hook = 0.5
        Pad = 0.5
        Percussion = 0.5
        Stab = 0.5
        Sticky = 0.5
        Stripe = 0.5
        Transition = 0.5
      }
    mainProgramLengthMaxDelta = 280
    memeTaxonomy = [
        {
          memes = ["RED","GREEN","BLUE"]
          name = "COLOR"
        },
        {
          memes = ["WINTER","SPRING","SUMMER","FALL"]
          name = "SEASON"
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
    stickyBunEnabled = true
    """;
  final List<InstrumentType> detailLayerOrder;
  final List<InstrumentType> instrumentTypesForAudioLengthFinalization;
  final List<InstrumentType> instrumentTypesForInversionSeeking;
  final List<String> eventNamesLarge;
  final List<String> eventNamesMedium;
  final List<String> eventNamesSmall;
  final MemeTaxonomy memeTaxonomy;
  final List<String> deltaArcBeatLayersToPrioritize;
  final boolean intensityAutoCrescendoEnabled;
  final boolean deltaArcEnabled;
  final boolean stickyBunEnabled;
  final Map<InstrumentType, Double> choiceMuteProbability;
  final Map<InstrumentType, Double> dubMasterVolume;
  final Map<InstrumentType, Integer> intensityLayers;
  final Map<InstrumentType, Double> intensityThreshold;
  final double intensityAutoCrescendoMaximum;
  final double intensityAutoCrescendoMinimum;
  final double mixerCompressAheadSeconds;
  final double mixerCompressDecaySeconds;
  final double mixerCompressRatioMax;
  final double mixerCompressRatioMin;
  final double mixerCompressToAmplitude;
  final double mixerNormalizationBoostThreshold;
  final double mixerNormalizationCeiling;
  final int deltaArcBeatLayersIncoming;
  final int mainProgramLengthMaxDelta;
  final int mixerDspBufferSize;
  final int mixerHighpassThresholdHz;
  final int mixerLowpassThresholdHz;

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
      Config config = StringUtils.isNullOrEmpty(configText) ? ConfigFactory.parseString(DEFAULT) : ConfigFactory.parseString(configText).withFallback(ConfigFactory.parseString(DEFAULT));
      choiceMuteProbability = config.getObject("choiceMuteProbability").unwrapped().entrySet().stream().collect(Collectors.toMap(entry -> InstrumentType.valueOf(entry.getKey()), entry -> Double.parseDouble(entry.getValue().toString())));
      deltaArcBeatLayersIncoming = config.getInt("deltaArcBeatLayersIncoming");
      deltaArcBeatLayersToPrioritize = getStringListWithCsvFallback(config, "deltaArcBeatLayersToPrioritize");
      deltaArcEnabled = config.getBoolean("deltaArcEnabled");
      detailLayerOrder = requireAtLeastOne("detailLayerOrder", config.getStringList("detailLayerOrder").stream().map(InstrumentType::valueOf).toList());
      dubMasterVolume = config.getObject("dubMasterVolume").unwrapped().entrySet().stream().collect(Collectors.toMap(entry -> InstrumentType.valueOf(entry.getKey()), entry -> Double.parseDouble(entry.getValue().toString())));
      eventNamesLarge = requireAtLeastOne("eventNamesLarge", config.getStringList("eventNamesLarge").stream().map(StringUtils::toMeme).toList());
      eventNamesMedium = requireAtLeastOne("eventNamesMedium", config.getStringList("eventNamesMedium").stream().map(StringUtils::toMeme).toList());
      eventNamesSmall = requireAtLeastOne("eventNamesSmall", config.getStringList("eventNamesSmall").stream().map(StringUtils::toMeme).toList());
      instrumentTypesForAudioLengthFinalization = requireAtLeastOne("instrumentTypesForAudioLengthFinalization", config.getStringList("instrumentTypesForAudioLengthFinalization").stream().map(InstrumentType::valueOf).toList());
      instrumentTypesForInversionSeeking = requireAtLeastOne("instrumentTypesForInversionSeeking", config.getStringList("instrumentTypesForInversionSeeking").stream().map(InstrumentType::valueOf).toList());
      intensityAutoCrescendoEnabled = config.getBoolean("intensityAutoCrescendoEnabled");
      intensityAutoCrescendoMaximum = config.getDouble("intensityAutoCrescendoMaximum");
      intensityAutoCrescendoMinimum = config.getDouble("intensityAutoCrescendoMinimum");
      intensityLayers = config.getObject("intensityLayers").unwrapped().entrySet().stream().collect(Collectors.toMap(entry -> InstrumentType.valueOf(entry.getKey()), entry -> Integer.parseInt(entry.getValue().toString())));
      intensityThreshold = config.getObject("intensityThreshold").unwrapped().entrySet().stream().collect(Collectors.toMap(entry -> InstrumentType.valueOf(entry.getKey()), entry -> Double.parseDouble(entry.getValue().toString())));
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
      stickyBunEnabled = config.getBoolean("stickyBunEnabled");
    } catch (ConfigException e) {
      throw new ValueException(e.getMessage());
    }
  }

  /**
   Get a list of strings from a config, falling back to a CSV string if the list is invalid

   @param config to get list from
   @param key    key to get list from
   @return list of strings
   */
  private List<String> getStringListWithCsvFallback(Config config, String key) {
    try {
      return config.getStringList(key);
    } catch (ConfigException e) {
      return List.of(config.getString(key).split(","));
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
    config.put("choiceMuteProbability", this.formatInstrumentTypeDoubles(choiceMuteProbability));
    config.put("deltaArcBeatLayersIncoming", String.valueOf(deltaArcBeatLayersIncoming));
    config.put("deltaArcBeatLayersToPrioritize", formatTypesafeQuoted(deltaArcBeatLayersToPrioritize));
    config.put("deltaArcEnabled", String.valueOf(deltaArcEnabled));
    config.put("detailLayerOrder", formatTypesafeQuoted(detailLayerOrder));
    config.put("dubMasterVolume", this.formatInstrumentTypeDoubles(dubMasterVolume));
    config.put("eventNamesLarge", formatTypesafeQuoted(eventNamesLarge));
    config.put("eventNamesMedium", formatTypesafeQuoted(eventNamesMedium));
    config.put("eventNamesSmall", formatTypesafeQuoted(eventNamesSmall));
    config.put("instrumentTypesForAudioLengthFinalization", formatTypesafeQuoted(instrumentTypesForAudioLengthFinalization));
    config.put("instrumentTypesForInversionSeeking", formatTypesafeQuoted(instrumentTypesForInversionSeeking));
    config.put("intensityAutoCrescendoEnabled", String.valueOf(intensityAutoCrescendoEnabled));
    config.put("intensityAutoCrescendoMaximum", String.valueOf(intensityAutoCrescendoMaximum));
    config.put("intensityAutoCrescendoMinimum", String.valueOf(intensityAutoCrescendoMinimum));
    config.put("intensityLayers", formatInstrumentTypeIntegers(intensityLayers));
    config.put("intensityThreshold", this.formatInstrumentTypeDoubles(intensityThreshold));
    config.put("mainProgramLengthMaxDelta", String.valueOf(mainProgramLengthMaxDelta));
    config.put("memeTaxonomy", formatMemeTaxonomy(memeTaxonomy));
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
    config.put("stickyBunEnabled", String.valueOf(stickyBunEnabled));
    return StringUtils.formatMultiline(config.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(pair -> String.format("%s = %s", pair.getKey(), pair.getValue())).toArray());
  }

  /**
   Compute the HOCON string value of the meme taxonomy

   @return string value
   */
  String formatMemeTaxonomy(MemeTaxonomy taxonomy) {
    return String.format("[%s\n  ]", taxonomy.getCategories().stream()
      .map(category -> String.format("\n    {\n      memes = [%s]\n      name = %s\n    }",
        category.getMemes().stream().map(StringUtils::doubleQuoted).collect(Collectors.joining(",")),
        StringUtils.doubleQuoted(category.getName())))
      .collect(Collectors.joining(",")));
  }

  /**
   Compute the HOCON string value of a map of instrument types to double values

   @return string value
   */
  String formatInstrumentTypeDoubles(Map<InstrumentType, Double> instrumentTypeDoubles) {
    return String.format("{%s\n  }", instrumentTypeDoubles.entrySet().stream()
      .sorted(Comparator.comparing((entry) -> entry.getKey().toString()))
      .map(type -> String.format("\n    %s = %s", type.getKey(), StringUtils.formatMinimumDigits(type.getValue())))
      .collect(Collectors.joining()));
  }

  /**
   Compute the HOCON string value of a map of instrument modes to integer values

   @return string value
   */
  String formatInstrumentTypeIntegers(Map<InstrumentType, Integer> instrumentTypeIntegers) {
    return String.format("{%s\n  }", instrumentTypeIntegers.entrySet().stream()
      .sorted(Comparator.comparing((entry) -> entry.getKey().toString()))
      .map(mode -> String.format("\n    %s = %d", mode.getKey(), mode.getValue()))
      .collect(Collectors.joining()));
  }

  /**
   Convert a list of objects in a quoted list of strings in a set of brackets, for inclusion in a typesafe config

   @param values to format
   @return typesafe array of quoted values
   */
  <N> String formatTypesafeQuoted(List<N> values) {
    return String.format("[%s]", values.stream().map(N::toString).map(StringUtils::doubleQuoted).collect(Collectors.joining(",")));
  }

  /**
   @return instrument layers in the intended order of craft
   */
  public List<InstrumentType> getDetailLayerOrder() {
    return detailLayerOrder;
  }

  /**
   @return true if choice delta is enabled
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean isDeltaArcEnabled() {
    return deltaArcEnabled;
  }

  /**
   @return the number of delta arc beat layers incoming each segment
   */
  public int getDeltaArcBeatLayersIncoming() {
    return deltaArcBeatLayersIncoming;
  }

  /**
   @return delta arc beat layer prioritization regexp
   */
  public List<String> getDeltaArcBeatLayersToPrioritize() {
    return deltaArcBeatLayersToPrioritize;
  }

  /**
   @return probability of a given type of instrument choice being muted for any given segment
   */
  @SuppressWarnings("DuplicatedCode")
  public double getChoiceMuteProbability(InstrumentType instrumentType) {
    return choiceMuteProbability.getOrDefault(instrumentType, 0.0);
  }

  /**
   @return ratio of amplitude to dub audio for a given type of instrument
   */
  @SuppressWarnings("DuplicatedCode")
  public double getDubMasterVolume(InstrumentType instrumentType) {
    return dubMasterVolume.getOrDefault(instrumentType, 1.0);
  }

  /**
   @return ratio of amplitude to dub audio for a given type of instrument
   */
  @SuppressWarnings("DuplicatedCode")
  public int getIntensityLayers(InstrumentType instrumentType) {
    return intensityLayers.getOrDefault(instrumentType, 1);
  }

  /**
   @return names of small transitions
   */
  public List<String> getEventNamesSmall() {
    return eventNamesSmall;
  }

  /**
   @return names of medium transitions
   */
  public List<String> getEventNamesMedium() {
    return eventNamesMedium;
  }

  /**
   @return names of large transitions
   */
  public List<String> getEventNamesLarge() {
    return eventNamesLarge;
  }

  /**
   @return list of instrument types for which we'll finalize audio lengths, as in one-shot instrument audios
   */
  public List<InstrumentType> getInstrumentTypesForAudioLengthFinalization() {
    return instrumentTypesForAudioLengthFinalization;
  }

  /**
   @return list of instrument types for which we'll seek inversions, as in note picking for chords
   */
  public List<InstrumentType> getInstrumentTypesForInversionSeeking() {
    return instrumentTypesForInversionSeeking;
  }

  /**
   @return true if choice delta is enabled
   */
  @SuppressWarnings("BooleanMethodIsAlwaysInverted")
  public boolean isIntensityAutoCrescendoEnabled() {
    return intensityAutoCrescendoEnabled;
  }

  /**
   @return intensity auto-crescendo maximum value
   */
  public double getIntensityAutoCrescendoMaximum() {
    return intensityAutoCrescendoMaximum;
  }

  /**
   @return intensity auto-crescendo minimum value
   */
  public double getIntensityAutoCrescendoMinimum() {
    return intensityAutoCrescendoMinimum;
  }

  /**
   @return threshold of a given type of instrument audio intensity to the target intensity to fade from 0 to 100% volume
   */
  public double getIntensityThreshold(InstrumentType instrumentType) {
    return intensityThreshold.getOrDefault(instrumentType, 0.0);
  }

  /**
   @return max length (delta) for a main program to run
   */
  public int getMainProgramLengthMaxDelta() {
    return mainProgramLengthMaxDelta;
  }

  /**
   @return meme taxonomy, categories of implicitly separate memes
   */
  public MemeTaxonomy getMemeTaxonomy() {
    return memeTaxonomy;
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
   @return true if sticky bun behavior is enabled
   */
  public boolean isStickyBunEnabled() {
    return stickyBunEnabled;
  }
}
