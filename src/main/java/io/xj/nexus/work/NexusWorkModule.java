// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.work;

import com.google.inject.AbstractModule;
import com.google.inject.assistedinject.FactoryModuleBuilder;
import io.xj.lib.telemetry.TelemetryModule;
import io.xj.nexus.craft.CraftModule;
import io.xj.nexus.fabricator.NexusFabricatorModule;
import io.xj.nexus.hub_client.client.HubClientModule;
import io.xj.nexus.dao.NexusDAOModule;
import io.xj.nexus.dub.DubModule;

public class NexusWorkModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new TelemetryModule());
    install(new CraftModule());
    install(new DubModule());
    install(new HubClientModule());
    install(new NexusDAOModule());
    install(new NexusFabricatorModule());
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
