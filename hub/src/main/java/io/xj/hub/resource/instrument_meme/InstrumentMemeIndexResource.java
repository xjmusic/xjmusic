// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.instrument_meme;

import com.google.common.collect.ImmutableList;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.InstrumentMemeDAO;
import io.xj.core.model.instrument_meme.InstrumentMeme;
import io.xj.core.model.instrument_meme.InstrumentMemeWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

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
import java.math.BigInteger;
import java.util.Objects;

/**
 Instrument record
 */
@Path("instrument-memes")
public class InstrumentMemeIndexResource extends HubResource {
  private final InstrumentMemeDAO instrumentMemeDAO = injector.getInstance(InstrumentMemeDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("instrumentId")
  String instrumentId;

  /**
   Get Memes in one instrument.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (Objects.isNull(instrumentId) || instrumentId.isEmpty()) {
      return response.notAcceptable("Instrument id is required");
    }

    try {
      return response.readMany(
        InstrumentMeme.KEY_MANY,
        instrumentMemeDAO.readAll(
          Access.fromContext(crc),
          ImmutableList.of(new BigInteger(instrumentId))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new instrument meme

   @param data with which to update Instrument record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(InstrumentMemeWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        InstrumentMeme.KEY_MANY,
        InstrumentMeme.KEY_ONE,
        instrumentMemeDAO.create(
          Access.fromContext(crc),
          data.getInstrumentMeme()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
