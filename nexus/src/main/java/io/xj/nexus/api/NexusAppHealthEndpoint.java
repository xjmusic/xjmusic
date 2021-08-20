// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.api;

import com.google.inject.Inject;
import io.xj.nexus.work.NexusWork;

import javax.annotation.security.PermitAll;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 Health resource.
 */
@Path("-/health")
public class NexusAppHealthEndpoint {

  private final NexusWork nexusWork;

  @Inject
  public NexusAppHealthEndpoint(
    NexusWork nexusWork
  ) {
    this.nexusWork = nexusWork;
  }

  @GET
  @PermitAll
  public Response index() {
    if (!nexusWork.isHealthy()) return Response.serverError().entity("Work is stale!").build();

    return Response.ok().entity("ok").build();
  }
}
