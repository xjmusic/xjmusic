// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.audio_event;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.AudioEventDAO;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.model.audio_event.AudioEvent;
import io.outright.xj.core.model.audio_event.AudioEventWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.json.JSONObject;

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
 AudioEvents
 */
@Path("audio-events")
public class AudioEventIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  //  private static Logger log = LoggerFactory.getLogger(AudioEventIndexResource.class);
  private final AudioEventDAO audioEventDAO = injector.getInstance(AudioEventDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("audioId")
  String audioId;

  /**
   Get all audioEvents.

   @return application/json response.
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
      JSONArray result = audioEventDAO.readAllIn(access, ULong.valueOf(audioId));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(AudioEvent.KEY_MANY, result).toString())
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
   Create new audioEvent

   @param data with which to update AudioEvent record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST})
  public Response create(AudioEventWrapper data, @Context ContainerRequestContext crc) {
    AccessControl access = AccessControl.fromContext(crc);
    try {
      JSONObject newEntity = audioEventDAO.create(access, data);
      return Response
        .created(Exposure.apiURI(AudioEvent.KEY_MANY + "/" + newEntity.get(Entity.KEY_ID)))
        .entity(JSON.wrap(AudioEvent.KEY_ONE, newEntity).toString())
        .build();

    } catch (Exception e) {
      return httpResponseProvider.failureToCreate(e);
    }
  }

}
