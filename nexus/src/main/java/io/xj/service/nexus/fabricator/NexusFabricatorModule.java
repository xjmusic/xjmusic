// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.fabricator;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class NexusFabricatorModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new FactoryModuleBuilder()
      .implement(Fabricator.class, FabricatorImpl.class)
      .implement(SegmentWorkbench.class, SegmentWorkbenchImpl.class)
      .implement(SegmentRetrospective.class, SegmentRetrospectiveImpl.class)
      .implement(TimeComputer.class, TimeComputerImpl.class)
      .build(FabricatorFactory.class));
  }
}
