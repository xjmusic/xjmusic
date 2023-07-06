// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.api;

import io.xj.nexus.work.CraftWork;
import io.xj.nexus.work.DubWork;
import io.xj.nexus.work.ShipWork;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;

@RestController
public class NexusHealthController {
  private final CraftWork craftWork;
  private final DubWork dubWork;
  private final ShipWork shipWork;

  public NexusHealthController(
    CraftWork craftWork,
    DubWork dubWork,
    ShipWork shipWork
  ) {
    this.craftWork = craftWork;
    this.dubWork = dubWork;
    this.shipWork = shipWork;
  }

  @GET
  @PermitAll
  @GetMapping("/healthz")
  public ResponseEntity<String> index() {
    if (!craftWork.isHealthy()) {
      return ResponseEntity.internalServerError()
        .body("Craft work is not healthy");
    }
    if (!dubWork.isHealthy()) {
      return ResponseEntity.internalServerError()
        .body("Dub work is not healthy");
    }
    if (!shipWork.isHealthy()) {
      return ResponseEntity.internalServerError()
        .body("Ship work is not healthy");
    }
    return ResponseEntity.ok().build();
  }
}
