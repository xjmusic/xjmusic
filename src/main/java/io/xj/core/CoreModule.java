// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core;

import com.google.inject.AbstractModule;
import io.xj.core.access.AccessControlModule;
import io.xj.core.heartbeat.HeartbeatModule;
import io.xj.core.cache.CacheModule;
import io.xj.core.dao.DAOModule;
import io.xj.core.external.ExternalResourceModule;
import io.xj.core.fabricator.FabricatorModule;
import io.xj.core.ingest.IngestModule;
import io.xj.core.persistence.PersistenceModule;
import io.xj.core.testing.IntegrationTestModule;
import io.xj.core.transport.TransportModule;
import io.xj.core.work.WorkModule;
import io.xj.mixer.MixerModule;

public class CoreModule extends AbstractModule {
  @Override
  protected void configure() {
    install(new AccessControlModule());
    install(new HeartbeatModule());
    install(new CacheModule());
    install(new DAOModule());
    install(new ExternalResourceModule());
    install(new FabricatorModule());
    install(new IngestModule());
    install(new MixerModule());
    install(new PersistenceModule());
    install(new IntegrationTestModule());
    install(new TransportModule());
    install(new WorkModule());
  }

}
