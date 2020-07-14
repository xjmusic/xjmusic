// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.persistence;

import com.google.inject.AbstractModule;
import io.xj.lib.entity.EntityModule;

public class NexusEntityStoreModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new EntityModule());
    bind(NexusEntityStore.class).to(NexusEntityStoreImpl.class);
  }

}
