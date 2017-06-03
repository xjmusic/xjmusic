// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.core.chain_gang.impl.link_pilot_work;

import io.outright.xj.core.chain_gang.ChainGangFactory;
import io.outright.xj.core.chain_gang.Leader;
import io.outright.xj.core.chain_gang.Follower;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class LinkPilotWorkFactoryModule extends AbstractModule {
  protected void configure() {
    installWorkFactory();
  }

  private void installWorkFactory() {
    install(new FactoryModuleBuilder()
      .implement(Leader.class, LinkPilotLeaderImpl.class)
      .implement(Follower.class, LinkPilotFollowerImpl.class)
      .build(ChainGangFactory.class));
  }

}
