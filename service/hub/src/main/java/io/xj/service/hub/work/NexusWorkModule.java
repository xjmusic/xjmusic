// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.work;

import com.google.inject.AbstractModule;

public class NexusWorkModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(WorkManager.class).to(WorkManagerImpl.class);
  }
}
