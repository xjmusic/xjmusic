// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.api;

import io.xj.hub.kubernetes.KubernetesAdmin;
import io.xj.hub.persistence.HubSqlStoreProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import java.sql.SQLException;

@RestController
public class HealthController {
  private final HubSqlStoreProvider sqlStoreProvider;
  private final KubernetesAdmin kubernetesAdmin;

  public HealthController(
    HubSqlStoreProvider sqlStoreProvider,
    KubernetesAdmin kubernetesAdmin
  ) {
    this.sqlStoreProvider = sqlStoreProvider;
    this.kubernetesAdmin = kubernetesAdmin;
  }

  @GET
  @PermitAll
  @GetMapping("/healthz")
  public ResponseEntity<String> index() {
    try (
      var ignoredSqlConnection = sqlStoreProvider.getDataSource().getConnection();
    ) {
      if (!kubernetesAdmin.isReady()) {
        return ResponseEntity.internalServerError()
          .body("Kubernetes is not ready");
      }
      return ResponseEntity.ok().build();
    } catch (SQLException e) {
      return ResponseEntity.internalServerError()
        .body(e.getMessage());
    }
  }
}
