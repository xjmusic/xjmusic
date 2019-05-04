// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain_sequence;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class ChainSequenceWrapper {
  private ChainSequence chainSequence;

  public ChainSequence getChainSequence() {
    return chainSequence;
  }

  public ChainSequenceWrapper setChainSequence(ChainSequence chainSequence) {
    this.chainSequence = chainSequence;
    return this;
  }
}
