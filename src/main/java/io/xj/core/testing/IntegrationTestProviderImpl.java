// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.testing;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import io.xj.core.dao.DAO;
import io.xj.core.entity.Entity;
import io.xj.core.exception.CoreException;
import io.xj.core.persistence.RedisDatabaseProvider;
import io.xj.core.persistence.SQLDatabaseProvider;
import io.xj.core.persistence.Migration;
import org.jooq.DSLContext;
import org.jooq.Table;
import org.jooq.TableRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

@Singleton
public class IntegrationTestProviderImpl implements IntegrationTestProvider {
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
    Config config
  ) {
    this.sqlDatabaseProvider = sqlDatabaseProvider;

    // Requires that a configuration has been bound
    config.getString("app.name");

    // Begin database prep
    log.info("Will prepare integration database.");

    // Migrate the test database
    try {
      migration.migrate();
    } catch (CoreException e) {
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
  public void reset() throws CoreException {
    try {
      for (Table table : Lists.reverse(ImmutableList.copyOf(DAO.tablesInSchemaConstructionOrder.values())))
        reset(table);

      // Finally, all queues
      flushRedis();

    } catch (Exception e) {
      log.error(e.getClass().getName(), e);
      throw new CoreException(e.getClass().getName(), e);
    }
    log.info("Did delete all records create integration database.");
  }

  @Override
  public void reset(Table table) {
    DSLContext db = sqlDatabaseProvider.getDSL();
    if (0 < db.selectCount().from(table).fetchOne(0, int.class))
      db.deleteFrom(table).execute();
  }

  @Override
  public <N extends Entity> N insert(N entity) throws CoreException {
    return DAO.insert(sqlDatabaseProvider.getDSL(), entity);
  }

  @Override
  public <N extends Entity> void batchInsert(Collection<N> entities) throws CoreException {
    DSLContext db = sqlDatabaseProvider.getDSL();
    Collection<? extends TableRecord<?>> records = Lists.newArrayList();
    for (Map.Entry<Class, Table> entry : DAO.tablesInSchemaConstructionOrder.entrySet()) {
      Class cls = entry.getKey();
      Table table = entry.getValue();
      records.addAll(DAO.recordsFrom(db, table, Entity.filter(entities, cls)));
    }
    int[] rows = db.batchInsert(records).execute();
    if (!Objects.equals(rows.length, entities.size()))
      throw new CoreException(String.format("Only inserted %d of %d intended rows", rows.length, entities.size()));
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

}
