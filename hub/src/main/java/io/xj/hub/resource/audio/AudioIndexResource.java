// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.hub.resource.audio;

import io.xj.core.CoreModule;
import io.xj.core.app.access.impl.Access;
import io.xj.core.app.server.HttpResponseProvider;
import io.xj.core.dao.AudioDAO;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio.AudioWrapper;
import io.xj.core.model.role.Role;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

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
 Audios
 */
@Path("audios")
public class AudioIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final AudioDAO DAO = injector.getInstance(AudioDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("instrumentId")
  String instrumentId;

  /**
   Get all audios.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    if (instrumentId == null || instrumentId.length() == 0) {
      return response.notAcceptable("Instrument id is required");
    }

    try {
      return response.readMany(
        Audio.KEY_MANY,
        DAO.readAll(
          Access.fromContext(crc),
          ULong.valueOf(instrumentId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new audio

   @param data with which to update Audio record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST})
  public Response create(AudioWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        Audio.KEY_MANY,
        Audio.KEY_ONE,
        DAO.create(
          Access.fromContext(crc),
          data.getAudio()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
