// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.api;

import io.xj.nexus.work.NexusWork;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.core.Response;

@RestController
public class HealthEndpoint {
  private final NexusWork work;

  public HealthEndpoint(
    NexusWork work
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
