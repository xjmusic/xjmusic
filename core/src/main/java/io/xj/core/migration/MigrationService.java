// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.core.migration;

import io.xj.core.exception.ConfigException;
import io.xj.core.database.sql.SQLDatabaseProvider;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum MigrationService {
  INSTANCE;
  private static Logger log = LoggerFactory.getLogger(MigrationService.class);

  public static void migrate(SQLDatabaseProvider sqlDatabaseProvider) throws ConfigException {
    try {
      flyway(sqlDatabaseProvider).migrate();
    } catch (Exception e) {
      log.error("migration failed.", e);
      throw new ConfigException("migration failed. " + e.getClass().getName() + ": " + e);
    }
  }

  public static void validate(SQLDatabaseProvider sqlDatabaseProvider) throws ConfigException {
    try {
      flyway(sqlDatabaseProvider).validate();
    } catch (Exception e) {
      log.error("migration validation check failed.", e);
      throw new ConfigException("migration validation check failed. " + e.getClass().getName() + ": " + e);
    }
  }

  private static Flyway flyway(SQLDatabaseProvider sqlDatabaseProvider) throws ConfigException {
    Flyway flyway = new Flyway();
    flyway.setDataSource(sqlDatabaseProvider.getUrl(), sqlDatabaseProvider.getUser(), sqlDatabaseProvider.getPass());
    return flyway;
  }
}
