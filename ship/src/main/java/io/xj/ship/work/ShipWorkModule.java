// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

import com.google.inject.AbstractModule;
import io.xj.lib.json.JsonModule;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.lib.notification.NotificationModule;
import io.xj.lib.telemetry.TelemetryModule;
import io.xj.ship.broadcast.BroadcastModule;
import io.xj.ship.source.SourceModule;

public class ShipWorkModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new SourceModule());
    install(new BroadcastModule());
    bind(Janitor.class).to(JanitorImpl.class);
    bind(ShipWork.class).to(ShipWorkImpl.class);

    install(new JsonModule());
    install(new JsonapiModule());
    install(new MixerModule());
    install(new NotificationModule());
    install(new TelemetryModule());
  }
}
