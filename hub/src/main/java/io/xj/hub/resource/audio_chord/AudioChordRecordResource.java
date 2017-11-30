// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.hub.resource.audio_chord;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.dao.AudioChordDAO;
import io.xj.core.model.audio_chord.AudioChord;
import io.xj.core.model.audio_chord.AudioChordWrapper;
import io.xj.core.model.role.Role;

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
 AudioChord record
 */
@Path("audio-chords/{id}")
public class AudioChordRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final AudioChordDAO DAO = injector.getInstance(AudioChordDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one audioChord.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        AudioChord.KEY_ONE,
        DAO.readOne(
          Access.fromContext(crc),
          ULong.valueOf(id)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Update one audioChord

   @param data with which to update AudioChord record.
   @return Response
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST})
  public Response update(AudioChordWrapper data, @Context ContainerRequestContext crc) {
    try {
      DAO.update(Access.fromContext(crc), ULong.valueOf(id), data.getAudioChord());
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failureToUpdate(e);
    }
  }

  /**
   Delete one audioChord

   @return Response
   */
  @DELETE
  @RolesAllowed({Role.ARTIST})
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      DAO.delete(Access.fromContext(crc), ULong.valueOf(id));
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
