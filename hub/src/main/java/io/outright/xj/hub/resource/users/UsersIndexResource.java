package io.outright.xj.hub.resource.users;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.Role;
import io.outright.xj.core.app.output.JSONOutputProvider;
import io.outright.xj.hub.HubModule;
import io.outright.xj.hub.controller.user.UserController;
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
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Current user
 */
@Path("users")
public class UsersIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule(), new HubModule());
  private static Logger log = LoggerFactory.getLogger(UsersIndexResource.class);
//  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);
//  private final JSONOutputProvider accessControlModuleProvider = injector.getInstance(JSONOutputProvider.class);
  private final UserController userController = injector.getInstance(UserController.class);
//  private final JsonFactory jsonFactory = injector.getInstance(JsonFactory.class);
  private final JSONOutputProvider jsonOutputProvider = injector.getInstance(JSONOutputProvider.class);

  /**
   * Get all users.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ADMIN})
  public Response getAllUsers(@Context ContainerRequestContext crc) throws IOException {
//    AccessControlModule accessControlModule = AccessControlModule.fromContext(crc);

    ResultSet users;
    try {
      users = userController.fetchUsersAndRoles();
    } catch (Exception e) {
      return Response.serverError().build();
    }

    if (users != null) {
      try {
        return Response
          .accepted(jsonOutputProvider.ListOf("users", users).toString())
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


}
