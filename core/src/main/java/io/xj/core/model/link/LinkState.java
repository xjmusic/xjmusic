// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.link;

import com.google.common.collect.ImmutableList;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import java.util.Objects;

public enum LinkState {
  Planned,
  Crafting,
  Crafted,
  Dubbing,
  Dubbed,
  Failed;

  /**
   String Values
   @return ImmutableList of string values
   */
  public static ImmutableList<String> stringValues() {
    ImmutableList.Builder<String> valuesBuilder = ImmutableList.builder();
    for (LinkState value : values()) {
      valuesBuilder.add(value.toString());
    }
    return valuesBuilder.build();
  }

  /**
   cast string to enum

   @param value to cast to enum
   @return enum
   @throws BusinessException on failure
   */
  public static LinkState validate(String value) throws BusinessException {
    if (Objects.isNull(value))
      throw new BusinessException("State is required");

    try {
      return valueOf(Text.ProperSlug(value));
    } catch (Exception e) {
      throw new BusinessException("'" + value + "' is not a valid state (" + CSV.joinEnum(values()) + ").", e);
    }
  }

  /**
   * compare to string value
   * @param value to compare
   * @return true if equal
   */
  public boolean equals(String value) {
    return this.toString().equals(value);
  }

}
