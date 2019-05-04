// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.pattern_chord;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class PatternChordWrapper {
  private PatternChord patternChord;

  public PatternChord getPatternChord() {
    return patternChord;
  }

  public PatternChordWrapper setPatternChord(PatternChord patternChord) {
    this.patternChord = patternChord;
    return this;
  }
}
