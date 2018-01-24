// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.generation;

import io.xj.core.exception.BusinessException;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import java.util.List;
import java.util.Objects;

/**
 Type of Macro-Craft, depending on previous link existence and choices
 */
public enum GenerationType {
  LibrarySuperpattern; // the first macro and main patterns in the chain


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
  public static GenerationType validate(String value) throws BusinessException {
    if (Objects.isNull(value))
      throw new BusinessException("type is required (" + CSV.joinEnum(values()) + ").");

    try {
      return valueOf(value);
    } catch (Exception ignored) {
      throw new BusinessException("'" + value + "' is not a valid type (" + CSV.joinEnum(values()) + ").");
    }
  }
}
