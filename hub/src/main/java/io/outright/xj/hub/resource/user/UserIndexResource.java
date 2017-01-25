package io.outright.xj.hub.resource.user;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControlModule;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.model.user.User;
import io.outright.xj.core.transport.JSON;
import io.outright.xj.hub.HubModule;
import io.outright.xj.core.dao.UserDAO;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Current user
 */
@Path("users")
public class UserIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule(), new HubModule());
  private static Logger log = LoggerFactory.getLogger(UserIndexResource.class);
  private final UserDAO userDAO = injector.getInstance(UserDAO.class);

  /**
   * Get all users.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    AccessControlModule accessControlModule = AccessControlModule.fromContext(crc);

    JSONArray result;
    try {
      if (accessControlModule.matchRoles(new String[]{Role.ADMIN})) {
        result = userDAO.readAll();
      } else {
        result = userDAO.readAllVisible(accessControlModule.getUserId());
      }
    } catch (Exception e) {
      return Response.serverError().build();
    }

    if (result != null) {
      try {
        return Response
          .accepted(JSON.wrap(User.KEY_MANY, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } catch (Exception e) {
        log.error(e.getClass().getName(), e);
        return Response.serverError().build();
      }
    } else {
      return Response.noContent().build();
    }
  }


}
