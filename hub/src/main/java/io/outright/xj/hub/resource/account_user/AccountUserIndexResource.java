package io.outright.xj.hub.resource.account_user;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControlModule;
import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.model.Entity;
import io.outright.xj.core.model.account_user.AccountUser;
import io.outright.xj.core.model.account_user.AccountUserWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;
import io.outright.xj.hub.HubModule;
import io.outright.xj.core.dao.AccountUserDAO;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * Account record
 */
@Path("account-users")
public class AccountUserIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule(), new HubModule());
  private static Logger log = LoggerFactory.getLogger(AccountUserIndexResource.class);
  private final AccountUserDAO accountUserDAO = injector.getInstance(AccountUserDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("account")
  String accountId;

  /**
   * Get Users in one account.
   * TODO: Return 404 if the account is not found.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    AccessControlModule access = AccessControlModule.fromContext(crc);
    JSONArray result;

    if (accountId == null || accountId.length() == 0) {
      return notAcceptable("Account id is required");
    }

    if (!access.isGrantedAccount(accountId)) {
      return httpResponseProvider.unauthorized();
    }

    try {
      result = accountUserDAO.readAll(ULong.valueOf(accountId));
    } catch (Exception e) {
      log.error(e.getClass().getName(), e);
      return Response.serverError().build();
    }

    if (result != null) {
      return Response
        .accepted(JSON.wrap(AccountUser.KEY_MANY, result).toString())
        .type(MediaType.APPLICATION_JSON)
        .build();
    } else {
      return Response.noContent().build();
    }
  }

  /**
   * Create new account user
   *
   * @param data with which to update Account record.
   * @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ADMIN})
  public Response create(AccountUserWrapper data) {
    JSONObject result;

    try {
      result = accountUserDAO.create(data);
    } catch (BusinessException e) {
      log.warn("BusinessException: " + e.getMessage());
      return Response
        .status(HttpStatus.SC_UNPROCESSABLE_ENTITY)
        .entity(JSON.wrapError(e.getMessage()).toString())
        .build();
    } catch (Exception e) {
      log.error(e.getClass().getName(), e);
      return Response.serverError().build();
    }

    return Response
      .created(Exposure.apiURI(AccountUser.KEY_MANY + "/" + result.get(Entity.KEY_ID)))
      .entity(JSON.wrap(AccountUser.KEY_ONE, result).toString())
      .build();
  }

  /**
   * Respond with not acceptable, account id required.
   *
   * @return Response
   */
  private Response notAcceptable(String message) {
    return Response
      .status(HttpStatus.SC_NOT_ACCEPTABLE)
      .entity(JSON.wrapError(message).toString())
      .build();
  }

}
