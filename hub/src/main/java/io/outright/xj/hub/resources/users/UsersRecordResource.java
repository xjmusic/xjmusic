package io.outright.xj.hub.resources.users;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.outright.xj.core.app.CoreModule;
import io.outright.xj.core.app.access.Role;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.exception.DatabaseException;
import io.outright.xj.core.app.output.JSONOutputProvider;
import io.outright.xj.core.external.google.GoogleModule;
import io.outright.xj.hub.HubModule;
import io.outright.xj.hub.controller.user.UserController;
import io.outright.xj.hub.model.user.EditUser;
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
 * Current user
 */
@Path("users/{userId}")
public class UsersRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule(), new HubModule(), new GoogleModule());
  private static Logger log = LoggerFactory.getLogger(UsersRecordResource.class);
  private final UserController userController = injector.getInstance(UserController.class);
  private final JSONOutputProvider jsonOutputProvider = injector.getInstance(JSONOutputProvider.class);

  @PathParam("userId")
  private String userId;

  /**
   * Get one user.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ADMIN})
  public Response getOneUser() throws IOException {

    Record user;
    try {
      user = userController.fetchUserAndRoles(ULong.valueOf(userId));
    } catch (Exception e) {
      return Response.serverError().build();
    }

    if (user != null) {
      return Response
        .accepted(jsonOutputProvider.Record("user", user.intoMap()).toString())
        .type(MediaType.APPLICATION_JSON)
        .build();
    } else {
      return Response.noContent().build();
    }
  }

  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ADMIN})
  public Response putUser(EditUser user) {

    try {
      // TODO: update user or return error
      userController.updateUserRolesAndDestroyTokens(ULong.valueOf(userId), user);
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
