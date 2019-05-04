// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.sequence_pattern;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class SequencePatternWrapper {
  private SequencePattern sequencePattern;

  public SequencePattern getSequencePattern() {
    return sequencePattern;
  }

  public SequencePatternWrapper setSequencePattern(SequencePattern sequencePattern) {
    this.sequencePattern = sequencePattern;
    return this;
  }

}
