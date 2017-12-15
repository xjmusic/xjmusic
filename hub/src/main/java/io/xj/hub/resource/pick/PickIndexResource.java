// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.pick;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.dao.PickDAO;
import io.xj.core.model.pick.Pick;
import io.xj.core.model.user_role.UserRoleType;



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
import java.math.BigInteger;
import java.util.Objects;

/**
 Picks
 */
@Path("picks")
public class PickIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final PickDAO pickDAO = injector.getInstance(PickDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("morphId")
  String morphId;

  /**
   Get all picks.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({UserRoleType.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (Objects.isNull(morphId) || morphId.isEmpty()) {
      return response.notAcceptable("Morph id is required");
    }

    try {
      return response.readMany(
        Pick.KEY_MANY,
        pickDAO.readAll(
          Access.fromContext(crc),
          new BigInteger(morphId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
