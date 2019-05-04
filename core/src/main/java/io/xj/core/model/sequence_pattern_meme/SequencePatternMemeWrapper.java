// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.sequence_pattern_meme;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class SequencePatternMemeWrapper {
  private SequencePatternMeme sequencePatternMeme;

  public SequencePatternMeme getSequencePatternMeme() {
    return sequencePatternMeme;
  }

  public SequencePatternMemeWrapper setSequencePatternMeme(SequencePatternMeme sequencePatternMeme) {
    this.sequencePatternMeme = sequencePatternMeme;
    return this;
  }
}
