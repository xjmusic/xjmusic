// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.idea_meme;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.IdeaMemeDAO;
import io.outright.xj.core.model.idea_meme.IdeaMeme;
import io.outright.xj.core.model.idea_meme.IdeaMemeWrapper;
import io.outright.xj.core.model.role.Role;

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
 Idea record
 */
@Path("idea-memes")
public class IdeaMemeIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final IdeaMemeDAO DAO = injector.getInstance(IdeaMemeDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("ideaId")
  String ideaId;

  /**
   Get Memes in one idea.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (ideaId == null || ideaId.length() == 0) {
      return response.notAcceptable("Idea id is required");
    }

    try {
      return response.readMany(
        IdeaMeme.KEY_MANY,
        DAO.readAll(
          Access.fromContext(crc),
          ULong.valueOf(ideaId)));

    } catch (Exception e) {
      return response.failure(e);
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
    try {
      return response.create(
        IdeaMeme.KEY_MANY,
        IdeaMeme.KEY_ONE,
        DAO.create(
          Access.fromContext(crc),
          data.getIdeaMeme()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
