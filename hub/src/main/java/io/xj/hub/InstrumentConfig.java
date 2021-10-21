// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import io.xj.hub.tables.pojos.Instrument;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;

import java.util.Map;

/**
 Parse a TypeSafe `config` value for an Instrument's configuration, overriding values from top-level default.conf--
 e.g.
 if the `config` value contains only `previewLengthMaxHours = 8`
 */
public class InstrumentConfig {
  public static final String DEFAULT =
    """
      instrument {
        isMultiphonic = false
        isTonal = false
      }
      """;
  private static final String KEY_PREFIX = "instrument.";
  private final Boolean isTonal;
  private final Boolean isMultiphonic;

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
        ConfigFactory.parseString(String.format("instrument {\n%s\n}", configText))
          .withFallback(ConfigFactory.parseString(DEFAULT));
      isTonal = getOptionalBoolean(config, prefixed("isTonal"));
      isMultiphonic = getOptionalBoolean(config, prefixed("isMultiphonic"));

    } catch (ConfigException e) {
      throw new ValueException(e.getMessage());
    }
  }

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
   If a boolean value is present in the config, return it, otherwise false

   @param config to search for value at key
   @param key    at which to search
   @return value if present, else false
   */
  private Boolean getOptionalBoolean(Config config, String key) {
    if (!config.hasPath(key)) return false;
    return config.getBoolean(key);
  }

  /**
   Instrument-prefixed version of a key

   @param key to prefix
   @return instrument-prefixed key
   */
  private String prefixed(String key) {
    return String.format("%s%s", KEY_PREFIX, key);
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public String toString() {
    Map<String, String> config = Maps.newHashMap();
    config.put("isTonal", isTonal.toString());
    config.put("isMultiphonic", isMultiphonic.toString());
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
}
