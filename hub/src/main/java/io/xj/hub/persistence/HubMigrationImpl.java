// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.persistence;

import com.google.inject.Inject;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HubMigrationImpl implements HubMigration {
  private static final Logger log = LoggerFactory.getLogger(HubMigrationImpl.class);
  private final HubDatabaseProvider hubDatabaseProvider;

  @Inject
  HubMigrationImpl(HubDatabaseProvider hubDatabaseProvider) {
    this.hubDatabaseProvider = hubDatabaseProvider;
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
   Get a Flyway instance to perform or validate migration

   @return Flyway instance
   */
  private Flyway getFlyway() {
    Flyway flyway = Flyway.configure()
      .dataSource(hubDatabaseProvider.getDataSource())
      .schemas(hubDatabaseProvider.getSchemas())
      .load();
    log.debug("Will configure Flyway");
    return flyway;
  }

}
