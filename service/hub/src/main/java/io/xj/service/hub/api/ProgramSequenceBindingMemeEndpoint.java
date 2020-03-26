// Copyright (c) XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.service.hub.api;

import com.google.inject.Injector;
import io.xj.service.hub.HubEndpoint;
import io.xj.service.hub.dao.ProgramSequenceBindingMemeDAO;
import io.xj.service.hub.model.UserRoleType;
import io.xj.lib.rest_api.MediaType;
import io.xj.lib.rest_api.Payload;

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

/**
 ProgramSequenceBindingMeme endpoint
 */
@Path("program-sequence-binding-memes")
public class ProgramSequenceBindingMemeEndpoint extends HubEndpoint {
  private ProgramSequenceBindingMemeDAO dao;

  /**
   The constructor's @javax.inject.Inject bindingMeme is for HK2, Jersey's injection system,
   which injects the inner com.google.inject.Injector for Guice-bound classes
   */
  @Inject
  public ProgramSequenceBindingMemeEndpoint(
    Injector injector
  ) {
    super(injector);
    dao = injector.getInstance(ProgramSequenceBindingMemeDAO.class);
  }

  /**
   Create new programSequence bindingMeme

   @param payload with which to of ProgramSequence BindingMeme
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed({UserRoleType.ARTIST})
  public Response create(Payload payload, @Context ContainerRequestContext crc) {
    return create(crc, dao(), payload);
  }

  /**
   Get one ProgramSequenceBindingMeme by id

   @return application/json response.
   */
  @GET
  @Path("{id}")
  @RolesAllowed({UserRoleType.ARTIST})
  public Response readOne(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return readOne(crc, dao(), id);
  }

  /**
   Get BindingMemes in one programSequence.

   @return application/json response.
   */
  @GET
  @RolesAllowed({UserRoleType.ARTIST})
  public Response readAll(@Context ContainerRequestContext crc, @QueryParam("programSequenceBindingId") String programSequenceBindingId) {
    return readMany(crc, dao(), programSequenceBindingId);
  }

  /**
   Update one ProgramSequenceBindingMeme

   @param payload with which to update record.
   @return Response
   */
  @PATCH
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response update(Payload payload, @Context ContainerRequestContext crc, @PathParam("id") String id) {
    return update(crc, dao(), id, payload);
  }

  /**
   Delete one ProgramSequenceBindingMeme by programSequenceId and bindingMemeId

   @return application/json response.
   */
  @DELETE
  @Path("{id}")
  @RolesAllowed({UserRoleType.ARTIST})
  public Response delete(@Context ContainerRequestContext crc, @PathParam("id") String id) {
    return delete(crc, dao(), id);
  }

  /**
   Get DAO of injector

   @return DAO
   */
  private ProgramSequenceBindingMemeDAO dao() {
    return dao;
  }

}
