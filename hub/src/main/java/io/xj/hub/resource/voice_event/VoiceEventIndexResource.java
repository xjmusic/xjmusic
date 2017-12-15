// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.voice_event;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.dao.VoiceEventDAO;
import io.xj.core.model.voice_event.VoiceEvent;
import io.xj.core.model.voice_event.VoiceEventWrapper;



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
 VoiceEvents
 */
@Path("voice-events")
public class VoiceEventIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final VoiceEventDAO voiceEventDAO = injector.getInstance(VoiceEventDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("voiceId")
  String voiceId;

  /**
   Get all voiceEvents.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (Objects.isNull(voiceId) || voiceId.isEmpty()) {
      return response.notAcceptable("Voice id is required");
    }

    try {
      return response.readMany(
        VoiceEvent.KEY_MANY,
        voiceEventDAO.readAll(
          Access.fromContext(crc),
          new BigInteger(voiceId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new voiceEvent

   @param data with which to update VoiceEvent record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(VoiceEventWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        VoiceEvent.KEY_MANY,
        VoiceEvent.KEY_ONE,
        voiceEventDAO.create(
          Access.fromContext(crc),
          data.getVoiceEvent()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
