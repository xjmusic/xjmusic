package io.outright.xj.hub.resources.auth;

import io.outright.xj.core.app.access.Role;
import io.outright.xj.core.app.access.UserAccessModel;

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
    UserAccessModel userAccessModel = UserAccessModel.fromContext(crc);
     return Response
        .accepted(userAccessModel.toJSON())
        .type(MediaType.APPLICATION_JSON)
        .build();
  }
}
