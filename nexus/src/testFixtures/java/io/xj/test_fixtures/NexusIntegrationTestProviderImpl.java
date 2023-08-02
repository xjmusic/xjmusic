// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.test_fixtures;

import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.persistence.NexusEntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class NexusIntegrationTestProviderImpl implements NexusIntegrationTestProvider {
  final Logger LOG = LoggerFactory.getLogger(NexusIntegrationTestProviderImpl.class);
  final NexusEntityStore store;

  /**
   * Since this class is a singleton, the process here in its constructor
   * will happen only once for a whole test suite
   */
  NexusIntegrationTestProviderImpl(
    EntityFactory entityFactory,
    NexusEntityStore nexusEntityStore
  ) {
    this.store = nexusEntityStore;

    // Build the Nexus REST API payload topology
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Begin database prep
    LOG.debug("Will prepare integration database.");

    // Prepared
    LOG.debug("Did open master connection and prepare integration database.");
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
  }

}
