// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.outright.xj.hub.resource.chain_instrument;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.ChainInstrumentDAO;
import io.outright.xj.core.model.chain_instrument.ChainInstrument;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Chain Instrument record
 */
@Path("chain-instruments/{id}")
public class ChainInstrumentRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
//  private static Logger log = LoggerFactory.getLogger(ChainInstrumentRecordResource.class);
  private final ChainInstrumentDAO chainInstrumentDAO = injector.getInstance(ChainInstrumentDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   * Get one ChainInstrument by id
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);
    try {
      JSONObject result = chainInstrumentDAO.readOne(access, ULong.valueOf(id));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(ChainInstrument.KEY_ONE, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return httpResponseProvider.notFound("Chain Instrument");
      }

    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
  }

  /**
   * Delete one ChainInstrument
   *
   * @return application/json response.
   */
  // TODO [hub] Return 404 if the chain instrument is not found.
  @DELETE
  @RolesAllowed({Role.ADMIN})
  public Response delete(@Context ContainerRequestContext crc) {
    AccessControl access = AccessControl.fromContext(crc);
    try {
      chainInstrumentDAO.delete(access, ULong.valueOf(id));
      return Response.accepted("{}").build();
    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
  }

}
