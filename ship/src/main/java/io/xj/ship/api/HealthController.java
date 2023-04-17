// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.api;

import io.xj.ship.work.ShipWork;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;

@RestController
public class HealthController {
  private final ShipWork work;

  public HealthController(
    ShipWork work
  ) {
    this.work = work;
  }

  @GET
  @PermitAll
  @GetMapping("/healthz")
  public ResponseEntity<String> index() {
    if (!work.isHealthy()) {
      return ResponseEntity.internalServerError()
        .body("Work is not ready");
    }
    return ResponseEntity.ok().build();
  }
}
