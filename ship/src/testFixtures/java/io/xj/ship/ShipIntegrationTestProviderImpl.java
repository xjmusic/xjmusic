// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship;

import io.xj.lib.entity.EntityFactory;
import io.xj.nexus.NexusException;
import io.xj.nexus.NexusTopology;
import io.xj.nexus.persistence.NexusEntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class ShipIntegrationTestProviderImpl implements ShipIntegrationTestProvider {
  final Logger LOG = LoggerFactory.getLogger(ShipIntegrationTestProviderImpl.class);
  private final NexusEntityStore store;

  /**
   Since this class is a singleton, the process here in its constructor
   will happen only once for a whole test suite
   */
  ShipIntegrationTestProviderImpl(
    EntityFactory entityFactory,
    NexusEntityStore nexusEntityStore
  ) {
    this.store = nexusEntityStore;

    // Build the Ship REST API payload topology
    NexusTopology.buildNexusApiTopology(entityFactory);

    // Begin database prep
    LOG.debug("Will prepare integration database.");

    // Prepared
    LOG.debug("Did open master connection and prepare integration database.");
  }

  @Override
  public void setUp() throws ShipException {
    try {
      store.deleteAll();
    } catch (NexusException e) {
      throw new ShipException(e);
    }
  }

  @Override
  public <N> N put(N entity) throws ShipException {
    try {
      return store.put(entity);
    } catch (NexusException e) {
      throw new ShipException(e);
    }
  }

  @Override
  public <E, I> E put(E entity, Collection<I> included) throws ShipException {
    try {
      store.put(entity);
      store.putAll(included);
      return entity;
    } catch (NexusException e) {
      throw new ShipException(e);
    }
  }

  @Override
  public void tearDown() {
  }

}
