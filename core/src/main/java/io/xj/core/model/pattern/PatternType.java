// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pattern;

import io.xj.core.exception.BusinessException;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import java.util.List;
import java.util.Objects;

/**
 [#153976073] Artist wants Pattern to have type *Macro* or *Main* (for Macro- or Main-type sequences), or *Intro*, *Loop*, or *Outro* (for Rhythm or Detail-type Sequence) in order to create a composition that is dynamic when chosen to fill a Segment.
 */
public enum PatternType {
  Macro,
  Main,
  Intro,
  Loop,
  Outro;

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
  public static PatternType validate(String value) throws BusinessException {
    if (Objects.isNull(value))
      throw new BusinessException("Type is required");

    try {
      return valueOf(Text.toProperSlug(value));
    } catch (Exception e) {
      throw new BusinessException("'" + value + "' is not a valid type (" + CSV.joinEnum(values()) + ").", e);
    }
  }

  /**
   Pattern types available for detail (including Rhythm) sequences

   @return types
   */
  public static PatternType[] valuesForDetailSequence() {
    return new PatternType[]{Intro, Loop, Outro};
  }

  /**
   String Values

   @return ImmutableList of string values
   */
  public static List<String> stringValuesForDetailSequence() {
    return Text.stringValues(valuesForDetailSequence());
  }


}
