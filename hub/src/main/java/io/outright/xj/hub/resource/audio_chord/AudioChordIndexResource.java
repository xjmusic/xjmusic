// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.audio_chord;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.AudioChordDAO;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.model.audio_chord.AudioChord;
import io.outright.xj.core.model.audio_chord.AudioChordWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jooq.types.ULong;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * AudioChords
 */
@Path("audio-chords")
public class AudioChordIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private static Logger log = LoggerFactory.getLogger(AudioChordIndexResource.class);
  private final AudioChordDAO audioChordDAO = injector.getInstance(AudioChordDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("audioId")
  String audioId;

  /**
   * Get all audioChords.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);

    if (audioId == null || audioId.length() == 0) {
      return httpResponseProvider.notAcceptable("Audio id is required");
    }

    try {
      JSONArray result = audioChordDAO.readAllIn(access, ULong.valueOf(audioId));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(AudioChord.KEY_MANY, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return Response.noContent().build();
      }

    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
  }

  /**
   * Create new audioChord
   *
   * @param data with which to update AudioChord record.
   * @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST})
  public Response create(AudioChordWrapper data, @Context ContainerRequestContext crc) {
    AccessControl access = AccessControl.fromContext(crc);
    try {
      JSONObject newEntity = audioChordDAO.create(access, data);
      return Response
        .created(Exposure.apiURI(AudioChord.KEY_MANY + "/" + newEntity.get(Entity.KEY_ID)))
        .entity(JSON.wrap(AudioChord.KEY_ONE, newEntity).toString())
        .build();

    } catch (Exception e) {
      return httpResponseProvider.failureToCreate(e);
    }
  }

}
