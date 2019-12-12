// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub;

import com.google.inject.Injector;
import io.xj.core.app.AppResource;
import io.xj.core.dao.ProgramMemeDAO;
import io.xj.core.model.UserRoleType;
import io.xj.core.payload.MediaType;
import io.xj.core.payload.Payload;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 ProgramMeme endpoint
 */
@Path("program-memes")
public class ProgramMemeEndpoint extends AppResource {
  private ProgramMemeDAO dao;

  /**
   The constructor's @javax.inject.Inject binding is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public ProgramMemeEndpoint(
    Injector injector
  ) {
    super(injector);
    dao = injector.getInstance(ProgramMemeDAO.class);
  }

  /**
   Get Bindings in one programMeme.

   @return application/json response.
   */
  @GET
  @RolesAllowed({UserRoleType.ENGINEER})
  public Response readAll(@Context ContainerRequestContext crc, @QueryParam("programMemeId") String programMemeId) {
    return readMany(crc, dao(), programMemeId);
  }

  /**
   Create new programMeme binding

   @param payload with which to of ProgramMeme Binding
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed({UserRoleType.ENGINEER})
  public Response create(Payload payload, @Context ContainerRequestContext crc) {
    return create(crc, dao(), payload);
  }

  /**
   Get one ProgramMeme by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed({UserRoleType.ENGINEER})
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Delete one ProgramMeme by programMemeId and bindingId

   @return application/json response.
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed({UserRoleType.ENGINEER})
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, dao(), id);
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private ProgramMemeDAO dao() {
    return dao;
  }

}
