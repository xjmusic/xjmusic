// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.sequence;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class SequenceWrapper {
  private Sequence sequence;

  public Sequence getSequence() {
    return sequence;
  }

  public SequenceWrapper setSequence(Sequence sequence) {
    this.sequence = sequence;
    return this;
  }
}
