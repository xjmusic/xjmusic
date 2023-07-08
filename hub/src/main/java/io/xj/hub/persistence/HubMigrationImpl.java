// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.persistence;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HubMigrationImpl implements HubMigration {
  static final Logger log = LoggerFactory.getLogger(HubMigrationImpl.class);
  final HubSqlStoreProvider sqlStoreProvider;

  public HubMigrationImpl(HubSqlStoreProvider sqlStoreProvider) {
    this.sqlStoreProvider = sqlStoreProvider;
  }

  @Override
  public void migrate() throws HubPersistenceException {
    try {
      getFlyway().migrate();
      log.info("Did migrate database.");
    } catch (Exception e) {
      log.error("migration failed.", e);
      throw new HubPersistenceException("migration failed. " + e.getClass().getName() + ": " + e);
    }
  }

  @Override
  public void validate() throws HubPersistenceException {
    try {
      getFlyway().validate();
      log.info("Did validate database migration.");
    } catch (Exception e) {
      log.error("migration validation check failed.", e);
      throw new HubPersistenceException("migration validation check failed. " + e.getClass().getName() + ": " + e);
    }
  }

  /**
   * Get a Flyway instance to perform or validate migration
   *
   * @return Flyway instance
   */
  Flyway getFlyway() {
    Flyway flyway = Flyway.configure()
      .dataSource(sqlStoreProvider.getDataSource())
      .schemas(sqlStoreProvider.getSchemas())
      .load();
    log.debug("Will configure Flyway");
    return flyway;
  }

}
