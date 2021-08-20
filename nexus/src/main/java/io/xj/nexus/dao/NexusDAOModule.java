// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.dao;

import com.google.inject.AbstractModule;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.notification.NotificationModule;
import io.xj.nexus.persistence.NexusEntityStoreModule;

public class NexusDAOModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new NexusEntityStoreModule());
    install(new FileStoreModule());
    install(new NotificationModule());
    bind(ChainDAO.class).to(ChainDAOImpl.class);
    bind(SegmentDAO.class).to(SegmentDAOImpl.class);
  }

}
