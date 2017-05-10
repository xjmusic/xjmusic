// Copyright Outright Mental, Inc. All Rights Reserved.
package io.outright.xj.hub.resource.morph;

import io.outright.xj.core.CoreModule;
import io.outright.xj.core.app.access.impl.Access;
import io.outright.xj.core.app.server.HttpResponseProvider;
import io.outright.xj.core.dao.MorphDAO;
import io.outright.xj.core.model.morph.Morph;
import io.outright.xj.core.model.role.Role;

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
 Morphs
 */
@Path("morphs")
public class MorphIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final MorphDAO DAO = injector.getInstance(MorphDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("arrangementId")
  String arrangementId;

  /**
   Get all morphs.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.USER})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (arrangementId == null || arrangementId.length() == 0) {
      return response.notAcceptable("Arrangement id is required");
    }

    try {
      return response.readMany(
        Morph.KEY_MANY,
        DAO.readAll(
          Access.fromContext(crc),
          ULong.valueOf(arrangementId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

}
