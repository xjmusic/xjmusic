package io.outright.xj.hub.resources.auth;

import io.outright.xj.core.app.CoreModule;
import io.outright.xj.core.app.access.Role;
import io.outright.xj.core.app.access.UserAccess;
import io.outright.xj.core.external.google.GoogleModule;
import io.outright.xj.core.tables.records.UserRecord;
import io.outright.xj.hub.HubModule;
import io.outright.xj.hub.controller.user.UserController;

import com.google.api.client.json.JsonFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Current user authentication
 */
@Path("auth")
public class AuthResource {
  private static final Injector injector = Guice.createInjector(new CoreModule(), new HubModule(), new GoogleModule());
//  private static Logger log = LoggerFactory.getLogger(AuthResource.class);
  private final JsonFactory jsonFactory = injector.getInstance(JsonFactory.class);
  private final UserController userController = injector.getInstance(UserController.class);

  /**
   * Get current authentication.
   *
   * @return JSONObject that will be returned as an application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response getCurrentAuthentication(@Context ContainerRequestContext crc) throws IOException {
    UserAccess userAccess = UserAccess.fromContext(crc);
    UserRecord user = userController.fetchOneUser(userAccess.getUserId());
    if (user != null) {
      return Response.accepted(jsonFactory.toString(user.intoMap())).build();
    } else {
      return Response.noContent().build();
    }
  }
}
