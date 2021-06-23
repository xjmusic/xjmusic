// Copyright (c) 1999-2021, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus;

import io.xj.Chain;
import io.xj.ChainBinding;

import java.util.Collection;

/**
 Deserialize JSON to bootstrap a Chain with specified Bindings
 */
public class NexusChainBootstrapPayload {
  Chain chain;
  Collection<ChainBinding> chainBindings;

  public Chain getChain() {
    return chain;
  }

  public void setChain(Chain chain) {
    this.chain = chain;
  }

  public Collection<ChainBinding> getChainBindings() {
    return chainBindings;
  }

  public void setChainBindings(Collection<ChainBinding> chainBindings) {
    this.chainBindings = chainBindings;
  }
}
