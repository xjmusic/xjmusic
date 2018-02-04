// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.audio_event;

import com.google.common.collect.ImmutableList;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.AudioEventDAO;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.model.audio_event.AudioEventWrapper;
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
 AudioEvents
 */
@Path("audio-events")
public class AudioEventIndexResource extends HubResource {
  private final AudioEventDAO audioEventDAO = injector.getInstance(AudioEventDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("audioId")
  String audioId;

  /**
   Get all audioEvents.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    if (Objects.isNull(audioId) || audioId.isEmpty()) {
      return response.notAcceptable("Audio id is required");
    }

    try {
      return response.readMany(
        AudioEvent.KEY_MANY,
        audioEventDAO.readAll(
          Access.fromContext(crc),
          ImmutableList.of(new BigInteger(audioId))));

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
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(AudioEventWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        AudioEvent.KEY_MANY,
        AudioEvent.KEY_ONE,
        audioEventDAO.create(
          Access.fromContext(crc),
          data.getAudioEvent()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
