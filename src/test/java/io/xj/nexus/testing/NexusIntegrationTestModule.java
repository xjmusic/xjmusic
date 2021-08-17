// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.testing;

import com.google.inject.AbstractModule;
import io.xj.lib.filestore.FileStoreModule;
import io.xj.lib.jsonapi.JsonapiModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.nexus.craft.CraftModule;
import io.xj.nexus.dao.NexusDAOModule;
import io.xj.nexus.dub.DubModule;
import io.xj.nexus.fabricator.NexusFabricatorModule;
import io.xj.nexus.hub_client.client.HubClientModule;
import io.xj.nexus.persistence.NexusEntityStoreModule;
import io.xj.nexus.work.NexusWorkModule;

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
    install(new JsonapiModule());
    install(new FileStoreModule());
    bind(NexusIntegrationTestProvider.class).to(NexusIntegrationTestProviderImpl.class);
  }
}
