// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.voice_event;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.VoiceEventDAO;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.model.voice_event.VoiceEvent;
import io.outright.xj.core.model.voice_event.VoiceEventWrapper;

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
 VoiceEvents
 */
@Path("voice-events")
public class VoiceEventIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final VoiceEventDAO DAO = injector.getInstance(VoiceEventDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("voiceId")
  String voiceId;

  /**
   Get all voiceEvents.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (voiceId == null || voiceId.length() == 0) {
      return response.notAcceptable("Voice id is required");
    }

    try {
      return response.readMany(
        VoiceEvent.KEY_MANY,
        DAO.readAll(
          Access.fromContext(crc),
          ULong.valueOf(voiceId)));

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
  @RolesAllowed({Role.ARTIST})
  public Response create(VoiceEventWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        VoiceEvent.KEY_MANY,
        VoiceEvent.KEY_ONE,
        DAO.create(
          Access.fromContext(crc),
          data.getVoiceEvent()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
