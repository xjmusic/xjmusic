// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

import com.google.inject.AbstractModule;
import io.xj.lib.notification.NotificationModule;
import io.xj.lib.telemetry.TelemetryModule;

public class ShipWorkModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new TelemetryModule());
    install(new NotificationModule());
    bind(ShipWork.class).to(ShipWorkImpl.class);
    bind(ShipWorkChainManager.class).to(ShipWorkChainManagerImpl.class);
  }
}
