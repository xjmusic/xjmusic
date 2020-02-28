// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.ingest;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

public class IngestModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(IngestCacheProvider.class).to(IngestCacheProviderImpl.class);
    install(new FactoryModuleBuilder()
      .implement(Ingest.class, IngestImpl.class)
      .build(IngestFactory.class));
  }
}
