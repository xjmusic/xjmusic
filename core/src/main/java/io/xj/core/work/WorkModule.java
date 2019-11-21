// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.work;

import com.google.inject.AbstractModule;
import io.xj.core.work.impl.WorkManagerImpl;

public class WorkModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(WorkManager.class).to(WorkManagerImpl.class);
  }
}
