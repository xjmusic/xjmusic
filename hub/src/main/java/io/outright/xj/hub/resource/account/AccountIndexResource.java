package io.outright.xj.hub.resource.account;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.config.Exposure;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.output.JSONOutputProvider;
import io.outright.xj.core.model.account.Account;
import io.outright.xj.core.model.account.AccountWrapper;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.tables.records.AccountRecord;
import io.outright.xj.hub.HubModule;
import io.outright.xj.hub.controller.account.AccountController;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
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

/**
 * Accounts
 */
@Path("accounts")
public class AccountIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule(), new HubModule());
  private static Logger log = LoggerFactory.getLogger(AccountIndexResource.class);
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

    JSONArray accounts;
    try {
      accounts = accountController.readAll();
    } catch (Exception e) {
      log.error("Exception", e);
      return Response.serverError().build();
    }

    if (accounts != null) {
      return Response
        .accepted(jsonOutputProvider.wrap(Account.KEY_MANY, accounts).toString())
        .type(MediaType.APPLICATION_JSON)
        .build();
    } else {
      return Response.noContent().build();
    }
  }

  /**
   * Create new account
   *
   * @param data with which to update Account record.
   * @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ADMIN})
  public Response createAccount(AccountWrapper data) {
    AccountRecord newAccount;

    try {
      newAccount = accountController.create(data);
    } catch (BusinessException e) {
      log.warn("BusinessException: " + e.getMessage());
      return Response
        .status(HttpStatus.SC_UNPROCESSABLE_ENTITY)
        .entity(jsonOutputProvider.wrapError(e.getMessage()).toString())
        .build();
    } catch (Exception e) {
      log.error(e.getClass().getName(), e);
      return Response.serverError().build();
    }

    return Response
      .created(Exposure.apiURI(Account.KEY_MANY + "/" + newAccount.getId().toString()))
      .entity(jsonOutputProvider.wrap(Account.KEY_ONE,
        jsonOutputProvider.objectFromMap(newAccount.intoMap())).toString())
      .build();
  }


}
