// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.work;

import com.google.inject.AbstractModule;

public class WorkModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(WorkManager.class).to(WorkManagerImpl.class);
  }
}
