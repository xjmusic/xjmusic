// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.ingest;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;

import io.xj.core.ingest.cache.IngestCacheProvider;
import io.xj.core.ingest.cache.impl.IngestCacheProviderImpl;
import io.xj.core.ingest.impl.IngestImpl;

public class IngestModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(IngestCacheProvider.class).to(IngestCacheProviderImpl.class);
    install(new FactoryModuleBuilder()
      .implement(Ingest.class, IngestImpl.class)
      .build(IngestFactory.class));
  }
}
