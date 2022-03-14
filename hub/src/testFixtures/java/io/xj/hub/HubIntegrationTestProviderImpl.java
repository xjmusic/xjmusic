// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.

package io.xj.hub;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.xj.hub.access.HubAccess;
import io.xj.hub.access.HubAccessControlProvider;
import io.xj.hub.manager.ManagerException;
import io.xj.hub.persistence.*;
import io.xj.lib.entity.EntityFactory;
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
public class HubIntegrationTestProviderImpl<O> extends HubPersistenceServiceImpl<O> implements HubIntegrationTestProvider {
  private static final String SELECT_ALL_PATTERN = "*";
  final Logger log = LoggerFactory.getLogger(HubIntegrationTestProviderImpl.class);
  final Jedis redisConnection;
  private final HubAccessControlProvider hubAccessControlProvider;

  /**
   Since this class is a singleton, the process here in its constructor
   will happen only once for a whole test suite
   */
  @Inject
  HubIntegrationTestProviderImpl(
    HubDatabaseProvider dbProvider,
    HubRedisProvider hubRedisProvider,
    HubMigration hubMigration,
    EntityFactory entityFactory,
    HubAccessControlProvider hubAccessControlProvider
  ) {
    super(entityFactory, dbProvider);
    this.hubAccessControlProvider = hubAccessControlProvider;

    // Build the Hub REST API payload topology
    HubTopology.buildHubApiTopology(entityFactory);

    // Begin database prep
    log.debug("Will prepare integration database.");

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
    log.debug("Did open master connection and prepare integration database.");
  }

  @Override
  public void reset() throws HubException {
    try {
      for (Table<?> table : Lists.reverse(ImmutableList.copyOf(tablesInSchemaConstructionOrder.values())))
        reset(table);

      // Finally, all queues
      resetRedis();

    } catch (Exception e) {
      log.error(e.getClass().getName(), e);
      throw new HubException(e.getClass().getName(), e);
    }
    log.debug("Did delete all records create integration database.");
  }

  @Override
  public void reset(Table<?> table) {
    DSLContext db = dbProvider.getDSL();
    var count = db.selectCount().from(table).fetchOne(0, int.class);
    if (Objects.nonNull(count) && 0 < count)
      db.deleteFrom(table).execute();
  }

  @Override
  public <N> N insert(N entity) throws HubException {
    try {
      return insert(dbProvider.getDSL(), entity);
    } catch (Exception e) {
      throw new HubException(e);
    }
  }

  @Override
  public <N> void batchInsert(Collection<N> entities) throws HubException {
    try {
      DSLContext db = dbProvider.getDSL();
      Collection<? extends TableRecord<?>> records = Lists.newArrayList();
      for (Map.Entry<Class<?>, Table<?>> entry : tablesInSchemaConstructionOrder.entrySet()) {
        Class<?> cls = entry.getKey();
        Table<?> table = entry.getValue();
        records.addAll(recordsFrom(db, table, filter(entities, cls)));
      }
      int[] rows = db.batchInsert(records).execute();
      if (!Objects.equals(rows.length, entities.size()))
        throw new ManagerException(String.format("Only inserted %d of %d intended rows", rows.length, entities.size()));
    } catch (Exception e) {
      throw new HubException(e);
    }
  }

  @Override
  public void shutdown() {
    try {
      dbProvider.shutdown();
    } catch (Exception e) {
      log.error("Failed to shutdown SQL connection", e);
    }
    redisConnection.close();
    log.debug("Did close master connection to integration database.");
  }

  @Override
  public void resetRedis() {
    redisDeleteAllKeysMatching(hubAccessControlProvider.computeKey(SELECT_ALL_PATTERN));
  }

  @Override
  public DSLContext getDSL() {
    return dbProvider.getDSL();
  }

  public O create(HubAccess access, O entity) {
    return null;
  }

  public void destroy(HubAccess access, UUID id) {

  }

  public O newInstance() {
    return null;
  }

  public Collection<O> readMany(HubAccess access, Collection<UUID> parentIds) {
    return null;
  }

  public O readOne(HubAccess access, UUID id) {
    return null;
  }

  public O update(HubAccess access, UUID id, O entity) {
    // no op
    return entity;
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
    log.debug("Did delete all redis keys matching: {}", pattern);
  }

}
