package io.xj.service.hub.dao;

import com.google.common.base.Strings;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import io.xj.Instrument;
import io.xj.lib.util.ValueException;

/**
 Parse a TypeSafe `config` value for a Instrument's configuration, overriding values from top-level default.conf--
 e.g.
 if the `config` value contains only `previewLengthMaxHours = 8`
 */
public class InstrumentConfig {
  private final boolean multiphonic;
  private final boolean atonal;

  /**
   Instantiate a Instrument configuration from a string of typesafe config.
   Said string will be embedded in a `instrument{...}` block such that
   provided simple Key=Value pairs will be understood as members of `instrument`
   e.g. will override values from the `instrument{...}` block of the top-level **default.conf**

   @param instrument to get Config from
   */
  public InstrumentConfig(Instrument instrument, Config defaultConfig) throws ValueException {
    try {
      Config config = Strings.isNullOrEmpty(instrument.getConfig()) ?
        defaultConfig :
        ConfigFactory.parseString(String.format("instrument {\n%s\n}", instrument.getConfig()))
          .withFallback(defaultConfig);
      multiphonic = config.getBoolean("instrument.isMultiphonic");
      atonal = config.getBoolean("instrument.isTonal");

    } catch (ConfigException e) {
      throw new ValueException(e.getMessage());
    }
  }

  /**
   @return whether this instrument ios multiphonic
   */
  public boolean isMultiphonic() {
    return multiphonic;
  }

  /**
   @return whether this instrument is atonal
   */
  public boolean isTonal() {
    return atonal;
  }

}
