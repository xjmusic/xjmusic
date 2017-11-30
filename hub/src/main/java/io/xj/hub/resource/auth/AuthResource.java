// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.hub.resource.auth;

import io.xj.core.access.impl.Access;
import io.xj.core.model.role.Role;

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
 Current user authentication
 */
@Path("auth")
public class AuthResource {

  /**
   Get current authentication.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response getCurrentAuthentication(@Context ContainerRequestContext crc) throws IOException {
    Access access = Access.fromContext(crc);
    return Response
      .accepted(access.toJSON())
      .type(MediaType.APPLICATION_JSON)
      .build();
  }
}
