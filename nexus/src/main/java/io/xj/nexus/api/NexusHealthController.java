// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.api;

import io.xj.nexus.work.WorkFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;

@RestController
public class NexusHealthController {
  final WorkFactory workFactory;

  public NexusHealthController(
    WorkFactory workFactory
  ) {
    this.workFactory = workFactory;
  }

  @GET
  @PermitAll
  @GetMapping("/healthz")
  public ResponseEntity<String> index() {
    if (!workFactory.isHealthy()) {
      return ResponseEntity.internalServerError()
        .body("Work is not healthy");
    }
    return ResponseEntity.ok().build();
  }
}