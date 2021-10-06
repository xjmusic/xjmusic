// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.persistence;

import com.google.inject.AbstractModule;
import io.xj.lib.entity.EntityModule;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.notification.NotificationModule;

public class NexusPersistenceModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ChainManager.class).to(ChainManagerImpl.class);
    bind(NexusEntityStore.class).to(NexusEntityStoreImpl.class);
    bind(SegmentManager.class).to(SegmentManagerImpl.class);
    install(new EntityModule());
    install(new FileStoreModule());
    install(new NotificationModule());
  }
}
