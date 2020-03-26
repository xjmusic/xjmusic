// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.generation;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class GenerationModule extends AbstractModule {

  protected void configure() {
    install(new FactoryModuleBuilder()
      .implement(Generation.class, LibrarySupersequenceGenerationImpl.class)
      .implement(LibrarySupersequenceGeneration.class, LibrarySupersequenceGenerationImpl.class)
      .build(GenerationFactory.class));
  }
}
