// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.library;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.LibraryDAO;
import io.xj.core.model.library.Library;
import io.xj.core.model.library.LibraryWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
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
public class LibraryIndexResource extends HubResource {
  private final LibraryDAO libraryDAO = injector.getInstance(LibraryDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("accountId")
  String accountId;

  /**
   Get all libraries.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readMany(
        Library.KEY_MANY,
        libraryDAO.readAll(
          Access.fromContext(crc),
          Objects.nonNull(accountId) && !accountId.isEmpty() ? ImmutableList.of(new BigInteger(accountId)) : Lists.newArrayList()));

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
  @RolesAllowed({UserRoleType.ADMIN, UserRoleType.ENGINEER})
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
