// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub;

import com.google.inject.AbstractModule;
import io.xj.lib.mixer.MixerModule;
import io.xj.lib.rest_api.RestApiModule;
import io.xj.service.hub.access.HubAccessControlModule;
import io.xj.service.hub.cache.NexusCacheModule;
import io.xj.service.hub.dao.HubDaoModule;
import io.xj.service.hub.ingest.HubIngestModule;
import io.xj.service.hub.persistence.HubPersistenceModule;
import io.xj.service.hub.work.NexusWorkModule;

public class HubModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new HubAccessControlModule());
    install(new HubDaoModule());
    install(new HubIngestModule());
    install(new HubPersistenceModule());
    install(new MixerModule());
    install(new NexusCacheModule());
    install(new NexusWorkModule());
    install(new RestApiModule());
  }

}
