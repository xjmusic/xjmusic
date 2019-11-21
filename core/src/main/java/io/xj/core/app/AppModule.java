// Copyright (c) 2020, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.app;

import com.google.inject.AbstractModule;
import io.xj.core.app.impl.AppImpl;
import io.xj.core.app.impl.HealthImpl;
import io.xj.core.app.impl.HeartbeatImpl;

public class AppModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(App.class).to(AppImpl.class);
    bind(Health.class).to(HealthImpl.class);
    bind(Heartbeat.class).to(HeartbeatImpl.class);
  }
}
