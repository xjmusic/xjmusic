// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.instrument_meme;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.InstrumentMemeDAO;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.model.instrument_meme.InstrumentMeme;
import io.outright.xj.core.model.instrument_meme.InstrumentMemeWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 Instrument record
 */
@Path("instrument-memes")
public class InstrumentMemeIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  //  private static Logger log = LoggerFactory.getLogger(InstrumentMemeIndexResource.class);
  private final InstrumentMemeDAO instrumentMemeDAO = injector.getInstance(InstrumentMemeDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("instrumentId")
  String instrumentId;

  /**
   Get Memes in one instrument.
   TODO: Return 404 if the instrument is not found.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);

    if (instrumentId == null || instrumentId.length() == 0) {
      return httpResponseProvider.notAcceptable("Instrument id is required");
    }

    try {
      JSONArray result = instrumentMemeDAO.readAllIn(access, ULong.valueOf(instrumentId));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(InstrumentMeme.KEY_MANY, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return Response.noContent().build();
      }

    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
  }

  /**
   Create new instrument meme

   @param data with which to update Instrument record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST})
  public Response create(InstrumentMemeWrapper data, @Context ContainerRequestContext crc) {
    AccessControl access = AccessControl.fromContext(crc);
    try {
      JSONObject result = instrumentMemeDAO.create(access, data);
      return Response
        .created(Exposure.apiURI(InstrumentMeme.KEY_MANY + "/" + result.get(Entity.KEY_ID)))
        .entity(JSON.wrap(InstrumentMeme.KEY_ONE, result).toString())
        .build();

    } catch (Exception e) {
      return httpResponseProvider.failureToCreate(e);
    }
  }

}
