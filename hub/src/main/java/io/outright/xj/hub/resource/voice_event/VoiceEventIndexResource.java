// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.voice_event;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.VoiceEventDAO;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.model.voice_event.VoiceEvent;
import io.outright.xj.core.model.voice_event.VoiceEventWrapper;
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
 * VoiceEvents
 */
@Path("voice-events")
public class VoiceEventIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  //  private static Logger log = LoggerFactory.getLogger(VoiceEventIndexResource.class);
  private final VoiceEventDAO voiceEventDAO = injector.getInstance(VoiceEventDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("voice")
  String voiceId;

  /**
   * Get all voiceEvents.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);

    if (voiceId == null || voiceId.length() == 0) {
      return httpResponseProvider.notAcceptable("Voice id is required");
    }

    try {
      JSONArray result = voiceEventDAO.readAllIn(access, ULong.valueOf(voiceId));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(VoiceEvent.KEY_MANY, result).toString())
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
   * Create new voiceEvent
   *
   * @param data with which to update VoiceEvent record.
   * @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST})
  public Response create(VoiceEventWrapper data, @Context ContainerRequestContext crc) {
    AccessControl access = AccessControl.fromContext(crc);
    try {
      JSONObject newEntity = voiceEventDAO.create(access, data);
      return Response
        .created(Exposure.apiURI(VoiceEvent.KEY_MANY + "/" + newEntity.get(Entity.KEY_ID)))
        .entity(JSON.wrap(VoiceEvent.KEY_ONE, newEntity).toString())
        .build();

    } catch (Exception e) {
      return httpResponseProvider.failureToCreate(e);
    }
  }

}
