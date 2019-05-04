// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.core.persistence.sql.migration;

import io.xj.core.exception.CoreException;
import io.xj.core.persistence.sql.SQLDatabaseProvider;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum MigrationService {
  INSTANCE;
  private static Logger log = LoggerFactory.getLogger(MigrationService.class);

  public static void migrate(SQLDatabaseProvider sqlDatabaseProvider) throws CoreException {
    try {
      flyway(sqlDatabaseProvider).migrate();
    } catch (Exception e) {
      log.error("migration failed.", e);
      throw new CoreException("migration failed. " + e.getClass().getName() + ": " + e);
    }
  }

  public static void validate(SQLDatabaseProvider sqlDatabaseProvider) throws CoreException {
    try {
      flyway(sqlDatabaseProvider).validate();
    } catch (Exception e) {
      log.error("migration validation check failed.", e);
      throw new CoreException("migration validation check failed. " + e.getClass().getName() + ": " + e);
    }
  }

  private static Flyway flyway(SQLDatabaseProvider sqlDatabaseProvider) throws CoreException {
    Flyway flyway = new Flyway();
    flyway.setDataSource(sqlDatabaseProvider.getUrl(), sqlDatabaseProvider.getUser(), sqlDatabaseProvider.getPass());
    return flyway;
  }
}
