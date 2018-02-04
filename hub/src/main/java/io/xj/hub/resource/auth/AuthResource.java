// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.auth;

import io.xj.core.access.impl.Access;
import io.xj.core.model.user_role.UserRoleType;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
  @RolesAllowed(UserRoleType.USER)
  public static Response getCurrentAuthentication(@Context ContainerRequestContext crc) {
    Access access = Access.fromContext(crc);
    return Response
      .accepted(access.toJSON())
      .type(MediaType.APPLICATION_JSON)
      .build();
  }
}
