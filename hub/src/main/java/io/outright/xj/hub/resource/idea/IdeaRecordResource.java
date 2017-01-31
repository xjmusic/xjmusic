// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.idea;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.IdeaDAO;
import io.outright.xj.core.model.idea.Idea;
import io.outright.xj.core.model.idea.IdeaWrapper;
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
 * Idea record
 */
@Path("ideas/{id}")
public class IdeaRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule(), new HubModule());
  private static Logger log = LoggerFactory.getLogger(IdeaRecordResource.class);
  private final IdeaDAO ideaDAO = injector.getInstance(IdeaDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String ideaId;

  /**
   * Get one idea.
   * TODO: Return 404 if the idea is not found.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);

    JSONObject result;
    try {
      result = ideaDAO.readOneAble(access, ULong.valueOf(ideaId));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(Idea.KEY_ONE, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return httpResponseProvider.notFound("Idea");
      }

    } catch (Exception e) {
      return Response.serverError().build();
    }
  }

  /**
   * Update one idea
   *
   * @param data with which to update Idea record.
   * @return Response
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST})
  public Response update(IdeaWrapper data, @Context ContainerRequestContext crc) {
    AccessControl access = AccessControl.fromContext(crc);

    try {
      ideaDAO.update(access, ULong.valueOf(ideaId), data);
      return Response.accepted("{}").build();

    } catch (BusinessException e) {
      log.warn("BusinessException: " + e.getMessage());
      return Response
        .status(HttpStatus.SC_UNPROCESSABLE_ENTITY)
        .entity(JSON.wrapError(e.getMessage()).toString())
        .build();

    } catch (DatabaseException e) {
      log.error("DatabaseException", e);
      return Response.serverError().build();

    } catch (ConfigException e) {
      log.error("ConfigException", e);
      return Response.serverError().build();

    } catch (Exception e) {
      log.error("Exception", e);
      return Response.serverError().build();
    }
  }

  /**
   * Delete one idea
   *
   * @return Response
   */
  @DELETE
  @RolesAllowed({Role.ADMIN})
  public Response delete() {

    try {
      ideaDAO.delete(ULong.valueOf(ideaId));
      return Response.accepted("{}").build();

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
  }

}
