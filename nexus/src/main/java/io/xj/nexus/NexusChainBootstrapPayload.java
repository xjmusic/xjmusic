// Copyright (c) 1999-2021, XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.nexus;

import io.xj.api.Chain;
import io.xj.api.TemplateBinding;

import java.util.Collection;

/**
 Deserialize JSON to bootstrap a Chain with specified Bindings
 */
public class NexusChainBootstrapPayload {
  Chain chain;
  Collection<TemplateBinding> chainBindings;

  public Chain getChain() {
    return chain;
  }

  public void setChain(Chain chain) {
    this.chain = chain;
  }

  public Collection<TemplateBinding> getChainBindings() {
    return chainBindings;
  }

  public void setChainBindings(Collection<TemplateBinding> chainBindings) {
    this.chainBindings = chainBindings;
  }
}
