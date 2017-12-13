// Copyright (c) 2017, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.phase;

import io.xj.core.CoreModule;
import io.xj.core.access.impl.Access;
import io.xj.core.server.HttpResponseProvider;
import io.xj.core.dao.PhaseDAO;
import io.xj.core.model.phase.Phase;
import io.xj.core.model.phase.PhaseWrapper;
import io.xj.core.model.role.Role;

import org.jooq.types.ULong;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.annotation.security.RolesAllowed;
import javax.jws.WebResult;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 Phases
 */
@Path("phases")
public class PhaseIndexResource {
  private static final Injector injector = Guice.createInjector(new CoreModule());
  private final PhaseDAO DAO = injector.getInstance(PhaseDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("patternId")
  String patternId;

  /**
   Get all phases.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed({Role.ARTIST})
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (patternId == null || patternId.length() == 0) {
      return response.notAcceptable("Pattern id is required");
    }

    try {
      return response.readMany(
        Phase.KEY_MANY,
        DAO.readAll(
          Access.fromContext(crc),
          ULong.valueOf(patternId)));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new phase

   @param data with which to update Phase record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed({Role.ARTIST})
  public Response create(PhaseWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        Phase.KEY_MANY,
        Phase.KEY_ONE,
        DAO.create(
          Access.fromContext(crc),
          data.getPhase()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
