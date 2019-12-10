// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.fabricator;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class FabricatorModule extends AbstractModule {
  @Override
  protected void configure() {

    // Fabricator
    install(new FactoryModuleBuilder()
      .implement(Fabricator.class, FabricatorImpl.class)
      .build(FabricatorFactory.class));

    // Time Computer
    install(new FactoryModuleBuilder()
      .implement(TimeComputer.class, TimeComputerImpl.class)
      .build(TimeComputerFactory.class));

    // Segment Workbench
    install(new FactoryModuleBuilder()
      .implement(SegmentWorkbench.class, SegmentWorkbenchImpl.class)
      .build(SegmentWorkbenchFactory.class));

    // Segment Retrospective
    install(new FactoryModuleBuilder()
      .implement(SegmentRetrospective.class, SegmentRetrospectiveImpl.class)
      .build(SegmentRetrospectiveFactory.class));
  }
}
