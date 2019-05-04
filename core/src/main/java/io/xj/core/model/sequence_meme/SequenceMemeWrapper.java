// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.sequence_meme;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class SequenceMemeWrapper {
  private SequenceMeme sequenceMeme;

  public SequenceMeme getSequenceMeme() {
    return sequenceMeme;
  }

  public SequenceMemeWrapper setSequenceMeme(SequenceMeme sequenceMeme) {
    this.sequenceMeme = sequenceMeme;
    return this;
  }
}
