// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.account;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.AccountDAO;
import io.outright.xj.core.model.account.Account;
import io.outright.xj.core.model.account.AccountWrapper;
import io.outright.xj.core.model.role.Role;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
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
public class AccountIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final AccountDAO DAO = injector.getInstance(AccountDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  /**
   Get all accounts.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    try {
      return response.readMany(
        Account.KEY_MANY,
        DAO.readAll(
          Access.fromContext(crc)));

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
  @RolesAllowed({Role.ADMIN})
  public Response create(AccountWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        Account.KEY_MANY,
        Account.KEY_ONE,
        DAO.create(
          Access.fromContext(crc),
          data.getAccount()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }


}
