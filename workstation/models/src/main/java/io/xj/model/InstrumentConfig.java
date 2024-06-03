// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.model;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.xj.model.pojos.Instrument;
import io.xj.model.util.CsvUtils;
import io.xj.model.util.StringUtils;
import io.xj.model.util.ValueException;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 Parse a TypeSafe `config` value for an Instrument's configuration, overriding values from top-level default.conf--
 e.g.
 if the `config` value contains only `previewLengthMaxHours = 8`
 */
public class InstrumentConfig {
  public static final String DEFAULT = """
    isAudioSelectionPersistent = true
    isMultiphonic = false
    isOneShot = false
    isOneShotCutoffEnabled = true
    isTonal = false
    oneShotObserveLengthOfEvents = []
    releaseMillis = 5
    """;

  final Boolean isAudioSelectionPersistent;

  final Boolean isMultiphonic;
  final Boolean isOneShot;
  final Boolean isOneShotCutoffEnabled;
  final Boolean isTonal;
  final Integer releaseMillis;
  final Collection<String> oneShotObserveLengthOfEvents;

  /**
   Instantiate an Instrument configuration from a string of typesafe config.
   Said string will be embedded in a `instrument{...}` block such that
   provided simple Key=Value pairs will be understood as members of `instrument`
   e.g. will override values from the `instrument{...}` block of the top-level **default.conf**

   @param instrument to get config from
   */
  public InstrumentConfig(Instrument instrument) {
    this(instrument.getConfig());
  }

  /**
   Get an instrument config from only the default config

   @throws ValueException on failure
   */
  public InstrumentConfig() throws ValueException {
    this("");
  }

  /**
   Instantiate an Instrument configuration from a string of typesafe config.
   Said string will be embedded in a `instrument{...}` block such that
   provided simple Key=Value pairs will be understood as members of `instrument`
   e.g. will override values from the `instrument{...}` block of the top-level **default.conf**
   */
  public InstrumentConfig(String configText) {
    Config config = StringUtils.isNullOrEmpty(configText) ? ConfigFactory.parseString(DEFAULT) : ConfigFactory.parseString(configText).withFallback(ConfigFactory.parseString(DEFAULT));
    isAudioSelectionPersistent = config.getBoolean("isAudioSelectionPersistent");
    isMultiphonic = config.getBoolean("isMultiphonic");
    isOneShot = config.getBoolean("isOneShot");
    isOneShotCutoffEnabled = config.getBoolean("isOneShotCutoffEnabled");
    isTonal = config.getBoolean("isTonal");
    oneShotObserveLengthOfEvents = config.getStringList("oneShotObserveLengthOfEvents").stream().map(StringUtils::toMeme).collect(Collectors.toList());
    releaseMillis = config.getInt("releaseMillis");
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public String toString() {
    Map<String, String> config = new HashMap<>();
    config.put("isAudioSelectionPersistent", isAudioSelectionPersistent.toString());
    config.put("isMultiphonic", isMultiphonic.toString());
    config.put("isOneShot", isOneShot.toString());
    config.put("isOneShotCutoffEnabled", isOneShotCutoffEnabled.toString());
    config.put("isTonal", isTonal.toString());
    config.put("oneShotObserveLengthOfEvents", String.format("[%s]", CsvUtils.join(oneShotObserveLengthOfEvents.stream().sorted().toList())));
    config.put("releaseMillis", releaseMillis.toString());
    return StringUtils.formatMultiline(config.entrySet().stream().sorted(Map.Entry.comparingByKey()).map(pair -> String.format("%s = %s", pair.getKey(), pair.getValue())).toArray());
  }

  /**
   @return True if multiphonic
   */
  public Boolean isMultiphonic() {
    return isMultiphonic;
  }

  /**
   @return true if instrument is one-shot (samples play til end, regardless of note length)
   */
  public Boolean isOneShot() {
    return isOneShot;
  }

  /**
   @return true if this instrument's one-shot cutoff are enabled
   */
  public Boolean isOneShotCutoffEnabled() {
    return isOneShotCutoffEnabled;
  }

  /**
   @return true if tonal
   */
  public Boolean isTonal() {
    return isTonal;
  }

  /**
   @return a list of event types that will ignore one-shot, if instrument is one-shot
   */
  public Collection<String> getOneShotObserveLengthOfEvents() {
    return oneShotObserveLengthOfEvents;
  }

  /**
   @return whether to choose the same instrument audio per note throughout a main program
   */
  public Boolean isAudioSelectionPersistent() {
    return isAudioSelectionPersistent;
  }

  /**
   @return Milliseconds length of one-shot instrument fadeout
   */
  public Integer getReleaseMillis() {
    return releaseMillis;
  }
}
