// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.voice;

import com.google.common.collect.ImmutableList;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.VoiceDAO;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.model.voice.Voice;
import io.xj.core.model.voice.VoiceWrapper;
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
 Voices
 */
@Path("voices")
public class VoiceIndexResource extends HubResource {
  private final VoiceDAO voiceDAO = injector.getInstance(VoiceDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("sequenceId")
  String sequenceId;

  /**
   Get all voices.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (Objects.isNull(sequenceId) || sequenceId.isEmpty()) {
      return response.notAcceptable("Sequence id is required");
    }

    try {
      return response.readMany(
        Voice.KEY_MANY,
        voiceDAO.readAll(
          Access.fromContext(crc),
          ImmutableList.of(new BigInteger(sequenceId))));

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
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(VoiceWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        Voice.KEY_MANY,
        Voice.KEY_ONE,
        voiceDAO.create(
          Access.fromContext(crc),
          data.getVoice()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
