// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.work;

import com.google.inject.AbstractModule;
import io.xj.lib.telemetry.TelemetryModule;
import io.xj.nexus.craft.CraftModule;
import io.xj.nexus.dub.DubModule;
import io.xj.nexus.fabricator.NexusFabricatorModule;
import io.xj.hub.client.HubClientModule;
import io.xj.nexus.persistence.NexusPersistenceModule;

public class NexusWorkModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new TelemetryModule());
    install(new CraftModule());
    install(new DubModule());
    install(new HubClientModule());
    install(new NexusPersistenceModule());
    install(new NexusFabricatorModule());
    bind(NexusWork.class).to(NexusWorkImpl.class);
  }
}
