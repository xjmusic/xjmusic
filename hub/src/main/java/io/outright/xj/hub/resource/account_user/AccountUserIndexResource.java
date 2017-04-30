// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.account_user;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.AccountUserDAO;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.model.account_user.AccountUser;
import io.outright.xj.core.model.account_user.AccountUserWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.json.JSONObject;

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
 Account record
 */
@Path("account-users")
public class AccountUserIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  //  private static Logger log = LoggerFactory.getLogger(AccountUserIndexResource.class);
  private final AccountUserDAO accountUserDAO = injector.getInstance(AccountUserDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("accountId")
  String accountId;

  /**
   Get Users in one account.

   @return application/json response.
   */
  // TODO [hub] Return 404 if the account is not found.
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);

    if (accountId == null || accountId.length() == 0) {
      return httpResponseProvider.notAcceptable("Account id is required");
    }

    try {
      JSONArray result = accountUserDAO.readAllIn(access, ULong.valueOf(accountId));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(AccountUser.KEY_MANY, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return Response.noContent().build();
      }

    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
  }

  /**
   Create new account user

   @param data with which to update Account record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ADMIN})
  public Response create(AccountUserWrapper data, @Context ContainerRequestContext crc) {
    AccessControl access = AccessControl.fromContext(crc);
    try {
      JSONObject result = accountUserDAO.create(access, data);
      return Response
        .created(Exposure.apiURI(AccountUser.KEY_MANY + "/" + result.get(Entity.KEY_ID)))
        .entity(JSON.wrap(AccountUser.KEY_ONE, result).toString())
        .build();

    } catch (Exception e) {
      return httpResponseProvider.failureToCreate(e);
    }
  }

}
