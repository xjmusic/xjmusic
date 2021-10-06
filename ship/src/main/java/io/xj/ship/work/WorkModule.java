// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.work;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.lib.json.JsonModule;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.lib.notification.NotificationModule;
import io.xj.lib.telemetry.TelemetryModule;
import io.xj.ship.persistence.PersistenceModule;

public class WorkModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new JsonModule());
    install(new JsonapiModule());
    install(new MixerModule());
    install(new NotificationModule());
    install(new PersistenceModule());
    install(new TelemetryModule());
    install(new FactoryModuleBuilder()
      .implement(ChainBoss.class, ChainBossImpl.class)
      .implement(Janitor.class, JanitorImpl.class)
      .implement(ChunkPrinter.class, ChunkPrinterImpl.class)
      .implement(SegmentLoader.class, SegmentLoaderImpl.class)
      .build(WorkFactory.class));

    bind(Work.class).to(WorkImpl.class);
  }
}
