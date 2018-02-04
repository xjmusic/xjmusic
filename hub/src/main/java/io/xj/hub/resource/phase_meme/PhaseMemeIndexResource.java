// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.phase_meme;

import com.google.common.collect.ImmutableList;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PhaseMemeDAO;
import io.xj.core.model.phase_meme.PhaseMeme;
import io.xj.core.model.phase_meme.PhaseMemeWrapper;
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
 Phase record
 */
@Path("phase-memes")
public class PhaseMemeIndexResource extends HubResource {
  private final PhaseMemeDAO phaseMemeDAO = injector.getInstance(PhaseMemeDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("phaseId")
  String phaseId;

  /**
   Get Memes in one phase.

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
        PhaseMeme.KEY_MANY,
        phaseMemeDAO.readAll(
          Access.fromContext(crc),
          ImmutableList.of(new BigInteger(phaseId))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new phase meme

   @param data with which to update Phase record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(PhaseMemeWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        PhaseMeme.KEY_MANY,
        PhaseMeme.KEY_ONE,
        phaseMemeDAO.create(
          Access.fromContext(crc),
          data.getPhaseMeme()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }
}
