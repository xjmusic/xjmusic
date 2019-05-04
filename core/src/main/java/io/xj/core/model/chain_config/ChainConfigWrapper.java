// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.model.chain_config;

/**
 Wrapper for use as POJO for decoding messages received by JAX-RS resources
 a.k.a. JSON input will be stored into an entity inside this object
 */
public class ChainConfigWrapper {
  private ChainConfig chainConfig;

  public ChainConfig getChainConfig() {
    return chainConfig;
  }

  public ChainConfigWrapper setChainConfig(ChainConfig chainConfig) {
    this.chainConfig = chainConfig;
    return this;
  }
}
