// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.testing;

import com.google.inject.AbstractModule;
import io.xj.lib.pubsub.FileStoreModule;
import io.xj.lib.jsonapi.JsonApiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.service.hub.client.HubClientModule;
import io.xj.service.nexus.craft.CraftModule;
import io.xj.service.nexus.dao.NexusDAOModule;
import io.xj.service.nexus.dub.DubModule;
import io.xj.service.nexus.fabricator.NexusFabricatorModule;
import io.xj.service.nexus.persistence.NexusEntityStoreModule;
import io.xj.service.nexus.work.NexusWorkModule;

public class NexusIntegrationTestModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new HubClientModule());
    install(new MixerModule());
    install(new CraftModule());
    install(new NexusFabricatorModule());
    install(new NexusDAOModule());
    install(new DubModule());
    install(new NexusEntityStoreModule());
    install(new NexusWorkModule());
    install(new JsonApiModule());
    install(new FileStoreModule());
    bind(NexusIntegrationTestProvider.class).to(NexusIntegrationTestProviderImpl.class);
  }
}
