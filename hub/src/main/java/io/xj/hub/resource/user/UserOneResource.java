// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.user;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.UserDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.user.User;
import io.xj.core.model.user.UserWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigInteger;

/**
 User record
 */
@Path("users/{id}")
public class UserOneResource extends HubResource {
  private final UserDAO userDAO = injector.getInstance(UserDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id") String id;

  /**
   Get one user.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        User.KEY_ONE,
        userDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (CoreException ignored) {
      return response.notFound("User");

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Update one User.

   @param data with which to update User record.
   @return Response.
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ADMIN)
  public Response update(UserWrapper data, @Context ContainerRequestContext crc) {
    try {
      userDAO.updateUserRolesAndDestroyTokens(
        Access.fromContext(crc),
        new BigInteger(id),
        data.getUser());
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failureToUpdate(e);
    }
  }

}
