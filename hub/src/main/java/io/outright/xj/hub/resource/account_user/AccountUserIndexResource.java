package io.outright.xj.hub.resource.account_user;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.output.JSONOutputProvider;
import io.outright.xj.core.model.account.Account;
import io.outright.xj.core.model.account_user.AccountUser;
import io.outright.xj.core.model.account_user.AccountUserWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.model.user.User;
import io.outright.xj.core.tables.records.AccountUserRecord;
import io.outright.xj.hub.HubModule;
import io.outright.xj.hub.controller.account_user.AccountUserController;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.http.HttpStatus;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Account record
 */
@Path("account-users")
public class AccountUserIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule(), new HubModule());
  private static Logger log = LoggerFactory.getLogger(AccountUserIndexResource.class);
  private final AccountUserController accountUserController = injector.getInstance(AccountUserController.class);
  private final JSONOutputProvider jsonOutputProvider = injector.getInstance(JSONOutputProvider.class);

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
  @RolesAllowed({Role.ADMIN})
  public Response readAll() throws IOException {
    ResultSet accountUsers;

    if (accountId == null || accountId.length() == 0) {
      return notAcceptableAccountIdRequired();
    }

    try {
      accountUsers = accountUserController.readAll(ULong.valueOf(accountId));
    } catch (Exception e) {
      return notAcceptableAccountIdRequired();
    }

    if (accountUsers != null) {
      try {
        return Response
          .accepted(jsonOutputProvider.ListOf(AccountUser.KEY_MANY, accountUsers).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } catch (SQLException e) {
        log.error("SQLException", e);
        return Response.serverError().build();
      }
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
    AccountUserRecord newAccountUser;

    try {
      newAccountUser = accountUserController.create(data);
    } catch (BusinessException e) {
      log.warn("BusinessException: " + e.getMessage());
      return Response
        .status(HttpStatus.SC_UNPROCESSABLE_ENTITY)
        .entity(jsonOutputProvider.Error(e.getMessage()).toString())
        .build();
    } catch (Exception e) {
      log.error(e.getClass().getName(), e);
      return Response.serverError().build();
    }

    return Response
      .created(Exposure.apiURI(AccountUser.KEY_MANY + "/" + newAccountUser.getId().toString()))
      .entity(jsonOutputProvider.Record(AccountUser.KEY_ONE, newAccountUser.intoMap()).toString())
      .build();
  }

  /**
   * Respond with not acceptable, account id required.
   *
   * @return Response
   */
  private Response notAcceptableAccountIdRequired() {
    return Response
      .status(HttpStatus.SC_NOT_ACCEPTABLE)
      .entity(jsonOutputProvider.Error("Account id is required").toString())
      .build();
  }

}
