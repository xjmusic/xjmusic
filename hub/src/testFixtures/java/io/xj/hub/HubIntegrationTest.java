// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.xj.hub.access.GoogleProvider;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessTokenGenerator;
import io.xj.hub.persistence.HubMigration;
import io.xj.hub.persistence.HubPersistenceException;
import io.xj.hub.persistence.HubPersistenceServiceImpl;
import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.persistence.kv.HubKvStoreProvider;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.entity.EntityStore;
import io.xj.lib.json.ApiUrlProvider;
import io.xj.lib.json.JsonProvider;
import io.xj.lib.json.JsonProviderImpl;
import io.xj.lib.jsonapi.JsonapiPayloadFactory;
import io.xj.lib.jsonapi.JsonapiResponseProvider;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

/**
 * An Integration test needs to:
 * 1. create injector with environment bound
 * 2. inject IntegrationTestProvider
 * AND
 * 3. call integrationTestProvider.shutdown()
 */
public class HubIntegrationTest extends HubPersistenceServiceImpl {
  final Logger log = LoggerFactory.getLogger(HubIntegrationTest.class);
  private final ApiUrlProvider apiUrlProvider;
  private final HubKvStoreProvider kvStoreProvider;
  private final GoogleProvider googleProvider;
  private final HubAccessTokenGenerator accessTokenGenerator;
  private final JsonapiPayloadFactory jsonapiPayloadFactory;
  private final JsonapiResponseProvider jsonapiResponseProvider;
  private final EntityStore entityStore;
  private final JsonProvider jsonProvider;

  /**
   * This should happen only once for a whole test suite
   */
  public HubIntegrationTest(
    ApiUrlProvider apiUrlProvider,
    EntityFactory entityFactory,
    GoogleProvider googleProvider,
    HubAccessTokenGenerator accessTokenGenerator,
    HubKvStoreProvider kvStoreProvider,
    HubMigration migration,
    HubSqlStoreProvider sqlStoreProvider,
    JsonapiResponseProvider jsonapiResponseProvider,
    JsonapiPayloadFactory jsonapiPayloadFactory
  ) {
    super(entityFactory, sqlStoreProvider);
    this.apiUrlProvider = apiUrlProvider;
    this.entityStore = entityFactory.createEntityStore();
    this.googleProvider = googleProvider;
    this.accessTokenGenerator = accessTokenGenerator;
    this.jsonProvider = new JsonProviderImpl();
    this.jsonapiPayloadFactory = jsonapiPayloadFactory;
    this.jsonapiResponseProvider = jsonapiResponseProvider;
    this.kvStoreProvider = kvStoreProvider;

    // Build the Hub REST API payload topology
    HubTopology.buildHubApiTopology(entityFactory);

    // Begin database prep
    log.debug("Will prepare integration database.");

    // Migrate the test database
    try {
      migration.migrate();
    } catch (HubPersistenceException e) {
      log.error("CoreException", e);
      System.exit(1);
    }

    // Prepared
    log.debug("Did open master connection and prepare integration database.");
  }

  /**
   * Reset sessions
   */
  public void resetSessions() {
    kvStoreProvider.clear();
  }

  /**
   * Runs on program exit
   */
  public void shutdown() {
//    try {
//      sqlStoreProvider.shutdown();
//    } catch (Exception e) {
//      log.error("Failed to shutdown SQL connection", e);
//    }
//    log.debug("Did close master connection to integration database.");
  }

  /**
   * Reset the database before an integration test.
   */
  public void reset() throws HubException {
    try {
      for (Table<?> table : Lists.reverse(ImmutableList.copyOf(tablesInSchemaConstructionOrder.values())))
        reset(table);

      // Finally, all sessions
      resetSessions();

    } catch (Exception e) {
      log.error(e.getClass().getName(), e);
      throw new HubException(e.getClass().getName(), e);
    }
    log.debug("Did delete all records create integration database.");
  }

  /**
   * Delete all of a given table, if the count of records is > 0
   *
   * @param table to delete all of
   */
  public void reset(Table<?> table) {
    DSLContext db = sqlStoreProvider.getDSL();
    try (var selectCount = db.selectCount()) {
      var count = selectCount.from(table).fetchOne(0, int.class);
      if (Objects.nonNull(count) && 0 < count)
        try (var d = db.deleteFrom(table)) {
          d.execute();
        }
    }
  }

  /**
   * Insert entity to database
   *
   * @param entity to insert
   * @return the same entity (for chaining methods)
   */
  public <N> N insert(N entity) throws HubException {
    try {
      return insert(sqlStoreProvider.getDSL(), entity);
    } catch (Exception e) {
      throw new HubException(e);
    }
  }

  /**
   * Get the master connection to integration database
   *
   * @return DSL Context
   */
  public DSLContext getDSL() {
    return sqlStoreProvider.getDSL();
  }

  public EntityFactory getEntityFactory() {
    return entityFactory;
  }

  public EntityStore getEntityStore() {
    return entityStore;
  }

  public GoogleProvider getGoogleProvider() {
    return googleProvider;
  }

  public JsonapiResponseProvider getHttpResponseProvider() {
    return jsonapiResponseProvider;
  }

  public HubAccessTokenGenerator getAccessTokenGenerator() {
    return accessTokenGenerator;
  }

  public JsonProvider getJsonProvider() {
    return jsonProvider;
  }

  public JsonapiPayloadFactory getJsonapiPayloadFactory() {
    return jsonapiPayloadFactory;
  }

  public HubKvStoreProvider getKvStoreProvider() {
    return kvStoreProvider;
  }

  public HubSqlStoreProvider getSqlStoreProvider() {
    return sqlStoreProvider;
  }

  public <E> E create(HubAccess ignoredAccess, E ignoredEntity) {
    return null;
  }

  public void destroy(HubAccess ignoredAccess, UUID ignoredId) {

  }

  public <E> Collection<E> readMany(HubAccess ignoredAccess, Collection<UUID> ignoredParentIds) {
    return null;
  }

  public <E> E readOne(HubAccess ignoredAccess, UUID ignoredId) {
    return null;
  }

  public <E> E update(HubAccess ignoredAccess, UUID ignoredId, E entity) {
    // no op
    return entity;
  }
  public ApiUrlProvider getApiUrlProvider() {
    return apiUrlProvider;
  }
}
