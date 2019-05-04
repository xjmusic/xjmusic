// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain_library;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class ChainLibraryWrapper {
  private ChainLibrary chainLibrary;

  public ChainLibrary getChainLibrary() {
    return chainLibrary;
  }

  public ChainLibraryWrapper setChainLibrary(ChainLibrary chainLibrary) {
    this.chainLibrary = chainLibrary;
    return this;
  }
}
