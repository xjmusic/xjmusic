// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.instrument;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.InstrumentDAO;
import io.xj.core.model.instrument.Instrument;
import io.xj.core.model.instrument.InstrumentWrapper;
import io.xj.core.model.role.Role;
import io.xj.core.server.HttpResponseProvider;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

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

/**
 Instrument record
 */
@Path("instruments/{id}")
public class InstrumentRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
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
  @RolesAllowed(Role.USER)
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        Instrument.KEY_ONE,
        instrumentDAO.readOne(
          Access.fromContext(crc),
          ULong.valueOf(id)));

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
  @RolesAllowed(Role.ARTIST)
  public Response update(InstrumentWrapper data, @Context ContainerRequestContext crc) {
    try {
      instrumentDAO.update(Access.fromContext(crc), ULong.valueOf(id), data.getInstrument());
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
  @RolesAllowed(Role.ADMIN)
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      instrumentDAO.delete(Access.fromContext(crc), ULong.valueOf(id));
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
