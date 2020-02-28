// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.lib.core.persistence;

import com.google.inject.Inject;
import io.xj.lib.core.exception.CoreException;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MigrationImpl implements Migration {
  private static final Logger log = LoggerFactory.getLogger(MigrationImpl.class);
  private final SQLDatabaseProvider sqlDatabaseProvider;

  @Inject
  MigrationImpl(SQLDatabaseProvider sqlDatabaseProvider) {
    this.sqlDatabaseProvider = sqlDatabaseProvider;
  }

  @Override
  public void migrate() throws CoreException {
    try {
      getFlyway().migrate();
      log.info("Did migrate database.");
    } catch (Exception e) {
      log.error("migration failed.", e);
      throw new CoreException("migration failed. " + e.getClass().getName() + ": " + e);
    }
  }

  @Override
  public void validate() throws CoreException {
    try {
      getFlyway().validate();
      log.info("Did validate database migration.");
    } catch (Exception e) {
      log.error("migration validation check failed.", e);
      throw new CoreException("migration validation check failed. " + e.getClass().getName() + ": " + e);
    }
  }

  /**
   Get a Flyway instance to perform or validate migration

   @return Flyway instance
   */
  private Flyway getFlyway() {
    Flyway flyway = Flyway.configure()
      .dataSource(sqlDatabaseProvider.getDataSource())
      .schemas(sqlDatabaseProvider.getSchemas())
      .load();
    log.info("Will configure Flyway");
    return flyway;
  }

}
