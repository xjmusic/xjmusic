// Copyright (c) XJ Music Inc. (https://xjmusic.com) All Rights Reserved.
package io.xj.workstation.service.api;

import io.xj.nexus.work.FabricationManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkstationServiceHealthController {
  final FabricationManager fabricationManager;

  public WorkstationServiceHealthController(
    FabricationManager fabricationManager
  ) {
    this.fabricationManager = fabricationManager;
  }

  @GetMapping("/healthz")
  public ResponseEntity<String> index() {
    if (!fabricationManager.isHealthy()) {
      return ResponseEntity.internalServerError()
        .body("Work is not healthy");
    }
    return ResponseEntity.ok().build();
  }
}
