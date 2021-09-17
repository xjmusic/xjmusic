// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import io.xj.hub.tables.pojos.Program;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;

import java.util.Map;

/**
 Parse a TypeSafe `config` value for a Program's configuration, overriding values from top-level default.conf--
 e.g.
 if the `config` value contains only `previewLengthMaxHours = 8`
 */
public class ProgramConfig {
  private static final String KEY_PREFIX = "program.";
  private static final String KEY_PATTERN_RESTART_ON_CHORD = "doPatternRestartOnChord";
  private final Boolean doPatternRestartOnChord;

  /**
   Instantiate a Program configuration from a string of typesafe config.
   Said string will be embedded in a `program{...}` block such that
   provided simple Key=Value pairs will be understood as members of `program`
   e.g. will override values from the `program{...}` block of the top-level **default.conf**
   */
  public ProgramConfig(String configText, Config defaultConfig) throws ValueException {
    try {
      Config config = Strings.isNullOrEmpty(configText) ?
        defaultConfig :
        ConfigFactory.parseString(String.format("program {\n%s\n}", configText))
          .withFallback(defaultConfig);
      doPatternRestartOnChord = getOptionalBoolean(config, prefixed(KEY_PATTERN_RESTART_ON_CHORD));

    } catch (ConfigException e) {
      throw new ValueException(e.getMessage());
    }
  }

  /**
   Instantiate a Program configuration from a string of typesafe config.
   Said string will be embedded in a `program{...}` block such that
   provided simple Key=Value pairs will be understood as members of `program`
   e.g. will override values from the `program{...}` block of the top-level **default.conf**

   @param program to get Config from
   */
  public ProgramConfig(Program program, Config defaultConfig) throws ValueException {
    this(program.getConfig(), defaultConfig);
  }

  /**
   Get a program config from only the default config

   @param config from which to get program config
   @throws ValueException on failure
   */
  public ProgramConfig(Config config) throws ValueException {
    this("", config);
  }

  /**
   Program-prefixed version of a key

   @param key to prefix
   @return program-prefixed key
   */
  @SuppressWarnings("SameParameterValue")
  private static String prefixed(String key) {
    return String.format("%s%s", KEY_PREFIX, key);
  }

  /**
   If a boolean value is present in the config, return it, otherwise false

   @param config to search for value at key
   @param key    at which to search
   @return value if present, else false
   */
  private static Boolean getOptionalBoolean(Config config, String key) {
    if (!config.hasPath(key)) return false;
    return config.getBoolean(key);
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public String toString() {
    Map<String, String> config = Maps.newHashMap();
    config.put(KEY_PATTERN_RESTART_ON_CHORD, doPatternRestartOnChord.toString());
    return Text.formatMultiline(config.entrySet().stream()
      .sorted(Map.Entry.comparingByKey())
      .map(pair -> String.format("%s = %s", pair.getKey(), pair.getValue()))
      .toArray());
  }

  /**
   @return True if multiphonic
   */
  public Boolean doPatternRestartOnChord() {
    return doPatternRestartOnChord;
  }

}
