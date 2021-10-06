// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.api;

import com.google.inject.Inject;
import io.xj.ship.work.Work;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 Health resource.
 */
@Path("healthz")
public class ShipAppHealthEndpoint {

  private final Work work;

  @Inject
  public ShipAppHealthEndpoint(
    Work work
  ) {
    this.work = work;
  }

  @GET
  @PermitAll
  public Response index() {
    if (!work.isHealthy()) return Response.serverError().entity("Work is stale!").build();

    return Response.ok().entity("ok").build();
  }
}
