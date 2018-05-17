// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.craft.digest;

import io.xj.core.exception.BusinessException;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import java.util.List;
import java.util.Objects;

/**
 [#154234716] Architect wants ingest of library contents, to modularize graph mathematics used during craft, and provide the Artist with useful insight for developing the library.
 [#154350346] Architect wants a universal Ingest Provider, to modularize graph mathematics used during craft to evaluate any combination of Library, Sequence, and Instrument for any purpose.
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
    return Text.stringValues(values());
  }

  /**
   cast string to enum

   @param value to cast to enum
   @return enum
   @throws BusinessException on failure
   */
  public static DigestType validate(String value) throws BusinessException {
    if (Objects.isNull(value))
      throw new BusinessException("type is required (" + CSV.joinEnum(values()) + ").");

    try {
      return valueOf(value);
    } catch (Exception ignored) {
      throw new BusinessException("'" + value + "' is not a valid type (" + CSV.joinEnum(values()) + ").");
    }
  }

}
