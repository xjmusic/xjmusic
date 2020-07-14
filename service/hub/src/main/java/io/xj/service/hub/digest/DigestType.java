// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.digest;

import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.service.hub.HubException;

import java.util.List;
import java.util.Objects;

/**
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 [#154350346] Architect wants a universal HubIngest Provider, to modularize graph mathematics used during craft to ingest any combination of Library, Sequence, and Instrument for any purpose.
 */
public enum DigestType {
  DigestChordMarkov,
  DigestChordProgression,
  DigestHash,
  DigestMeme,
  DigestSequenceStyle;

  /**
   String Values

   @return ImmutableList of string values
   */
  public static List<String> stringValues() {
    return Text.toStrings(values());
  }

  /**
   cast string to enum

   @param value to cast to enum
   @return enum
   @throws HubException on failure
   */
  public static DigestType validate(String value) throws HubException {
    if (Objects.isNull(value))
      throw new HubException("type is required (" + CSV.joinEnum(values()) + ").");

    try {
      return valueOf(value);
    } catch (Exception ignored) {
      throw new HubException("'" + value + "' is not a valid type (" + CSV.joinEnum(values()) + ").");
    }
  }

}
