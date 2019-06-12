//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.core.model.chain.impl;

import com.google.common.collect.Lists;
import io.xj.core.model.chain.Chain;
import io.xj.core.model.chain.ChainType;
import io.xj.core.model.chain.sub.ChainBinding;
import io.xj.core.model.chain.sub.ChainConfig;
import io.xj.core.model.entity.SuperEntityContent;

import java.util.Collection;

/**
 [#166743281] Chain handles all of its own binding + config entities
 */
public class ChainContent implements SuperEntityContent {
  private final Collection<ChainConfig> configs = Lists.newArrayList();
  private final Collection<ChainBinding> bindings = Lists.newArrayList();
  private ChainType type;

  /**
   Create a instance of ChainContent, used for transporting the content of a chain

   @param chain to get content of
   @return chain content
   */
  public static ChainContent of(Chain chain) {
    ChainContent content = new ChainContent();
    content.setConfigs(chain.getConfigs());
    content.setBindings(chain.getBindings());
    return content;
  }

  /**
   Get Chain Configs

   @return Chain Configs
   */
  public Collection<ChainConfig> getConfigs() {
    return configs;
  }

  /**
   Get Chain Bindings

   @return Chain Bindings
   */
  public Collection<ChainBinding> getBindings() {
    return bindings;
  }

  /**
   Get Type

   @return Type
   */
  public ChainType getType() {
    return type;
  }

  /**
   Set Chain Configs

   @param configs to set
   */
  public void setConfigs(Collection<ChainConfig> configs) {
    this.configs.clear();
    this.configs.addAll(configs);
  }

  /**
   Set Chain Bindings

   @param bindings to set
   */
  public void setBindings(Collection<ChainBinding> bindings) {
    this.bindings.clear();
    this.bindings.addAll(bindings);
  }

  /**
   Set Type

   @param type to set
   */
  public void setType(String type) {
    this.type = ChainType.valueOf(type);
  }

}
