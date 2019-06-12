//  Copyright (c) 2019, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.persistence.sql.migration.impl;

import com.google.inject.Inject;
import io.xj.core.exception.CoreException;
import io.xj.core.persistence.sql.SQLDatabaseProvider;
import io.xj.core.persistence.sql.migration.Migration;
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
   @throws CoreException on failure
   */
  private Flyway getFlyway() throws CoreException {
    String url = sqlDatabaseProvider.getUrl();
    String user = sqlDatabaseProvider.getUser();
    String pass = sqlDatabaseProvider.getPassword();
    Flyway flyway = new Flyway();
    log.info("Will configure Flyway, url={} user={} pass={}", url, user, pass);
    flyway.setDataSource(url, user, pass);
    return flyway;
  }

}
