// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.fabricator;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.lib.http.HttpClientModule;

public class NexusFabricatorModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new FactoryModuleBuilder()
      .implement(Fabricator.class, FabricatorImpl.class)
      .implement(SegmentWorkbench.class, SegmentWorkbenchImpl.class)
      .implement(SegmentRetrospective.class, SegmentRetrospectiveImpl.class)
      .build(FabricatorFactory.class));
    install(new HttpClientModule());
  }
}
