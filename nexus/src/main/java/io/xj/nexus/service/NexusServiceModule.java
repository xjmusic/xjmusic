// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.service;

import com.google.inject.AbstractModule;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.notification.NotificationModule;
import io.xj.nexus.persistence.NexusEntityStoreModule;

public class NexusServiceModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new NexusEntityStoreModule());
    install(new FileStoreModule());
    install(new NotificationModule());
    bind(ChainService.class).to(ChainServiceImpl.class);
    bind(SegmentService.class).to(SegmentServiceImpl.class);
  }

}
