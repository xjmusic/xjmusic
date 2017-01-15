package io.outright.xj.hub.resources.users;

import io.outright.xj.core.app.CoreModule;
import io.outright.xj.core.app.access.AccessControlModule;
import io.outright.xj.core.app.access.Role;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.external.google.GoogleModule;
import io.outright.xj.hub.HubModule;
import io.outright.xj.hub.controller.user.UserController;

import com.google.api.client.json.JsonFactory;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.jooq.Record;

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
@Path("users/me")
public class UsersMeResource {
  private static final Injector injector = Guice.createInjector(new CoreModule(), new HubModule(), new GoogleModule());
//  private static Logger log = LoggerFactory.getLogger(UsersMeResource.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);
//  private final JSONOutputProvider accessControlModuleProvider = injector.getInstance(JSONOutputProvider.class);
  private final UserController userController = injector.getInstance(UserController.class);
  private final JsonFactory jsonFactory = injector.getInstance(JsonFactory.class);

  /**
   * Get current authentication.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response getCurrentlyAuthenticatedUser(@Context ContainerRequestContext crc) throws IOException {
    AccessControlModule accessControlModule = AccessControlModule.fromContext(crc);

    Record user;
    try {
      user = userController.fetchUserAndRoles(accessControlModule.getUserId());
    } catch (Exception e) {
      return httpResponseProvider.unauthorized();
    }

    if (user != null) {
      return Response
        .accepted(jsonFactory.toString(user.intoMap()))
        .type(MediaType.APPLICATION_JSON)
        .build();
    }
    return httpResponseProvider.unauthorized();
  }
}
