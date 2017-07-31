// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.job;

import io.xj.core.app.exception.BusinessException;
import io.xj.core.transport.CSV;

import com.google.common.collect.ImmutableList;

import java.util.Objects;

public enum JobType {
  AudioDelete,
  ChainDelete,
  ChainFabricate,
  LinkCraft,
  LinkDelete,
  LinkDub;

  /**
   String Values

   @return ImmutableList of string values
   */
  public static ImmutableList<String> stringValues() {
    ImmutableList.Builder<String> valuesBuilder = ImmutableList.builder();
    for (JobType value : values()) {
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
  public static JobType validate(String value) throws BusinessException {
    if (Objects.isNull(value))
      throw new BusinessException("Type is required (" + CSV.joinEnum(values()) + ").");

    try {
      return valueOf(value);
    } catch (Exception e) {
      throw new BusinessException("'" + value + "' is not a valid type (" + CSV.joinEnum(values()) + ").", e);
    }
  }
}
