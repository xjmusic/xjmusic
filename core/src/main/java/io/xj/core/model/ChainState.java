// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import io.xj.core.exception.CoreException;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import java.util.List;
import java.util.Objects;

public enum ChainState {
  Draft,
  Ready,
  Fabricate,
  Complete,
  Failed,
  Erase;

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
   @throws CoreException on failure
   */
  public static ChainState validate(String value) throws CoreException {
    if (Objects.isNull(value))
      return Draft;

    try {
      return valueOf(Text.toProperSlug(value));
    } catch (Exception ignored) {
      throw new CoreException("'" + value + "' is not a valid state (" + CSV.joinEnum(values()) + ").");
    }
  }

}
