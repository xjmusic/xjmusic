package io.outright.xj.hub.resource.accounts;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.Role;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.app.output.JSONOutputProvider;
import io.outright.xj.core.model.account.AccountWrapper;
import io.outright.xj.core.tables.records.AccountRecord;
import io.outright.xj.hub.HubModule;
import io.outright.xj.hub.controller.account.AccountController;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Accounts
 */
@Path("accounts")
public class AccountsIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule(), new HubModule());
  private static Logger log = LoggerFactory.getLogger(AccountsIndexResource.class);
  private final AccountController accountController = injector.getInstance(AccountController.class);
  private final JSONOutputProvider jsonOutputProvider = injector.getInstance(JSONOutputProvider.class);

  /**
   * Get all accounts.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ADMIN})
  public Response readAllAccounts(@Context ContainerRequestContext crc) throws IOException {

    ResultSet accounts;
    try {
      accounts = accountController.fetchAccounts();
    } catch (Exception e) {
      return Response.serverError().build();
    }

    if (accounts != null) {
      try {
        return Response
          .accepted(jsonOutputProvider.ListOf("accounts", accounts).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } catch (SQLException e) {
        log.error("BuildJSON.from(<ResultSet>)",e);
        return Response.serverError().build();
      }
    } else {
      return Response.noContent().build();
    }
  }

  /**
   * Create new account
   * @param data with which to update Account record.
   * @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ADMIN})
  public Response createAccount(AccountWrapper data) {
    AccountRecord newAccount;

    try {
      newAccount = accountController.createAccount(data);
    } catch (BusinessException e) {
      log.warn("BusinessException: " + e.getMessage());
      return Response
        .status(HttpStatus.SC_UNPROCESSABLE_ENTITY)
        .entity(jsonOutputProvider.Error(e.getMessage()).toString())
        .build();
    } catch (DatabaseException e) {
      log.error("DatabaseException", e);
      return Response.serverError().build();
    } catch (ConfigException e) {
      log.error("ConfigException", e);
      return Response.serverError().build();
    } catch (Exception e) {
      log.error("Exception", e);
      return Response.serverError().build();
    }


    return Response
      .created(URI.create("accounts/" + newAccount.getId().toString()))
      .entity(jsonOutputProvider.Record("account", newAccount.intoMap()).toString())
      .build();
  }


}
