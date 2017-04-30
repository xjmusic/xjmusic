// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.idea_meme;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.IdeaMemeDAO;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.model.idea_meme.IdeaMeme;
import io.outright.xj.core.model.idea_meme.IdeaMemeWrapper;
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
 Idea record
 */
@Path("idea-memes")
public class IdeaMemeIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  //  private static Logger log = LoggerFactory.getLogger(IdeaMemeIndexResource.class);
  private final IdeaMemeDAO ideaMemeDAO = injector.getInstance(IdeaMemeDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("ideaId")
  String ideaId;

  /**
   Get Memes in one idea.
   TODO: Return 404 if the idea is not found.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);

    if (ideaId == null || ideaId.length() == 0) {
      return httpResponseProvider.notAcceptable("Idea id is required");
    }

    try {
      JSONArray result = ideaMemeDAO.readAllIn(access, ULong.valueOf(ideaId));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(IdeaMeme.KEY_MANY, result).toString())
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
   Create new idea meme

   @param data with which to update Idea record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST})
  public Response create(IdeaMemeWrapper data, @Context ContainerRequestContext crc) {
    AccessControl access = AccessControl.fromContext(crc);
    try {
      JSONObject result = ideaMemeDAO.create(access, data);
      return Response
        .created(Exposure.apiURI(IdeaMeme.KEY_MANY + "/" + result.get(Entity.KEY_ID)))
        .entity(JSON.wrap(IdeaMeme.KEY_ONE, result).toString())
        .build();

    } catch (Exception e) {
      return httpResponseProvider.failureToCreate(e);
    }
  }

}
