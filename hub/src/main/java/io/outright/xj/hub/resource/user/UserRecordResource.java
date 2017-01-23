package io.outright.xj.hub.resource.user;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.outright.xj.core.CoreModule;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.app.output.JSONOutputProvider;
import io.outright.xj.core.model.user.User;
import io.outright.xj.hub.HubModule;
import io.outright.xj.hub.controller.user.UserController;
import io.outright.xj.core.model.user.UserWrapper;
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
 * User record
 */
@Path("users/{id}")
public class UserRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule(), new HubModule());
  private static Logger log = LoggerFactory.getLogger(UserRecordResource.class);
  private final UserController userController = injector.getInstance(UserController.class);
  private final JSONOutputProvider jsonOutputProvider = injector.getInstance(JSONOutputProvider.class);

  @PathParam("id") String userId;

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
        .accepted(jsonOutputProvider.wrap(User.KEY_ONE,
          jsonOutputProvider.objectFromMap(user.intoMap())).toString())
        .type(MediaType.APPLICATION_JSON)
        .build();
    } else {
      return Response.noContent().build();
    }
  }

  /**
   * Update one User.
   * @param data with which to update User record.
   * @return Response.
   */
  @PUT
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ADMIN})
  public Response putUser(UserWrapper data) {

    try {
      userController.updateUserRolesAndDestroyTokens(ULong.valueOf(userId), data);
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

    return Response.accepted("{}").build();
  }

}
