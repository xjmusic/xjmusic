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
 Arrangements
 */
@Path("arrangements")
public class ArrangementIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private static Logger log = LoggerFactory.getLogger(ArrangementIndexResource.class);
  private final ArrangementDAO arrangementDAO = injector.getInstance(ArrangementDAO.class);
  private final HttpResponseProvider httpResponseProvider = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("choiceId")
  String choiceId;

  /**
   Get all arrangements.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {
    AccessControl access = AccessControl.fromContext(crc);

    if (choiceId == null || choiceId.length() == 0) {
      return httpResponseProvider.notAcceptable("Choice id is required");
    }

    try {
      JSONArray result = arrangementDAO.readAllIn(access, ULong.valueOf(choiceId));
      if (result != null) {
        return Response
          .accepted(JSON.wrap(Arrangement.KEY_MANY, result).toString())
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
