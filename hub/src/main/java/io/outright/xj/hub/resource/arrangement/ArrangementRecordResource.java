// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.arrangement;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.AccessControl;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.ArrangementDAO;
import io.outright.xj.core.model.arrangement.Arrangement;
import io.outright.xj.core.model.role.Role;
import io.outright.xj.core.transport.JSON;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import org.json.JSONObject;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Arrangement record
 */
@Path("arrangements/{id}")
public class ArrangementRecordResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final ArrangementDAO arrangementDAO = injector.getInstance(ArrangementDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @PathParam("id")
  String id;

  /**
   * Get one arrangement.
   *
   * @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readOne(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);

    JSONObject result;
    try {
      result = arrangementDAO.readOne(access, ULong.valueOf(id));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(Arrangement.KEY_ONE, result).toString())
          .type(MediaType.APPLICATION_JSON)
          .build();
      } else {
        return httpResponseProvider.notFound("Arrangement");
      }

    } catch (Exception e) {
      return httpResponseProvider.failure(e);
    }
  }

}
