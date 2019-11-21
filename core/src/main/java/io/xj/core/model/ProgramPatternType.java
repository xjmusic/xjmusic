// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model;

import io.xj.core.exception.CoreException;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import java.util.List;
import java.util.Objects;

/**
 [#153976073] Artist wants Pattern to have type *Macro* or *Main* (for Macro- or Main-type sequences), or *Intro*, *Loop*, or *Outro* (for Rhythm or Detail-type Sequence) in order to of a composition that is dynamic when chosen to fill a Segment.
 */
public enum ProgramPatternType {
  Intro,
  Loop,
  Outro;

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
  public static ProgramPatternType validate(String value) throws CoreException {
    if (Objects.isNull(value))
      throw new CoreException("Type is required");

    try {
      return valueOf(Text.toProperSlug(value));
    } catch (Exception e) {
      throw new CoreException("'" + value + "' is not a valid type (" + CSV.joinEnum(values()) + ").", e);
    }
  }

  /**
   Pattern types available for detail (including Rhythm) sequences

   @return types
   */
  public static ProgramPatternType[] valuesForDetailSequence() {
    return new ProgramPatternType[]{Intro, Loop, Outro};
  }

  /**
   String Values

   @return ImmutableList of string values
   */
  public static List<String> stringValuesForDetailSequence() {
    return Text.toStrings(valuesForDetailSequence());
  }


}
