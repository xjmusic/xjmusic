// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.chain_library;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.ChainLibraryDAO;
import io.xj.core.model.chain_library.ChainLibrary;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.server.HttpResponseProvider;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.math.BigInteger;

/**
 Chain Library record
 */
@Path("chain-libraries/{id}")
public class ChainLibraryOneResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final ChainLibraryDAO chainLibraryDAO = injector.getInstance(ChainLibraryDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one ChainLibrary by id

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.USER)
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        ChainLibrary.KEY_ONE,
        chainLibraryDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Delete one ChainLibrary

   @return application/json response.
   */
  @DELETE
  @RolesAllowed({UserRoleType.ARTIST, UserRoleType.ENGINEER, UserRoleType.ADMIN})
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      chainLibraryDAO.delete(Access.fromContext(crc), new BigInteger(id));
      return Response.accepted("{}").build();
    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
