// Copyright (c) 2017, Outright Mental Inc. (http://outright.io) All Rights Reserved.
package io.xj.hub.resource.pick;

import io.xj.core.CoreModule;
import io.xj.core.app.access.impl.Access;
import io.xj.core.app.server.HttpResponseProvider;
import io.xj.core.dao.PickDAO;
import io.xj.core.model.pick.Pick;
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
 Picks
 */
@Path("picks")
public class PickIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final PickDAO DAO = injector.getInstance(PickDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("morphId")
  String morphId;

  /**
   Get all picks.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (morphId == null || morphId.length() == 0) {
      return response.notAcceptable("Morph id is required");
    }

    try {
      return response.readMany(
        Pick.KEY_MANY,
        DAO.readAll(
          Access.fromContext(crc),
          ULong.valueOf(morphId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
