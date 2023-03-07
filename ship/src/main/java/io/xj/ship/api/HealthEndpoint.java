// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.api;

import io.xj.ship.work.ShipWork;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

@RestController
public class HealthEndpoint {
  private final ShipWork work;

  public HealthEndpoint(
    ShipWork work
  ) {
    this.work = work;
  }

  @GET
  @PermitAll
  @GetMapping("/healthz")
  public Response index() {
    if (!work.isHealthy())
      return Response.serverError().entity("Work is stale!").build();

    return Response.ok().entity("ok").build();
  }
}
