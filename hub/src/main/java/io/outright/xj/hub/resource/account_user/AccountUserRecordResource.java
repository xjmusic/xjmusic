package io.outright.xj.hub.resource.account_user;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.output.JSONOutputProvider;
import io.outright.xj.core.model.account_user.AccountUser;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.hub.HubModule;
import io.outright.xj.hub.controller.account_user.AccountUserController;
import org.apache.http.HttpStatus;
import org.jooq.types.ULong;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Account record
 */
@Path("account-users/{id}")
public class AccountUserRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule(), new HubModule());
  private static Logger log = LoggerFactory.getLogger(AccountUserRecordResource.class);
  private final AccountUserController accountUserController = injector.getInstance(AccountUserController.class);
  private final JSONOutputProvider jsonOutputProvider = injector.getInstance(JSONOutputProvider.class);

  @PathParam("id")
  String accountUserId;

  /**
   * Get one AccountUser by accountId and userId
   * TODO: Return 404 if the account is not found.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ADMIN})
  public Response read() throws IOException {
    JSONObject result;

    try {
      result = accountUserController.read(ULong.valueOf(accountUserId));
    } catch (Exception e) {
      return Response.serverError().build();
    }

    if (result != null) {
      return Response
        .accepted(jsonOutputProvider.wrap(AccountUser.KEY_ONE, result).toString())
        .type(MediaType.APPLICATION_JSON)
        .build();
    } else {
      return Response.noContent().build();
    }
  }

  /**
   * Delete one AccountUser by accountId and userId
   * TODO: Return 404 if the account is not found.
   *
   * @return application/json response.
   */
  @DELETE
  @RolesAllowed({Role.ADMIN})
  public Response deleteAccountUser() {

    try {
      accountUserController.delete(ULong.valueOf(accountUserId));
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
