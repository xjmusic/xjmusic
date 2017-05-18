// Copyright (c) 2017, Outright Mental Inc. (https://w.outright.io) All Rights Reserved.
package io.outright.xj.hub.resource.chain_instrument;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.ChainInstrumentDAO;
import io.outright.xj.core.model.chain_instrument.ChainInstrument;
import io.outright.xj.core.model.role.Role;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 Chain Instrument record
 */
@Path("chain-instruments/{id}")
public class ChainInstrumentRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final ChainInstrumentDAO DAO = injector.getInstance(ChainInstrumentDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one ChainInstrument by id

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        ChainInstrument.KEY_ONE,
        DAO.readOne(
          Access.fromContext(crc),
          ULong.valueOf(id)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Delete one ChainInstrument

   @return application/json response.
   */
  @DELETE
  @RolesAllowed({Role.ARTIST,Role.ENGINEER,Role.ADMIN})
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      DAO.delete(Access.fromContext(crc), ULong.valueOf(id));
      return Response.accepted("{}").build();
    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
