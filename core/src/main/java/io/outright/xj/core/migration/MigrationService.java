// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.core.migration;

import io.outright.xj.core.app.db.SQLDatabaseProvider;
import io.outright.xj.core.app.exception.ConfigException;

import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum MigrationService {
  GLOBAL;
  private static Logger log = LoggerFactory.getLogger(MigrationService.class);

  public void migrate(SQLDatabaseProvider sqlDatabaseProvider) throws ConfigException {
    try {
      Flyway flyway = new Flyway();
      flyway.setDataSource(sqlDatabaseProvider.getUrl(), sqlDatabaseProvider.getUser(), sqlDatabaseProvider.getPass());
      flyway.migrate();
    } catch (Exception e) {
      log.error(e.getClass().getName() + ": " + e);
      throw new ConfigException(e.getClass().getName() + ": " + e);
    }
  }

}
