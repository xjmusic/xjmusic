// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.audio;

import com.google.inject.Guice;
import com.google.inject.Injector;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.AudioDAO;
import io.xj.core.model.audio.Audio;
import io.xj.core.model.audio.AudioWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.transport.JSON;
import org.json.JSONObject;

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
 Audio record
 */
@Path("audios/{id}")
public class AudioOneResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final AudioDAO audioDAO = injector.getInstance(AudioDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Read one audio

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        Audio.KEY_ONE,
        audioDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Update one audio

   @param data with which to update Audio record.
   @return Response
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response update(AudioWrapper data, @Context ContainerRequestContext crc) {
    try {
      audioDAO.update(Access.fromContext(crc), new BigInteger(id), data.getAudio());
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failureToUpdate(e);
    }
  }

  /**
   Delete one audio
   [#294] Eraseworker finds Links and Audio in deleted state and actually deletes the records, child entities and S3 objects
   Hub DELETE /audios/# is actually a state update to ERASE

   @return Response
   */
  @DELETE
  @RolesAllowed(UserRoleType.ARTIST)
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      audioDAO.erase(Access.fromContext(crc), new BigInteger(id));
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Generate an Upload policy to upload the corresponding file to 3rd-party storage (e.g. Amazon S3)

   @return application/json response.
   */
  @GET
  @Path("/upload")
  @WebResult
  @RolesAllowed(UserRoleType.ARTIST)
  public Response uploadOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      JSONObject result = audioDAO.authorizeUpload(Access.fromContext(crc), new BigInteger(id));
      if (null != result) {
        return Response
          .accepted(JSON.wrap(Audio.KEY_ONE, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return response.notFound("Audio");
      }


    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
