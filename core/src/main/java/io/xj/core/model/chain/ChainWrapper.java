// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class ChainWrapper {
  private Chain chain;

  public Chain getChain() {
    return chain;
  }

  public ChainWrapper setChain(Chain chain) {
    this.chain = chain;
    return this;
  }
}
