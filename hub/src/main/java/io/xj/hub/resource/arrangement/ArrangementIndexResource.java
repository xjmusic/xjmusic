// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.arrangement;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.dao.ArrangementDAO;
import io.xj.core.model.arrangement.Arrangement;
import io.xj.core.model.role.Role;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 Arrangements
 */
@Path("arrangements")
public class ArrangementIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final ArrangementDAO DAO = injector.getInstance(ArrangementDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

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
    if (choiceId == null || choiceId.length() == 0) {
      return response.notAcceptable("Choice id is required");
    }

    try {
      return response.readMany(
        Arrangement.KEY_MANY,
        DAO.readAll(
          Access.fromContext(crc),
          ULong.valueOf(choiceId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
