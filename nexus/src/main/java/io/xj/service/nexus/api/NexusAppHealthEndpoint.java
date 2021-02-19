// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.nexus.api;

import com.google.inject.Inject;

import javax.annotation.security.PermitAll;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

/**
 Health resource.
 */
@Path("api/1/-/health")
@Singleton
public class NexusAppHealthEndpoint {

  @Inject
  public NexusAppHealthEndpoint() {
  }

  @GET
  @PermitAll
  public String index() {
    return "ok";
  }
}
