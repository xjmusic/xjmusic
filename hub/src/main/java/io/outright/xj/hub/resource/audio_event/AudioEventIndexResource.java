// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.audio_event;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.AudioEventDAO;
import io.outright.xj.core.model.audio_event.AudioEvent;
import io.outright.xj.core.model.audio_event.AudioEventWrapper;
import io.outright.xj.core.model.role.Role;

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
 AudioEvents
 */
@Path("audio-events")
public class AudioEventIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final AudioEventDAO DAO = injector.getInstance(AudioEventDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("audioId")
  String audioId;

  /**
   Get all audioEvents.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    if (audioId == null || audioId.length() == 0) {
      return response.notAcceptable("Audio id is required");
    }

    try {
      return response.readMany(
        AudioEvent.KEY_MANY,
        DAO.readAll(
          Access.fromContext(crc),
          ULong.valueOf(audioId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new audioEvent

   @param data with which to update AudioEvent record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST})
  public Response create(AudioEventWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        AudioEvent.KEY_MANY,
        AudioEvent.KEY_ONE,
        DAO.create(
          Access.fromContext(crc),
          data.getAudioEvent()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
