// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.audio_event;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.AudioEventDAO;
import io.xj.core.model.audio_event.AudioEvent;
import io.xj.core.model.audio_event.AudioEventWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;

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
import java.math.BigInteger;

/**
 AudioEvent record
 */
@Path("audio-events/{id}")
public class AudioEventOneResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final AudioEventDAO audioEventDAO = injector.getInstance(AudioEventDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one audioEvent.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        AudioEvent.KEY_ONE,
        audioEventDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Update one audioEvent

   @param data with which to update AudioEvent record.
   @return Response
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response update(AudioEventWrapper data, @Context ContainerRequestContext crc) {
    try {
      audioEventDAO.update(Access.fromContext(crc), new BigInteger(id), data.getAudioEvent());
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failureToUpdate(e);
    }
  }

  /**
   Delete one audioEvent

   @return Response
   */
  @DELETE
  @RolesAllowed(UserRoleType.ARTIST)
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      audioEventDAO.destroy(Access.fromContext(crc), new BigInteger(id));
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
