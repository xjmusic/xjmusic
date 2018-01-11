// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.audio_chord;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.AudioChordDAO;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.model.audio_chord.AudioChordWrapper;
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
 AudioChords
 */
@Path("audio-chords")
public class AudioChordIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final AudioChordDAO audioChordDAO = injector.getInstance(AudioChordDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("audioId")
  String audioId;

  /**
   Get all audioChords.

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
        AudioChord.KEY_MANY,
        audioChordDAO.readAll(
          Access.fromContext(crc),
          new BigInteger(audioId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new audioChord

   @param data with which to update AudioChord record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(AudioChordWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        AudioChord.KEY_MANY,
        AudioChord.KEY_ONE,
        audioChordDAO.create(
          Access.fromContext(crc),
          data.getAudioChord()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
