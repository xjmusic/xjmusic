// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.testing;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.NexusException;
import io.xj.nexus.work.NexusWork;
import io.xj.nexus.NexusApp;
import io.xj.nexus.persistence.NexusEntityStore;
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
          NexusEntityStore nexusEntityStore
  ) {
    this.workManager = nexusWork;
    this.store = nexusEntityStore;

    // Build the Nexus REST API payload topology
    NexusApp.buildApiTopology(entityFactory);

    // Requires that a configuration has been bound
    config.getString("app.name");

    // Begin database prep
    log.debug("Will prepare integration database.");

    // Prepared
    log.debug("Did open master connection and prepare integration database.");
  }

  @Override
  public void setUp() throws NexusException {
    store.deleteAll();
  }

  @Override
  public <N> N put(N entity) throws NexusException {
    store.put(entity);
    return entity;
  }

  @Override
  public <E, I> E put(E entity, Collection<I> included) throws NexusException {
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
