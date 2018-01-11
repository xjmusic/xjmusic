// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.account_user;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.dao.AccountUserDAO;
import io.xj.core.model.account_user.AccountUser;
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
 Account record
 */
@Path("account-users/{id}")
public class AccountUserOneResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final AccountUserDAO accountUserDAO = injector.getInstance(AccountUserDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one AccountUser by id

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({UserRoleType.USER})
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        AccountUser.KEY_ONE,
        accountUserDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Delete one AccountUser by accountId and userId

   @return application/json response.
   */
  @DELETE
  @RolesAllowed({UserRoleType.ADMIN})
  public Response delete(@Context ContainerRequestContext crc) {
    try {
      accountUserDAO.delete(Access.fromContext(crc), new BigInteger(id));
      return Response.accepted("{}").build();
    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
