// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.model;

import io.xj.lib.core.exception.CoreException;
import io.xj.lib.core.util.CSV;
import io.xj.lib.core.util.Text;

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
   @throws CoreException on failure
   */
  public static UserAuthType validate(String value) throws CoreException {
    if (Objects.isNull(value))
      throw new CoreException("Role is required");

    try {
      return valueOf(Text.toProperSlug(value));
    } catch (Exception e) {
      throw new CoreException("'" + value + "' is not a valid role (" + CSV.joinEnum(values()) + ").", e);
    }
  }
}
