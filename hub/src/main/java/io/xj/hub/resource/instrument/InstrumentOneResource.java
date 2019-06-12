// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.instrument;

import io.xj.core.dao.InstrumentDAO;
import io.xj.core.model.payload.MediaType;
import io.xj.core.model.payload.Payload;
import io.xj.core.model.user.role.UserRoleType;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PATCH;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 Instrument record
 */
@Path("instruments/{id}")
// TODO don't necessarily include all sub-entities--- only do so on request, and probably not at all for the index resources
public class InstrumentOneResource extends HubResource {

  @PathParam("id")
  String id;

  /**
   Get one instrument.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc) {
    return readOne(crc, dao(), id);
  }

  /**
   Update one instrument

   @param payload with which to update Instrument record.
   @return Response
   */
  @PATCH
  @Consumes(MediaType.APPLICATION_JSON_API)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response update(Payload payload, @Context ContainerRequestContext crc) {
    return update(crc, dao(), id, payload);
  }

  /**
   Delete one instrument

   @return Response
   */
  @DELETE
  @RolesAllowed(UserRoleType.ADMIN)
  public Response delete(@Context ContainerRequestContext crc) {
    return delete(crc, dao(), id);
  }

  /**
   Get DAO from injector

   @return DAO
   */
  private InstrumentDAO dao() {
    return injector.getInstance(InstrumentDAO.class);
  }
}
