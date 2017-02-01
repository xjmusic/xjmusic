// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.idea_meme;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.IdeaMemeDAO;
import io.outright.xj.core.model.idea_meme.IdeaMeme;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;
import io.outright.xj.hub.HubModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.http.HttpStatus;
import org.jooq.types.ULong;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Idea record
 */
@Path("idea-memes/{id}")
public class IdeaMemeRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule(), new HubModule());
  private static Logger log = LoggerFactory.getLogger(IdeaMemeRecordResource.class);
  private final IdeaMemeDAO ideaMemeDAO = injector.getInstance(IdeaMemeDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String ideaMemeId;

  /**
   * Get one IdeaMeme by ideaId and memeId
   * TODO: Return 404 if the idea is not found.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);

    JSONObject result;
    try {
      result = ideaMemeDAO.readOneAble(access, ULong.valueOf(ideaMemeId));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(IdeaMeme.KEY_ONE, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return httpResponseProvider.notFound("Idea Meme");
      }

    } catch (Exception e) {
      return Response.serverError().build();
    }
  }

  /**
   * Delete one IdeaMeme by ideaId and memeId
   * TODO: Return 404 if the idea is not found.
   *
   * @return application/json response.
   */
  @DELETE
  @RolesAllowed({Role.ARTIST})
  public Response deleteIdeaMeme(@Context ContainerRequestContext crc) {
    AccessControl access = AccessControl.fromContext(crc);

    try {
      ideaMemeDAO.delete(access, ULong.valueOf(ideaMemeId));
    } catch (BusinessException e) {
      log.warn("BusinessException: " + e.getMessage());
      return Response
        .status(HttpStatus.SC_BAD_REQUEST)
        .entity(JSON.wrapError(e.getMessage()).toString())
        .build();
    } catch (Exception e) {
      log.error(e.getClass().getName(), e);
      return Response.serverError().build();
    }

    return Response.accepted("{}").build();
  }

}
