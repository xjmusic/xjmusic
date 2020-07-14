// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.entity;

import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;

import java.util.List;
import java.util.Objects;

public enum MessageType {
  Debug,
  Info,
  Warning,
  Error;

  /**
   String Values

   @return ImmutableList of string values
   */
  public static List<String> stringValues() {
    return Text.toStrings((Object[]) values());
  }

  /**
   cast string to enum

   @param value to cast to enum
   @return enum
   @throws ValueException on failure
   */
  public static MessageType validate(String value) throws ValueException {
    if (Objects.isNull(value))
      throw new ValueException("Type is required");

    try {
      return valueOf(Text.toProperSlug(value));
    } catch (Exception e) {
      throw new ValueException("'" + value + "' is not a valid type (" + CSV.joinEnum(values()) + ").", e);
    }
  }

}
