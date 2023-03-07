// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import io.xj.hub.persistence.HubSqlStoreProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import java.sql.SQLException;

@RestController
public class HealthEndpoint {
  private final HubSqlStoreProvider sqlStoreProvider;

  public HealthEndpoint(
    HubSqlStoreProvider sqlStoreProvider
  ) {
    this.sqlStoreProvider = sqlStoreProvider;
  }

  @GET
  @PermitAll
  @GetMapping("/healthz")
  public ResponseEntity<String> index() {
    try (var ignored = sqlStoreProvider.getDataSource().getConnection()) {
      return ResponseEntity.ok().build();
    } catch (SQLException e) {
      return ResponseEntity.internalServerError()
        .body(e.getMessage());
    }
  }
}
