// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.voice;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.VoiceDAO;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.model.voice.Voice;
import io.xj.core.model.voice.VoiceWrapper;
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
 Voice record
 */
@Path("voices/{id}")
public class VoiceOneResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final VoiceDAO voiceDAO = injector.getInstance(VoiceDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one voice.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        Voice.KEY_ONE,
        voiceDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Update one voice

   @param data with which to update Voice record.
   @return Response
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response update(VoiceWrapper data, @Context ContainerRequestContext crc) {
    try {
      voiceDAO.update(
        Access.fromContext(crc),
        new BigInteger(id),
        data.getVoice());
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failureToUpdate(e);
    }
  }

  /**
   Delete one voice

   @return Response
   */
  @DELETE
  @RolesAllowed(UserRoleType.ARTIST)
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      voiceDAO.destroy(
        Access.fromContext(crc),
        new BigInteger(id));
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
