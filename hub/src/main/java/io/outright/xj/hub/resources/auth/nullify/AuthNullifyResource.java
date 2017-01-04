package io.outright.xj.hub.resources.auth.nullify;

import io.outright.xj.core.app.CoreModule;
import io.outright.xj.core.app.access.Role;
import io.outright.xj.core.app.access.UserAccessModel;
import io.outright.xj.core.app.access.UserAccessModelProvider;
import io.outright.xj.core.app.exception.AccessException;
import io.outright.xj.core.app.exception.ConfigException;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.external.google.GoogleModule;
import io.outright.xj.hub.HubModule;
import io.outright.xj.hub.controller.user.UserController;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Nullify current authentication
 */
@Path("auth/no")
public class AuthNullifyResource {
  private Injector injector = Guice.createInjector(new CoreModule(), new HubModule(), new GoogleModule());
  private static Logger log = LoggerFactory.getLogger(AuthNullifyResource.class);
//  private final JsonFactory jsonFactory = injector.getInstance(JsonFactory.class);
  private final UserController userController = injector.getInstance(UserController.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);
  private final UserAccessModelProvider userAccessModelProvider = injector.getInstance(UserAccessModelProvider.class);

  /**
   * Get current authentication, destroy all known access_tokens for that user, and delete the access token browser cookie.
   *
   * @return JSONObject that will be returned as an application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response getCurrentAuthentication(@Context ContainerRequestContext crc) throws IOException {
    UserAccessModel userAccessModel = UserAccessModel.fromContext(crc);
    try {
      userController.destroyAllTokens(userAccessModel.getUserId());
    } catch (AccessException e) {
      return Response.serverError().build();
    } catch (ConfigException e) {
      log.error("configuration error", e);
      return Response.serverError().build();
    }
    return httpResponseProvider.internalRedirectWithCookie("", userAccessModelProvider.newExpiredCookie());
  }
}
