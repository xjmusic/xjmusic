// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import io.xj.hub.pojos.Program;
import io.xj.hub.util.StringUtils;
import io.xj.hub.util.ValueException;

import java.util.HashMap;
import java.util.Map;

/**
 Parse a TypeSafe `config` value for a Program's configuration, overriding values from top-level default.conf--
 e.g.
 if the `config` value contains only `previewLengthMaxHours = 8`
 */
public class ProgramConfig {
  public static final String DEFAULT =
    """
      barBeats = 4
      cutoffMinimumBars = 2
      doPatternRestartOnChord = false
      """;
  final Boolean doPatternRestartOnChord;
  final int barBeats;
  final int cutoffMinimumBars;

  /**
   Instantiate a Program configuration from a string of typesafe config.
   Said string will be embedded in a `program{...}` block such that
   provided simple Key=Value pairs will be understood as members of `program`
   e.g. will override values from the `program{...}` block of the top-level **default.conf**

   @param program to get config from
   */
  public ProgramConfig(Program program) throws ValueException {
    this(program.getConfig());
  }

  /**
   Get a program config from only the default config

   @throws ValueException on failure
   */
  public ProgramConfig() throws ValueException {
    this("");
  }

  /**
   Instantiate a Program configuration from a string of typesafe config.
   Said string will be embedded in a `program{...}` block such that
   provided simple Key=Value pairs will be understood as members of `program`
   e.g. will override values from the `program{...}` block of the top-level **default.conf**
   */
  public ProgramConfig(String configText) throws ValueException {
    try {
      Config config = StringUtils.isNullOrEmpty(configText) ?
        ConfigFactory.parseString(DEFAULT) :
        ConfigFactory.parseString(configText).withFallback(ConfigFactory.parseString(DEFAULT));
      barBeats = config.getInt("barBeats");
      cutoffMinimumBars = config.getInt("cutoffMinimumBars");
      doPatternRestartOnChord = config.getBoolean("doPatternRestartOnChord");

    } catch (ConfigException e) {
      throw new ValueException(e.getMessage());
    }
  }

  @SuppressWarnings("DuplicatedCode")
  @Override
  public String toString() {
    Map<String, String> config = new HashMap<>();
    config.put("barBeats", String.valueOf(barBeats));
    config.put("cutoffMinimumBars", String.valueOf(cutoffMinimumBars));
    config.put("doPatternRestartOnChord", doPatternRestartOnChord.toString());
    return StringUtils.formatMultiline(config.entrySet().stream()
      .sorted(Map.Entry.comparingByKey())
      .map(pair -> String.format("%s = %s", pair.getKey(), pair.getValue()))
      .toArray());
  }

  /**
   @return the number of beats in a bar
   */
  public int getBarBeats() {
    return barBeats;
  }

  /**
   @return the minimum number of bars in a subsection
   */
  public int getCutoffMinimumBars() {
    return cutoffMinimumBars;
  }

  /**
   @return True if multiphonic
   */
  public Boolean doPatternRestartOnChord() {
    return doPatternRestartOnChord;
  }

}
