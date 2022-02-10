// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

/**
 Parse a TypeSafe `config` value for an Instrument's configuration, overriding values from top-level default.conf--
 e.g.
 if the `config` value contains only `previewLengthMaxHours = 8`
 */
public class InstrumentConfig {
  public static final String DEFAULT =
    """
      isMultiphonic = false
      isOneShot = false
      isOneShotCutoffEnabled = true
      isTonal = false
      oneShotObserveLengthOfEvents = []
      """;
  private final Boolean isMultiphonic;
  private final Boolean isOneShot;
  private final Boolean isTonal;
  private final Collection<String> oneShotObserveLengthOfEvents;

  private final Boolean isOneShotCutoffEnabled;

  /**
   Instantiate an Instrument configuration from a string of typesafe config.
   Said string will be embedded in a `instrument{...}` block such that
   provided simple Key=Value pairs will be understood as members of `instrument`
   e.g. will override values from the `instrument{...}` block of the top-level **default.conf**

   @param instrument to get config from
   */
  public InstrumentConfig(Instrument instrument) throws ValueException {
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
  public InstrumentConfig(String configText) throws ValueException {
    try {
      Config config = Strings.isNullOrEmpty(configText) ?
        ConfigFactory.parseString(DEFAULT) :
        ConfigFactory.parseString(configText).withFallback(ConfigFactory.parseString(DEFAULT));
      isMultiphonic = config.getBoolean("isMultiphonic");
      isOneShot = config.getBoolean("isOneShot");
      isOneShotCutoffEnabled = config.getBoolean("isOneShotCutoffEnabled");
      isTonal = config.getBoolean("isTonal");
      oneShotObserveLengthOfEvents = config.getStringList("oneShotObserveLengthOfEvents").stream().map(Text::toMeme).collect(Collectors.toList());

    } catch (ConfigException e) {
      throw new ValueException(e.getMessage());
    }
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public String toString() {
    Map<String, String> config = Maps.newHashMap();
    config.put("isMultiphonic", isMultiphonic.toString());
    config.put("isOneShot", isOneShot.toString());
    config.put("isOneShotCutoffEnabled", isOneShotCutoffEnabled.toString());
    config.put("isTonal", isTonal.toString());
    config.put("oneShotObserveLengthOfEvents", String.format("[%s]", CSV.join(oneShotObserveLengthOfEvents)));
    return Text.formatMultiline(config.entrySet().stream()
      .sorted(Map.Entry.comparingByKey())
      .map(pair -> String.format("%s = %s", pair.getKey(), pair.getValue()))
      .toArray());
  }

  /**
   @return True if multiphonic
   */
  public Boolean isMultiphonic() {
    return isMultiphonic;
  }

  /**
   @return true if tonal
   */
  public Boolean isTonal() {
    return isTonal;
  }

  /**
   @return true if instrument is one-shot (samples play til end, regardless of note length)
   */
  public Boolean isOneShot() {
    return isOneShot;
  }

  /**
   @return a list of event types that will ignore one-shot, if instrument is one-shot
   */
  public Collection<String> getOneShotObserveLengthOfEvents() {
    return oneShotObserveLengthOfEvents;
  }

  /**
   @return true if this instrument's one-shot cutoff are enabled
   */
  public Boolean isOneShotCutoffEnabled() {
    return isOneShotCutoffEnabled;
  }
}
