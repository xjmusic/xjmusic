// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub;

import com.google.inject.Injector;
import io.xj.lib.core.access.Access;
import io.xj.lib.core.app.AppResource;
import io.xj.lib.core.dao.LibraryDAO;
import io.xj.lib.core.model.UserRoleType;
import io.xj.lib.core.payload.MediaType;
import io.xj.lib.core.payload.Payload;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.util.Objects;

/**
 Libraries
 */
@Path("libraries")
public class LibraryEndpoint extends AppResource {

  private LibraryDAO dao;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public LibraryEndpoint(
    Injector injector
  ) {
    super(injector);
    dao = injector.getInstance(LibraryDAO.class);
  }

  /**
   Get all libraries.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(@Context ContainerRequestContext crc, @QueryParam("accountId") String accountId) {
    if (Objects.nonNull(accountId))
      return readMany(crc, dao(), accountId);
    else
      return readMany(crc, dao(), Access.fromContext(crc).getAccountIds());
  }

  /**
   Create new library

   @param payload with which to update Library record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
  public Response create(Payload payload, @Context ContainerRequestContext crc) {
    return create(crc, dao(), payload);
  }

  /**
   Get one library.

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Update one library

   @param payload with which to update Library record.
   @return Response
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
  public Response update(Payload payload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, payload);
  }

  /**
   Delete one library

   @return Response
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, dao(), id);
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private LibraryDAO dao() {
    return dao;
  }
}
