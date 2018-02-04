// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.instrument;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentWrapper;
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
 Instrument record
 */
@Path("instruments/{id}")
public class InstrumentOneResource extends HubResource {
  private final InstrumentDAO instrumentDAO = injector.getInstance(InstrumentDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one instrument.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        Instrument.KEY_ONE,
        instrumentDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Update one instrument

   @param data with which to update Instrument record.
   @return Response
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response update(InstrumentWrapper data, @Context ContainerRequestContext crc) {
    try {
      instrumentDAO.update(Access.fromContext(crc), new BigInteger(id), data.getInstrument());
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failureToUpdate(e);
    }
  }

  /**
   Delete one instrument

   @return Response
   */
  @DELETE
  @RolesAllowed(UserRoleType.ADMIN)
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      instrumentDAO.destroy(Access.fromContext(crc), new BigInteger(id));
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
