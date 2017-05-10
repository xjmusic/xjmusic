// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.voice;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.VoiceDAO;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.model.voice.Voice;
import io.outright.xj.core.model.voice.VoiceWrapper;

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
 Voices
 */
@Path("voices")
public class VoiceIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final VoiceDAO DAO = injector.getInstance(VoiceDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("phaseId")
  String phaseId;

  /**
   Get all voices.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (phaseId == null || phaseId.length() == 0) {
      return response.notAcceptable("Phase id is required");
    }

    try {
      return response.readMany(
        Voice.KEY_MANY,
        DAO.readAll(
          Access.fromContext(crc),
          ULong.valueOf(phaseId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new voice

   @param data with which to update Voice record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST})
  public Response create(VoiceWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        Voice.KEY_MANY,
        Voice.KEY_ONE,
        DAO.create(
          Access.fromContext(crc),
          data.getVoice()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
