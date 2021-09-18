// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.ship.api;

import com.google.inject.Inject;
import io.xj.ship.work.ShipWork;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 Health resource.
 */
@Path("healthz")
public class ShipAppHealthEndpoint {

  private final ShipWork shipWork;

  @Inject
  public ShipAppHealthEndpoint(
    ShipWork shipWork
  ) {
    this.shipWork = shipWork;
  }

  @GET
  @PermitAll
  public Response index() {
    if (!shipWork.isHealthy()) return Response.serverError().entity("Work is stale!").build();

    return Response.ok().entity("ok").build();
  }
}
