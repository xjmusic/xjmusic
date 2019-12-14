// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import io.xj.core.exception.CoreException;
import io.xj.core.util.CSV;
import io.xj.core.util.Text;

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
   @throws CoreException on failure
   */
  public static ChainConfigType validate(String typeString) throws CoreException {
    if (Objects.isNull(typeString))
      throw new CoreException("Type is required.");

    try {
      return valueOf(typeString);
    } catch (Exception e) {
      throw new CoreException("'" + typeString + "' is not a valid type (" + CSV.joinEnum(ChainConfigType.values()) + ").", e);
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
