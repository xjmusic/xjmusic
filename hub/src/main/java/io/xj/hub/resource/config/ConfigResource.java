// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.hub.resource.config;

import io.xj.core.app.config.Exposure;
import io.xj.core.app.exception.ConfigException;
import io.xj.core.model.role.Role;
import io.xj.core.transport.JSON;

import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 Current platform configuration
 */
@Path("config")
public class ConfigResource {

  /**
   Get current platform configuration (PUBLIC)

   @return application/json response.
   */
  @GET
  @WebResult
  @PermitAll
  public Response getConfig(@Context ContainerRequestContext crc) throws IOException, ConfigException {
    return Response
      .accepted(JSON.wrap(Exposure.KEY_CONFIG, Exposure.configJSON()).toString())
      .type(MediaType.APPLICATION_JSON)
      .build();
  }
}
