// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.entity;

import io.xj.lib.util.CSV;
import io.xj.lib.util.Text;
import io.xj.lib.util.ValueException;

import java.util.List;
import java.util.Objects;

public enum UserAuthType {
  Google;

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
   @throws ValueException on failure
   */
  public static UserAuthType validate(String value) throws ValueException {
    if (Objects.isNull(value))
      throw new ValueException("Role is required");

    try {
      return valueOf(Text.toProperSlug(value));
    } catch (Exception e) {
      throw new ValueException("'" + value + "' is not a valid role (" + CSV.joinEnum(values()) + ").", e);
    }
  }
}
