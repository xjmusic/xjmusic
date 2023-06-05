// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.yard.api;

import io.xj.nexus.work.NexusWork;
import io.xj.ship.work.ShipWork;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;

@RestController
public class YardHealthController {
  private final NexusWork nexusWork;
  private final ShipWork shipWork;

  public YardHealthController(
    NexusWork nexusWork,
    ShipWork shipWork
  ) {
    this.nexusWork = nexusWork;
    this.shipWork = shipWork;
  }

  @GET
  @PermitAll
  @GetMapping("/healthz")
  public ResponseEntity<String> index() {
    if (!nexusWork.isHealthy()) {
      return ResponseEntity.internalServerError()
        .body("Nexus work is not healthy");
    }
    if (!shipWork.isHealthy()) {
      return ResponseEntity.internalServerError()
        .body("Ship work is not healthy");
    }
    return ResponseEntity.ok().build();
  }
}
