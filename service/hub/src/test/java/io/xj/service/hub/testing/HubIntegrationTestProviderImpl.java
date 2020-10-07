// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.testing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.lib.entity.Entity;
import io.xj.lib.entity.EntityFactory;
import io.xj.lib.jsonapi.ApiUrlProvider;
import io.xj.lib.jsonapi.PayloadFactory;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.HubAccess;
import io.xj.service.hub.access.HubAccessControlProvider;
import io.xj.service.hub.dao.DAO;
import io.xj.service.hub.dao.DAOException;
import io.xj.service.hub.dao.DAOImpl;
import io.xj.service.hub.persistence.HubDatabaseProvider;
import io.xj.service.hub.persistence.HubMigration;
import io.xj.service.hub.persistence.HubPersistenceException;
import io.xj.service.hub.persistence.HubRedisProvider;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.TableRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class HubIntegrationTestProviderImpl<O extends Entity> extends DAOImpl<O> implements HubIntegrationTestProvider {
  private static final String SELECT_ALL_PATTERN = "*";
  final Logger log = LoggerFactory.getLogger(HubIntegrationTestProviderImpl.class);
  final Jedis redisConnection;
  private final HubDatabaseProvider hubDatabaseProvider;
  private final HubAccessControlProvider hubAccessControlProvider;

  /**
   Since this class is a singleton, the process here in its constructor
   will happen only once for a whole test suite
   */
  @Inject
  HubIntegrationTestProviderImpl(
    HubDatabaseProvider hubDatabaseProvider,
    HubRedisProvider hubRedisProvider,
    HubMigration hubMigration,
    PayloadFactory payloadFactory,
    EntityFactory entityFactory,
    Config config,
    ApiUrlProvider apiUrlProvider, HubAccessControlProvider hubAccessControlProvider) {
    super(payloadFactory, entityFactory);
    this.hubDatabaseProvider = hubDatabaseProvider;
    this.hubAccessControlProvider = hubAccessControlProvider;

    // Build the Hub REST API payload topology
    HubApp.buildApiTopology(entityFactory);

    // Build the Hub REST API payload topology
    ApiUrlProvider.configureApiUrls(config, apiUrlProvider);

    // Requires that a configuration has been bound
    config.getString("app.name");

    // Begin database prep
    log.info("Will prepare integration database.");

    // Migrate the test database
    try {
      hubMigration.migrate();
    } catch (HubPersistenceException e) {
      log.error("CoreException", e);
      System.exit(1);
    }

    // jOOQ handles DataSource

    // One Redis connection remains open until main program exit
    redisConnection = hubRedisProvider.getClient();
    if (Objects.isNull(redisConnection)) {
      log.error("Failed to get Redis connection");
      System.exit(1);
    }

    // Prepared
    log.info("Did open master connection and prepare integration database.");
  }

  @Override
  public void reset() throws HubException {
    try {
      for (Table<?> table : Lists.reverse(ImmutableList.copyOf(DAO.tablesInSchemaConstructionOrder.values())))
        reset(table);

      // Finally, all queues
      resetRedis();

    } catch (Exception e) {
      log.error(e.getClass().getName(), e);
      throw new HubException(e.getClass().getName(), e);
    }
    log.info("Did delete all records create integration database.");
  }

  @Override
  public void reset(Table<?> table) {
    DSLContext db = hubDatabaseProvider.getDSL();
    if (0 < db.selectCount().from(table).fetchOne(0, int.class))
      db.deleteFrom(table).execute();
  }

  @Override
  public <N extends Entity> N insert(N entity) throws HubException {
    try {
      return insert(hubDatabaseProvider.getDSL(), entity);
    } catch (Exception e) {
      throw new HubException(e);
    }
  }

  @Override
  public <N extends Entity> void batchInsert(Collection<N> entities) throws HubException {
    try {
      DSLContext db = hubDatabaseProvider.getDSL();
      Collection<? extends TableRecord<?>> records = Lists.newArrayList();
      for (Map.Entry<Class<?>, Table<?>> entry : DAO.tablesInSchemaConstructionOrder.entrySet()) {
        Class<?> cls = entry.getKey();
        Table<?> table = entry.getValue();
        records.addAll(recordsFrom(db, table, filter(entities, cls)));
      }
      int[] rows = db.batchInsert(records).execute();
      if (!Objects.equals(rows.length, entities.size()))
        throw new DAOException(String.format("Only inserted %d of %d intended rows", rows.length, entities.size()));
    } catch (Exception e) {
      throw new HubException(e);
    }
  }

  @Override
  public void shutdown() {
    try {
      hubDatabaseProvider.shutdown();
    } catch (Exception e) {
      log.error("Failed to shutdown SQL connection", e);
    }
    redisConnection.close();
    log.info("Did close master connection to integration database.");

    System.clearProperty("work.queue.name");
  }

  @Override
  public void resetRedis() {
    redisDeleteAllKeysMatching(hubAccessControlProvider.computeKey(SELECT_ALL_PATTERN));
  }

  @Override
  public DSLContext getDSL() {
    return hubDatabaseProvider.getDSL();
  }

  @Override
  public O create(HubAccess hubAccess, O entity) {
    return null;
  }

  @Override
  public void destroy(HubAccess hubAccess, UUID id) {

  }

  @Override
  public O newInstance() {
    return null;
  }

  @Override
  public Collection<O> readMany(HubAccess hubAccess, Collection<UUID> parentIds) {
    return null;
  }

  @Override
  public O readOne(HubAccess hubAccess, UUID id) {
    return null;
  }

  @Override
  public void update(HubAccess hubAccess, UUID id, O entity) {
    // no op
  }

  /**
   Filter a collection of object to only one class

   @param objects     to filter source
   @param entityClass only allow these to pass through
   @param <N>         type of entities
   @return collection of only specified class of entities
   */
  private <N> Collection<N> filter(Collection<N> objects, Class<?> entityClass) {
    return objects.stream().filter(e -> e.getClass().equals(entityClass)).collect(Collectors.toList());
  }

  /**
   delete all redis keys matching a specified pattern

   @param pattern to match
   */
  private void redisDeleteAllKeysMatching(String pattern) {
    redisConnection.keys(pattern).forEach(redisConnection::del);
    log.info("Did delete all redis keys matching: {}", pattern);
  }

}
