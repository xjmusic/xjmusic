// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.phase;

import io.xj.core.exception.BusinessException;
import io.xj.core.transport.CSV;
import io.xj.core.util.Text;

import java.util.List;
import java.util.Objects;

/**
 [#153976073] Artist wants Phase to have type *Macro* or *Main* (for Macro- or Main-type patterns), or *Intro*, *Loop*, or *Outro* (for Rhythm or Detail-type Pattern) in order to create a composition that is dynamic when chosen to fill a Link.
 */
public enum PhaseType {
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
  public static PhaseType validate(String value) throws BusinessException {
    if (Objects.isNull(value))
      throw new BusinessException("Type is required");

    try {
      return valueOf(Text.toProperSlug(value));
    } catch (Exception e) {
      throw new BusinessException("'" + value + "' is not a valid type (" + CSV.joinEnum(values()) + ").", e);
    }
  }

  /**
   Phase types available for detail (including Rhythm) patterns

   @return types
   */
  public static PhaseType[] valuesForDetailPattern() {
    return new PhaseType[]{Intro, Loop, Outro};
  }

  /**
   String Values

   @return ImmutableList of string values
   */
  public static List<String> stringValuesForDetailPattern() {
    return Text.stringValues(valuesForDetailPattern());
  }


}
