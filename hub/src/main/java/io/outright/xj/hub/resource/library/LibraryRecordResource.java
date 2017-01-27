// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.library;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.LibraryDAO;
import io.outright.xj.core.model.library.Library;
import io.outright.xj.core.model.library.LibraryWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;
import io.outright.xj.hub.HubModule;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.apache.http.HttpStatus;
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
 * Library record
 */
@Path("libraries/{id}")
public class LibraryRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule(), new HubModule());
  private static Logger log = LoggerFactory.getLogger(LibraryRecordResource.class);
  private final LibraryDAO libraryDAO = injector.getInstance(LibraryDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id") String libraryId;

  /**
   * Get one library.
   * TODO: Return 404 if the library is not found.
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
      result = libraryDAO.readOneAble(access, ULong.valueOf(libraryId));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(Library.KEY_ONE, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return httpResponseProvider.notFound("Library");
      }

    } catch (Exception e) {
      return Response.serverError().build();
    }
  }

  /**
   * Update one library
   *
   * @param data with which to update Library record.
   * @return Response
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ADMIN})
  public Response update(LibraryWrapper data) {

    try {
      libraryDAO.update(ULong.valueOf(libraryId), data);
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
   * Delete one library
   *
   * @return Response
   */
  @DELETE
  @RolesAllowed({Role.ADMIN})
  public Response delete() {

    try {
      libraryDAO.delete(ULong.valueOf(libraryId));
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
