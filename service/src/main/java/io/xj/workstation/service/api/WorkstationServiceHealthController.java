// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.workstation.service.api;

import io.xj.nexus.work.WorkFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkstationServiceHealthController {
  final WorkFactory workFactory;

  public WorkstationServiceHealthController(
    WorkFactory workFactory
  ) {
    this.workFactory = workFactory;
  }

  @GetMapping("/healthz")
  public ResponseEntity<String> index() {
    if (!workFactory.isHealthy()) {
      return ResponseEntity.internalServerError()
        .body("Work is not healthy");
    }
    return ResponseEntity.ok().build();
  }
}
