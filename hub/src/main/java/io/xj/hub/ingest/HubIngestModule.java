// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.ingest;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.lib.entity.EntityModule;
import io.xj.lib.json.JsonModule;

public class HubIngestModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new EntityModule());
    install(new JsonModule());
    install(new FactoryModuleBuilder()
      .implement(HubIngest.class, HubIngestImpl.class)
      .build(HubIngestFactory.class));
  }
}
