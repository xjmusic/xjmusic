// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.pick;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.AccessControl;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.PickDAO;
import io.outright.xj.core.model.pick.Pick;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Picks
 */
@Path("picks")
public class PickIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private static Logger log = LoggerFactory.getLogger(PickIndexResource.class);
  private final PickDAO pickDAO = injector.getInstance(PickDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("morphId")
  String morphId;

  /**
   * Get all picks.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);

    if (morphId == null || morphId.length() == 0) {
      return httpResponseProvider.notAcceptable("Morph id is required");
    }

    try {
      JSONArray result = pickDAO.readAllIn(access, ULong.valueOf(morphId));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(Pick.KEY_MANY, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return Response.noContent().build();
      }

    } catch (Exception e) {
      log.error("Exception", e);
      return httpResponseProvider.failure(e);
    }
  }

}
