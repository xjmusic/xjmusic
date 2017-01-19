package io.outright.xj.hub.resource.auth;

import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.app.access.AccessControlModule;

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
 * Current user authentication
 */
@Path("auth")
public class AuthResource {

  /**
   * Get current authentication.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response getCurrentAuthentication(@Context ContainerRequestContext crc) throws IOException {
    AccessControlModule accessControlModule = AccessControlModule.fromContext(crc);
     return Response
        .accepted(accessControlModule.toJSON())
        .type(MediaType.APPLICATION_JSON)
        .build();
  }
}
