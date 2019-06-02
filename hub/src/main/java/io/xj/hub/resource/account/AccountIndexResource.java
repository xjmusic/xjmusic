// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.account;

import com.google.common.collect.Lists;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.AccountDAO;
import io.xj.core.model.account.Account;
import io.xj.core.model.account.AccountWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 Accounts
 */
@Path("accounts")
public class AccountIndexResource extends HubResource {
  private final AccountDAO accountDAO = injector.getInstance(AccountDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  /**
   Get all accounts.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.USER)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readMany(
        Account.KEY_MANY,
        accountDAO.readAll(
          Access.fromContext(crc),
          Lists.newArrayList()));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new account

   @param data with which to update Account record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ADMIN)
  public Response create(AccountWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        Account.KEY_MANY,
        Account.KEY_ONE,
        accountDAO.create(
          Access.fromContext(crc),
          data.getAccount()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }


}
