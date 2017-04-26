// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.core.model.chain_config;

import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.model.EntityWrapper;

public class ChainConfigWrapper extends EntityWrapper {

  // Chain
  private ChainConfig chainConfig;

  public ChainConfig getChainConfig() {
    return chainConfig;
  }

  public ChainConfigWrapper setChainConfig(ChainConfig chainConfig) {
    this.chainConfig = chainConfig;
    return this;
  }

  /**
   * Validate data.
   *
   * @throws BusinessException if invalid.
   */
  public ChainConfig validate() throws BusinessException {
    if (this.chainConfig == null) {
      throw new BusinessException("Chain config is required.");
    }
    this.chainConfig.validate();
    return this.chainConfig;
  }

}
