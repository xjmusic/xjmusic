package io.outright.xj.hub.resource.user;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControlModule;
import io.outright.xj.core.app.exception.BusinessException;
import io.outright.xj.core.dao.UserDAO;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.model.user.User;
import io.outright.xj.core.model.user.UserWrapper;
import io.outright.xj.core.transport.JSON;
import io.outright.xj.hub.HubModule;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.apache.http.HttpStatus;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
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
  private final UserDAO userDAO = injector.getInstance(UserDAO.class);

  @PathParam("id") String userId;

  /**
   * Get one user.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    AccessControlModule accessControlModule = AccessControlModule.fromContext(crc);

    JSONObject result;
    try {
      if (accessControlModule.matchRoles(new String[]{Role.ADMIN})) {
        result = userDAO.readOne(ULong.valueOf(userId));
      } else {
        result = userDAO.readOneVisible(accessControlModule.getUserId(), ULong.valueOf(userId));
      }
    } catch (Exception e) {
      return Response.serverError().build();
    }

    if (result != null) {
      return Response
        .accepted(JSON.wrap(User.KEY_ONE, result).toString())
        .type(MediaType.APPLICATION_JSON)
        .build();
    } else {
      return Response
        .status(HttpStatus.SC_NOT_FOUND)
        .entity(JSON.wrapError("User not found").toString())
        .type(MediaType.APPLICATION_JSON)
        .build();
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
  public Response update(UserWrapper data) {

    try {
      userDAO.updateUserRolesAndDestroyTokens(ULong.valueOf(userId), data);
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

    return Response.accepted("{}").build();
  }

}
