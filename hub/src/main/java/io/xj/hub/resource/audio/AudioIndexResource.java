// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.audio;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.AudioDAO;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio.AudioWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.server.HttpResponseProvider;

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
import java.math.BigInteger;
import java.util.Objects;

/**
 Audios
 */
@Path("audios")
public class AudioIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final AudioDAO audioDAO = injector.getInstance(AudioDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("instrumentId")
  String instrumentId;

  @QueryParam("cloneId")
  String cloneId;

  /**
   Get all audios.

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
        Audio.KEY_MANY,
        audioDAO.readAll(
          Access.fromContext(crc),
          new BigInteger(instrumentId)));

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
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(AudioWrapper data, @Context ContainerRequestContext crc) {
    try {
      Audio created;
      if (Objects.nonNull(cloneId)) {
        created = audioDAO.clone(
          Access.fromContext(crc),
          new BigInteger(cloneId),
          data.getAudio());
      } else {
        created = audioDAO.create(
          Access.fromContext(crc),
          data.getAudio());
      }
      return response.create(
        Audio.KEY_MANY,
        Audio.KEY_ONE,
        created);

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
