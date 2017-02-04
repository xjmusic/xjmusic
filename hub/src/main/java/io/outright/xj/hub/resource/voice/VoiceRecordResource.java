// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.voice;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.VoiceDAO;
import io.outright.xj.core.model.voice.Voice;
import io.outright.xj.core.model.voice.VoiceWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jooq.types.ULong;
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
 * Voice record
 */
@Path("voices/{id}")
public class VoiceRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  //  private static Logger log = LoggerFactory.getLogger(VoiceRecordResource.class);
  private final VoiceDAO voiceDAO = injector.getInstance(VoiceDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   * Get one voice.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);
    try {
      JSONObject result = voiceDAO.readOne(access, ULong.valueOf(id));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(Voice.KEY_ONE, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return httpResponseProvider.notFound("Voice");
      }

    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
  }

  /**
   * Update one voice
   *
   * @param data with which to update Voice record.
   * @return Response
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST})
  public Response update(VoiceWrapper data, @Context ContainerRequestContext crc) {
    AccessControl access = AccessControl.fromContext(crc);
    try {
      voiceDAO.update(access, ULong.valueOf(id), data);
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return httpResponseProvider.failureToUpdate(e);
    }
  }

  /**
   * Delete one voice
   *
   * @return Response
   */
  @DELETE
  @RolesAllowed({Role.ARTIST})
  public Response delete(@Context ContainerRequestContext crc) {
    AccessControl access = AccessControl.fromContext(crc);

    try {
      voiceDAO.delete(access, ULong.valueOf(id));
      return Response.accepted("{}").build();

    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
  }

}
