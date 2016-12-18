// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.controller.migration;

import io.outright.xj.core.app.db.SQLDatabaseProvider;
import io.outright.xj.core.app.exception.ConfigException;

import com.google.inject.Inject;
import org.flywaydb.core.Flyway;

public class MigrationController {
  private SQLDatabaseProvider SQLDatabaseProvider;

  @Inject
  public MigrationController(
    SQLDatabaseProvider SQLDatabaseProvider
  ) {
    this.SQLDatabaseProvider = SQLDatabaseProvider;
  }

  public void migrate() throws ConfigException {
    Flyway flyway = new Flyway();
    flyway.setDataSource(SQLDatabaseProvider.getUrl(), SQLDatabaseProvider.getUser(), SQLDatabaseProvider.getPass());
    flyway.migrate();
  }
}
