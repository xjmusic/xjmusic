// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.account_user;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.AccountUserDAO;
import io.xj.core.exception.CoreException;
import io.xj.core.model.account_user.AccountUser;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
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
public class AccountUserOneResource extends HubResource {
  private final AccountUserDAO accountUserDAO = injector.getInstance(AccountUserDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   Get one AccountUser by id

   @return application/json response.
   */
  @GET
  @RolesAllowed({UserRoleType.USER})
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readOne(
        AccountUser.KEY_ONE,
        accountUserDAO.readOne(
          Access.fromContext(crc),
          new BigInteger(id)));

    } catch (CoreException ignored) {
      return response.notFound("Account User");

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
      accountUserDAO.destroy(Access.fromContext(crc), new BigInteger(id));
      return Response.accepted("{}").build();
    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
