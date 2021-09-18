//  Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.lib.entity.EntityFactory;
import io.xj.ship.persistence.ShipEntityStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

@Singleton
public class ShipIntegrationTestProviderImpl implements ShipIntegrationTestProvider {
  final Logger LOG = LoggerFactory.getLogger(ShipIntegrationTestProviderImpl.class);
  private final ShipEntityStore store;

  /**
   Since this class is a singleton, the process here in its constructor
   will happen only once for a whole test suite
   */
  @Inject
  ShipIntegrationTestProviderImpl(
    EntityFactory entityFactory,
    Config config,
    ShipEntityStore shipEntityStore
  ) {
    this.store = shipEntityStore;

    // Build the Ship REST API payload topology
    ShipTopology.buildShipApiTopology(entityFactory);

    // Requires that a configuration has been bound
    config.getString("app.name");

    // Begin database prep
    LOG.debug("Will prepare integration database.");

    // Prepared
    LOG.debug("Did open master connection and prepare integration database.");
  }

  @Override
  public void setUp() throws ShipException {
    store.deleteAll();
  }

  @Override
  public <N> N put(N entity) throws ShipException {
    store.put(entity);
    return entity;
  }

  @Override
  public <E, I> E put(E entity, Collection<I> included) throws ShipException {
    store.put(entity);
    store.putAll(included);
    return entity;
  }

  @Override
  public void tearDown() {
    System.clearProperty("work.queue.name");
  }

}
