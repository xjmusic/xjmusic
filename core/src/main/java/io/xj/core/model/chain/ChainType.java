// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain;

import io.xj.core.exception.CoreException;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import java.util.List;
import java.util.Objects;

public enum ChainType {
  Preview,
  Production;

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
   @throws CoreException on failure
   */
  public static ChainType validate(String value) throws CoreException {
    if (Objects.isNull(value))
      return Preview;

    try {
      return valueOf(Text.toProperSlug(value));
    } catch (Exception ignored) {
      throw new CoreException("'" + value + "' is not a valid type (" + CSV.joinEnum(values()) + ").");
    }
  }

}
