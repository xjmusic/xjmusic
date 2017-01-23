package io.outright.xj.hub.resource.account;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.app.output.JSONOutputProvider;
import io.outright.xj.core.model.account.Account;
import io.outright.xj.core.model.account.AccountWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.tables.records.AccountRecord;
import io.outright.xj.hub.HubModule;
import io.outright.xj.hub.controller.account.AccountController;
import org.apache.http.HttpStatus;
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
@Path("accounts/{id}")
public class AccountRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule(), new HubModule());
  private static Logger log = LoggerFactory.getLogger(AccountRecordResource.class);
  private final AccountController accountController = injector.getInstance(AccountController.class);
  private final JSONOutputProvider jsonOutputProvider = injector.getInstance(JSONOutputProvider.class);

  @PathParam("id") String accountId;

  /**
   * Get one account.
   * TODO: Return 404 if the account is not found.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ADMIN})
  public Response readOneAccount() throws IOException {
    AccountRecord account;
    try {
      account = accountController.read(ULong.valueOf(accountId));
    } catch (Exception e) {
      return Response.serverError().build();
    }

    if (account != null) {
      return Response
        .accepted(jsonOutputProvider.wrap(Account.KEY_ONE,
          jsonOutputProvider.objectFromMap(account.intoMap())).toString())
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
      accountController.update(ULong.valueOf(accountId), data);
    } catch (BusinessException e) {
      log.warn("BusinessException: " + e.getMessage());
      return Response
        .status(HttpStatus.SC_UNPROCESSABLE_ENTITY)
        .entity(jsonOutputProvider.wrapError(e.getMessage()).toString())
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
   * @return Response
   */
  @DELETE
  @RolesAllowed({Role.ADMIN})
  public Response deleteAccount() {

    try {
      accountController.delete(ULong.valueOf(accountId));
    } catch (BusinessException e) {
      log.warn("BusinessException: " + e.getMessage());
      return Response
        .status(HttpStatus.SC_BAD_REQUEST)
        .entity(jsonOutputProvider.wrapError(e.getMessage()).toString())
        .build();
    } catch (Exception e) {
      log.error(e.getClass().getName(), e);
      return Response.serverError().build();
    }

    return Response.accepted("{}").build();
  }

}
