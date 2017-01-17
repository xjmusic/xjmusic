package io.outright.xj.hub.resource.accounts;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.Role;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.app.output.JSONOutputProvider;
import io.outright.xj.hub.HubModule;
import io.outright.xj.hub.controller.account.AccountController;
import io.outright.xj.core.model.account.AccountWrapper;
import org.apache.http.HttpStatus;
import org.jooq.Record;
import org.jooq.types.ULong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Account record
 */
@Path("accounts/{accountId}")
public class AccountRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule(), new HubModule());
  private static Logger log = LoggerFactory.getLogger(AccountRecordResource.class);
  private final AccountController accountController = injector.getInstance(AccountController.class);
  private final JSONOutputProvider jsonOutputProvider = injector.getInstance(JSONOutputProvider.class);

  @PathParam("accountId")
  private String accountId;

  /**
   * Get one account.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ADMIN})
  public Response readOneAccount() throws IOException {

    Record account;
    try {
      account = accountController.fetchAccount(ULong.valueOf(accountId));
    } catch (Exception e) {
      return Response.serverError().build();
    }

    if (account != null) {
      return Response
        .accepted(jsonOutputProvider.Record("account", account.intoMap()).toString())
        .type(MediaType.APPLICATION_JSON)
        .build();
    } else {
      return Response.noContent().build();
    }
  }

  /**
   * Update one account
   * @param data with which to update Account record.
   * @return Response
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ADMIN})
  public Response updateAccount(AccountWrapper data) {

    try {
      accountController.updateAccount(ULong.valueOf(accountId), data);
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

    return Response.accepted("{}").build();
  }

  /**
   * Delete one account
   * @param data with which to delete Account record.
   * @return Response
   */
  @DELETE
  @RolesAllowed({Role.ADMIN})
  public Response deleteAccount() {

    try {
      accountController.deleteAccount(ULong.valueOf(accountId));
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

    return Response.accepted("{}").build();
  }

}
