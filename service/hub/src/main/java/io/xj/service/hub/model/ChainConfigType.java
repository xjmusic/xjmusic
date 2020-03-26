// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.model;

import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;

import java.util.List;
import java.util.Objects;

/**
 Chain Configuration Type
 */
public enum ChainConfigType {
  OutputSampleBits,
  OutputFrameRate,
  OutputChannels,
  OutputEncoding,
  OutputEncodingQuality,
  OutputContainer;

  /**
   cast string to chain config type enum

   @param typeString to cast to enum
   @return config type enum
   @throws ValueException on failure
   */
  public static ChainConfigType validate(String typeString) throws ValueException {
    if (Objects.isNull(typeString))
      throw new ValueException("Type is required.");

    try {
      return valueOf(typeString);
    } catch (Exception e) {
      throw new ValueException("'" + typeString + "' is not a valid type (" + CSV.joinEnum(ChainConfigType.values()) + ").", e);
    }
  }

  /**
   String Values

   @return ImmutableList of string values
   */
  public static List<String> stringValues() {
    return Text.toStrings(values());
  }

}
