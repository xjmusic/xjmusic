// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.voice_event;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.VoiceEventDAO;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.model.voice_event.VoiceEvent;
import io.outright.xj.core.model.voice_event.VoiceEventWrapper;
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
 VoiceEvent record
 */
@Path("voice-events/{id}")
public class VoiceEventRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  //  private static Logger log = LoggerFactory.getLogger(VoiceEventRecordResource.class);
  private final VoiceEventDAO voiceEventDAO = injector.getInstance(VoiceEventDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one voiceEvent.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);

    try {
      JSONObject result = voiceEventDAO.readOne(access, ULong.valueOf(id));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(VoiceEvent.KEY_ONE, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return httpResponseProvider.notFound("Voice Event");
      }

    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
  }

  /**
   Update one voiceEvent

   @param data with which to update VoiceEvent record.
   @return Response
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST})
  public Response update(VoiceEventWrapper data, @Context ContainerRequestContext crc) {
    AccessControl access = AccessControl.fromContext(crc);
    try {
      voiceEventDAO.update(access, ULong.valueOf(id), data);
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return httpResponseProvider.failureToUpdate(e);
    }
  }

  /**
   Delete one voiceEvent

   @return Response
   */
  @DELETE
  @RolesAllowed({Role.ARTIST})
  public Response delete(@Context ContainerRequestContext crc) {
    AccessControl access = AccessControl.fromContext(crc);
    try {
      voiceEventDAO.delete(access, ULong.valueOf(id));
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
  }

}
