// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.work;

import io.xj.core.exception.CoreException;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import java.util.List;
import java.util.Objects;

public enum WorkType {
  AudioClone,
  AudioErase,
  ChainErase,
  ChainFabricate,
  InstrumentClone,
  SegmentFabricate,
  SequenceClone,
  SequenceErase,
  PatternClone,
  PatternErase;

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
  public static WorkType validate(String value) throws CoreException {
    if (Objects.isNull(value))
      throw new CoreException("Type is required (" + CSV.joinEnum(values()) + ").");

    try {
      return valueOf(value);
    } catch (Exception e) {
      throw new CoreException("'" + value + "' is not a valid type (" + CSV.joinEnum(values()) + ").", e);
    }
  }
}
