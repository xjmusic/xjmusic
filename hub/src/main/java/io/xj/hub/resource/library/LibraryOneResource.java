// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.library;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.LibraryDAO;
import io.xj.core.model.library.Library;
import io.xj.core.model.library.LibraryWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
 Library record
 */
@Path("libraries/{id}")
public class LibraryOneResource extends HubResource {
  private final LibraryDAO libraryDAO = injector.getInstance(LibraryDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one library.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        Library.KEY_ONE,
        libraryDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Update one library

   @param data with which to update Library record.
   @return Response
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
  public Response update(LibraryWrapper data, @Context ContainerRequestContext crc) {
    try {
      libraryDAO.update(Access.fromContext(crc), new BigInteger(id), data.getLibrary());
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failureToUpdate(e);
    }
  }

  /**
   Delete one library

   @return Response
   */
  @DELETE
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      libraryDAO.destroy(Access.fromContext(crc), new BigInteger(id));
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
