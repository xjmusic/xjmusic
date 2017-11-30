// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.model.work;

import io.xj.core.exception.BusinessException;
import io.xj.core.transport.CSV;

import com.google.common.collect.ImmutableList;

import java.util.Objects;

public enum WorkType {
  AudioErase,
  ChainErase,
  ChainFabricate,
  LinkCraft,
  LinkDub;

  /**
   String Values

   @return ImmutableList of string values
   */
  public static ImmutableList<String> stringValues() {
    ImmutableList.Builder<String> valuesBuilder = ImmutableList.builder();
    for (WorkType value : values()) {
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
  public static WorkType validate(String value) throws BusinessException {
    if (Objects.isNull(value))
      throw new BusinessException("Type is required (" + CSV.joinEnum(values()) + ").");

    try {
      return valueOf(value);
    } catch (Exception e) {
      throw new BusinessException("'" + value + "' is not a valid type (" + CSV.joinEnum(values()) + ").", e);
    }
  }
}
