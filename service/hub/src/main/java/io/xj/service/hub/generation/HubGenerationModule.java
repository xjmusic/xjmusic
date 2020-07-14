// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.generation;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class HubGenerationModule extends AbstractModule {

  protected void configure() {
    install(new FactoryModuleBuilder()
      .implement(HubGeneration.class, HubGenerationLibrarySupersequenceImpl.class)
      .implement(HubGenerationLibrarySupersequence.class, HubGenerationLibrarySupersequenceImpl.class)
      .build(HubGenerationFactory.class));
  }
}
