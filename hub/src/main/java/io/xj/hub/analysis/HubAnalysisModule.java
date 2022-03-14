// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.analysis;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.lib.entity.EntityModule;
import io.xj.lib.json.JsonModule;

/**
 Template content Analysis #161199945
 */
public class HubAnalysisModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new EntityModule());
    install(new JsonModule());
    install(new FactoryModuleBuilder()
      .implement(HubAnalysis.class, HubAnalysisImpl.class)
      .build(HubAnalysisFactory.class));
  }
}
