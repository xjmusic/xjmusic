// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.phase_event;

import com.google.common.collect.ImmutableList;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PhaseEventDAO;
import io.xj.core.model.phase_event.PhaseEvent;
import io.xj.core.model.phase_event.PhaseEventWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

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
import java.math.BigInteger;
import java.util.Objects;

/**
 PhaseEvents
 */
@Path("phase-events")
public class PhaseEventIndexResource extends HubResource {
  private final PhaseEventDAO phaseEventDAO = injector.getInstance(PhaseEventDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("phaseId")
  String phaseId;

  /**
   Get all phaseEvents.

   @return application/json response.
   */
  @GET
  @WebResult
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (Objects.isNull(phaseId) || phaseId.isEmpty()) {
      return response.notAcceptable("Phase id is required");
    }

    try {
      return response.readMany(
        PhaseEvent.KEY_MANY,
        phaseEventDAO.readAll(
          Access.fromContext(crc),
          ImmutableList.of(new BigInteger(phaseId))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new phaseEvent

   @param data with which to update PhaseEvent record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(PhaseEventWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        PhaseEvent.KEY_MANY,
        PhaseEvent.KEY_ONE,
        phaseEventDAO.create(
          Access.fromContext(crc),
          data.getPhaseEvent()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
