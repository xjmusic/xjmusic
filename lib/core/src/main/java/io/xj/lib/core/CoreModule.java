// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core;

import com.google.inject.AbstractModule;
import io.xj.lib.core.access.AccessControlModule;
import io.xj.lib.core.heartbeat.HeartbeatModule;
import io.xj.lib.core.cache.CacheModule;
import io.xj.lib.core.dao.DAOModule;
import io.xj.lib.core.external.ExternalResourceModule;
import io.xj.lib.core.fabricator.FabricatorModule;
import io.xj.lib.core.ingest.IngestModule;
import io.xj.lib.core.persistence.PersistenceModule;
import io.xj.lib.core.testing.IntegrationTestModule;
import io.xj.lib.core.transport.TransportModule;
import io.xj.lib.core.work.WorkModule;
import io.xj.lib.mixer.MixerModule;

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
