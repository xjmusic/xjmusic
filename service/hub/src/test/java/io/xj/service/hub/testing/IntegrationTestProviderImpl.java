// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.testing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.lib.rest_api.ApiUrlProvider;
import io.xj.lib.rest_api.PayloadFactory;
import io.xj.lib.rest_api.RestApiException;
import io.xj.service.hub.HubApp;
import io.xj.service.hub.HubException;
import io.xj.service.hub.access.Access;
import io.xj.service.hub.dao.DAO;
import io.xj.service.hub.dao.DAOImpl;
import io.xj.service.hub.entity.Entity;
import io.xj.service.hub.persistence.Migration;
import io.xj.service.hub.persistence.RedisDatabaseProvider;
import io.xj.service.hub.persistence.SQLDatabaseProvider;
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
public class IntegrationTestProviderImpl<O extends Entity> extends DAOImpl<O> implements IntegrationTestProvider {
  final Logger log = LoggerFactory.getLogger(IntegrationTestProviderImpl.class);
  final Jedis redisConnection;
  private final SQLDatabaseProvider sqlDatabaseProvider;

  /**
   Since this class is a singleton, the process here in its constructor
   will happen only once for a whole test suite
   */
  @Inject
  IntegrationTestProviderImpl(
    SQLDatabaseProvider sqlDatabaseProvider,
    RedisDatabaseProvider redisDatabaseProvider,
    Migration migration,
    PayloadFactory payloadFactory,
    Config config,
    ApiUrlProvider apiUrlProvider) {
    super(payloadFactory);
    this.sqlDatabaseProvider = sqlDatabaseProvider;

    // Build the Hub REST API payload topology
    HubApp.buildApiTopology(payloadFactory);

    // Build the Hub REST API payload topology
    HubApp.configureApiUrls(config, apiUrlProvider);

    // Requires that a configuration has been bound
    config.getString("app.name");

    // Begin database prep
    log.info("Will prepare integration database.");

    // Migrate the test database
    try {
      migration.migrate();
    } catch (HubException e) {
      log.error("CoreException", e);
      System.exit(1);
    }

    // jOOQ handles DataSource

    // One Redis connection remains open until main program exit
    redisConnection = redisDatabaseProvider.getClient();
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
      flushRedis();

    } catch (Exception e) {
      log.error(e.getClass().getName(), e);
      throw new HubException(e.getClass().getName(), e);
    }
    log.info("Did delete all records create integration database.");
  }

  @Override
  public void reset(Table<?> table) {
    DSLContext db = sqlDatabaseProvider.getDSL();
    if (0 < db.selectCount().from(table).fetchOne(0, int.class))
      db.deleteFrom(table).execute();
  }

  @Override
  public <N extends Entity> N insert(N entity) throws HubException, RestApiException {
    return insert(sqlDatabaseProvider.getDSL(), entity);
  }

  @Override
  public <N extends Entity> void batchInsert(Collection<N> entities) throws HubException, RestApiException {
    DSLContext db = sqlDatabaseProvider.getDSL();
    Collection<? extends TableRecord<?>> records = Lists.newArrayList();
    for (Map.Entry<Class<?>, Table<?>> entry : DAO.tablesInSchemaConstructionOrder.entrySet()) {
      Class<?> cls = entry.getKey();
      Table<?> table = entry.getValue();
      records.addAll(recordsFrom(db, table, filter(entities, cls)));
    }
    int[] rows = db.batchInsert(records).execute();
    if (!Objects.equals(rows.length, entities.size()))
      throw new HubException(String.format("Only inserted %d of %d intended rows", rows.length, entities.size()));
  }

  @Override
  public void shutdown() {
    try {
      sqlDatabaseProvider.shutdown();
    } catch (Exception e) {
      log.error("Failed to shutdown SQL connection", e);
    }
    redisConnection.close();
    log.info("Did close master connection to integration database.");

    System.clearProperty("work.queue.name");
  }

  @Override
  public void flushRedis() {
    redisConnection.flushDB();
    log.info("Did flush entire Redis contents and database");
  }

  @Override
  public DSLContext getDSL() {
    return sqlDatabaseProvider.getDSL();
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

  @Override
  public O create(Access access, O entity) throws HubException {
    return null;
  }

  @Override
  public void destroy(Access access, UUID id) throws HubException {

  }

  @Override
  public O newInstance() {
    return null;
  }

  @Override
  public Collection<O> readMany(Access access, Collection<UUID> parentIds) throws HubException {
    return null;
  }

  @Override
  public O readOne(Access access, UUID id) throws HubException {
    return null;
  }

  @Override
  public void update(Access access, UUID id, O entity) throws HubException {

  }

}
