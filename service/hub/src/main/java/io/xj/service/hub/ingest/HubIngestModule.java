// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.ingest;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.lib.entity.EntityModule;

public class HubIngestModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(HubIngestCacheProvider.class).to(HubIngestCacheProviderImpl.class);
    install(new EntityModule());
    install(new FactoryModuleBuilder()
      .implement(HubIngest.class, HubIngestImpl.class)
      .build(HubIngestFactory.class));
  }
}
