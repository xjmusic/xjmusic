// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import com.google.inject.Inject;
import io.xj.hub.persistence.HubDatabaseProvider;

import javax.annotation.security.PermitAll;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import java.sql.SQLException;

/**
 Health resource.
 */
@Path("healthz")
@Singleton
public class HealthEndpoint {
  private final HubDatabaseProvider dbProvider;

  @Inject
  public HealthEndpoint(
    HubDatabaseProvider dbProvider
  ) {
    this.dbProvider = dbProvider;
  }

  @GET
  @PermitAll
  public String index() throws SQLException {
    try (var ignored = dbProvider.getDataSource().getConnection()) {
      return "ok";
    }
  }
}
