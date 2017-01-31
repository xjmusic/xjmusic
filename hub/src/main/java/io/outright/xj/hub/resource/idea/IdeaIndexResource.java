// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.idea;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.dao.IdeaDAO;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.idea.IdeaWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;
import io.outright.xj.hub.HubModule;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.http.HttpStatus;
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
 * Ideas
 */
@Path("ideas")
public class IdeaIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule(), new HubModule());
  private static Logger log = LoggerFactory.getLogger(IdeaIndexResource.class);
  private final IdeaDAO ideaDAO = injector.getInstance(IdeaDAO.class);

  @QueryParam("library")
  String libraryId;

  /**
   * Get all ideas.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);

    if (libraryId == null || libraryId.length() == 0) {
      return notAcceptable("Library id is required");
    }

    JSONArray result;
    try {
      result = ideaDAO.readAllAble(access, ULong.valueOf(libraryId));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(Idea.KEY_MANY, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return Response.noContent().build();
      }

    } catch (Exception e) {
      log.error("Exception", e);
      return Response.serverError().build();
    }
  }

  /**
   * Create new idea
   *
   * @param data with which to update Idea record.
   * @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST})
  public Response create(IdeaWrapper data, @Context ContainerRequestContext crc) {
    AccessControl access = AccessControl.fromContext(crc);
    JSONObject newEntity;

    try {
      newEntity = ideaDAO.create(access, data);
    } catch (BusinessException e) {
      log.warn("BusinessException: " + e.getMessage());
      return Response
        .status(HttpStatus.SC_UNPROCESSABLE_ENTITY)
        .entity(JSON.wrapError(e.getMessage()).toString())
        .build();
    } catch (Exception e) {
      log.error(e.getClass().getName(), e);
      return Response.serverError().build();
    }

    return Response
      .created(Exposure.apiURI(Idea.KEY_MANY + "/" + newEntity.get(Entity.KEY_ID)))
      .entity(JSON.wrap(Idea.KEY_ONE, newEntity).toString())
      .build();
  }

  /**
   * Respond with not acceptable, library id required.
   *
   * @return Response
   */
  private Response notAcceptable(String message) {
    return Response
      .status(HttpStatus.SC_NOT_ACCEPTABLE)
      .entity(JSON.wrapError(message).toString())
      .build();
  }

}
