// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.library;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.LibraryDAO;
import io.xj.core.model.library.Library;
import io.xj.core.model.library.LibraryWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.server.HttpResponseProvider;

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
import java.math.BigInteger;
import java.util.Objects;

/**
 Libraries
 */
@Path("libraries")
public class LibraryIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final LibraryDAO libraryDAO = injector.getInstance(LibraryDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("accountId")
  String accountId;

  /**
   Get all libraries.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (Objects.nonNull(accountId) && accountId.isEmpty()) {
      accountId = null;
    }

    try {
      return response.readMany(
        Library.KEY_MANY,
        libraryDAO.readAll(
          Access.fromContext(crc),
          Objects.nonNull(accountId) ? new BigInteger(accountId) : null));

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
  @RolesAllowed(UserRoleType.ADMIN)
  public Response create(LibraryWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        Library.KEY_MANY,
        Library.KEY_ONE,
        libraryDAO.create(
          Access.fromContext(crc),
          data.getLibrary()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
