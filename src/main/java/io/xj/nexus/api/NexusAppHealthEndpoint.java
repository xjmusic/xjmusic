// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.nexus.api;

import com.google.inject.Inject;
import io.xj.nexus.work.NexusWork;

import javax.annotation.security.PermitAll;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

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
  public String index() {
    if (!nexusWork.isHealthy()) throw new RuntimeException("Work is stale!");

    return "ok";
  }
}
