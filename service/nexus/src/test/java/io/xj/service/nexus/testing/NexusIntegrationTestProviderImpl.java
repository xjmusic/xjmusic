// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.testing;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.lib.entity.Entity;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.ApiUrlProvider;
import io.xj.service.nexus.NexusApp;
import io.xj.service.nexus.persistence.NexusEntityStore;
import io.xj.lib.entity.EntityStoreException;
import io.xj.service.nexus.work.NexusWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@Singleton
public class NexusIntegrationTestProviderImpl implements NexusIntegrationTestProvider {
  final Logger log = LoggerFactory.getLogger(NexusIntegrationTestProviderImpl.class);
  private final NexusWork workManager;
  private final NexusEntityStore store;

  /**
   Since this class is a singleton, the process here in its constructor
   will happen only once for a whole test suite
   */
  @Inject
  NexusIntegrationTestProviderImpl(
    NexusWork nexusWork,
    EntityFactory entityFactory,
    Config config,
    ApiUrlProvider apiUrlProvider,
    NexusEntityStore nexusEntityStore
  ) {
    this.workManager = nexusWork;
    this.store = nexusEntityStore;

    // Build the Nexus REST API payload topology
    NexusApp.buildApiTopology(entityFactory);

    // Build the Nexus REST API payload topology
    ApiUrlProvider.configureApiUrls(config, apiUrlProvider);

    // Requires that a configuration has been bound
    config.getString("app.name");

    // Begin database prep
    log.info("Will prepare integration database.");

    // Prepared
    log.info("Did open master connection and prepare integration database.");
  }

  @Override
  public void setUp() {
    store.deleteAll();
  }

  @Override
  public <N extends Entity> N put(N entity) throws EntityStoreException {
    store.put(entity);
    return entity;
  }

  @Override
  public <E extends Entity, I extends Entity> E put(E entity, Collection<I> included) throws EntityStoreException {
    store.put(entity);
    store.putAll(included);
    return entity;
  }

  @Override
  public void tearDown() {
    workManager.finish();
    System.clearProperty("work.queue.name");
  }

}
