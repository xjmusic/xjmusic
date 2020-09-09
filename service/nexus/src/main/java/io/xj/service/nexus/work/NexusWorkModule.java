// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.work;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.lib.telemetry.TelemetryModule;
import io.xj.service.hub.client.HubClientModule;
import io.xj.service.nexus.craft.CraftModule;
import io.xj.service.nexus.dao.NexusDAOModule;
import io.xj.service.nexus.dub.DubModule;
import io.xj.service.nexus.fabricator.NexusFabricatorModule;

public class NexusWorkModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new CraftModule());
    install(new DubModule());
    install(new HubClientModule());
    install(new NexusDAOModule());
    install(new NexusFabricatorModule());
    install(new TelemetryModule());
    bind(NexusWork.class).to(NexusWorkImpl.class);
    install(new FactoryModuleBuilder()
      .implement(BossWorker.class, BossWorkerImpl.class)
      .implement(JanitorWorker.class, JanitorWorkerImpl.class)
      .implement(MedicWorker.class, MedicWorkerImpl.class)
      .implement(ChainWorker.class, ChainWorkerImpl.class)
      .implement(FabricatorWorker.class, FabricatorWorkerImpl.class)
      .build(WorkerFactory.class));
  }
}
