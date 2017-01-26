// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.user;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.model.user.User;
import io.outright.xj.core.transport.JSON;
import io.outright.xj.hub.HubModule;
import io.outright.xj.core.dao.UserDAO;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONObject;

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
public class MeResource {
  private static final Injector injector = Guice.createInjector(new CoreModule(), new HubModule());
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);
  private final UserDAO userDAO = injector.getInstance(UserDAO.class);

  /**
   * Get current authentication.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response getCurrentlyAuthenticatedUser(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);

    JSONObject result;
    try {
      result = userDAO.readOneAble(access, access.getUserId());
    } catch (Exception e) {
      return httpResponseProvider.unauthorized();
    }

    if (result != null) {
      return Response
        .accepted(JSON.wrap(User.KEY_ONE, result).toString())
        .type(MediaType.APPLICATION_JSON)
        .build();
    }
    return httpResponseProvider.unauthorized();
  }
}
