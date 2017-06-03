// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.audio;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.AudioDAO;
import io.outright.xj.core.model.audio.Audio;
import io.outright.xj.core.model.audio.AudioWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

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

/**
 Audio record
 */
@Path("audios/{id}")
public class AudioRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final AudioDAO DAO = injector.getInstance(AudioDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Read one audio

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        Audio.KEY_ONE,
        DAO.readOne(
          Access.fromContext(crc),
          ULong.valueOf(id)));

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
  @RolesAllowed({Role.ARTIST})
  public Response update(AudioWrapper data, @Context ContainerRequestContext crc) {
    try {
      DAO.update(Access.fromContext(crc), ULong.valueOf(id), data.getAudio());
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return response.failureToUpdate(e);
    }
  }

  /**
   Delete one audio
   [#294] Eraseworker finds Links and Audio in deleted state and actually deletes the records, child entities and S3 objects
   Hub DELETE /audios/<id> is actually a state update to ERASE

   @return Response
   */
  @DELETE
  @RolesAllowed({Role.ARTIST})
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      DAO.erase(Access.fromContext(crc), ULong.valueOf(id));
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
  @RolesAllowed({Role.ARTIST})
  public Response uploadOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      JSONObject result = DAO.uploadOne(Access.fromContext(crc), ULong.valueOf(id));
      if (result != null) {
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
