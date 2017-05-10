// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.library;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.LibraryDAO;
import io.outright.xj.core.model.library.Library;
import io.outright.xj.core.model.library.LibraryWrapper;
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
 Libraries
 */
@Path("libraries")
public class LibraryIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final LibraryDAO DAO = injector.getInstance(LibraryDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("accountId")
  String accountId;

  /**
   Get all libraries.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (accountId == null || accountId.length() == 0) {
      return response.notAcceptable("Account id is required");
    }

    try {
      return response.readMany(
        Library.KEY_MANY,
        DAO.readAll(
          Access.fromContext(crc),
          ULong.valueOf(accountId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new library

   @param data with which to update Library record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ADMIN})
  public Response create(LibraryWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        Library.KEY_MANY,
        Library.KEY_ONE,
        DAO.create(
          Access.fromContext(crc),
          data.getLibrary()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
