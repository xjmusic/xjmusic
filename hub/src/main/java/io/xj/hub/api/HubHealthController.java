// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import io.xj.hub.persistence.HubSqlStoreProvider;
import io.xj.hub.service.PreviewNexusAdmin;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import java.sql.SQLException;

@RestController
public class HubHealthController {
  final HubSqlStoreProvider sqlStoreProvider;
  final PreviewNexusAdmin previewNexusAdmin;

  public HubHealthController(
    HubSqlStoreProvider sqlStoreProvider,
    PreviewNexusAdmin previewNexusAdmin
  ) {
    this.sqlStoreProvider = sqlStoreProvider;
    this.previewNexusAdmin = previewNexusAdmin;
  }

  @PermitAll
  @GetMapping("/healthz")
  public ResponseEntity<String> index() {
    try (
      var ignoredSqlConnection = sqlStoreProvider.getDataSource().getConnection()
    ) {
      if (!previewNexusAdmin.isReady()) {
        return ResponseEntity.internalServerError()
          .body("Service administrator is not ready");
      }
      return ResponseEntity.ok().build();
    } catch (SQLException e) {
      return ResponseEntity.internalServerError()
        .body(e.getMessage());
    }
  }
}
