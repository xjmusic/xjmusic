// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.pattern_event;

import com.google.common.collect.ImmutableList;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternEventDAO;
import io.xj.core.model.pattern_event.PatternEvent;
import io.xj.core.model.pattern_event.PatternEventWrapper;
import io.xj.core.model.user_role.UserRoleType;
import io.xj.core.transport.HttpResponseProvider;
import io.xj.hub.HubResource;

import javax.annotation.security.RolesAllowed;
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
 PatternEvents
 */
@Path("pattern-events")
public class PatternEventIndexResource extends HubResource {
  private final PatternEventDAO patternEventDAO = injector.getInstance(PatternEventDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("patternId")
  String patternId;

  /**
   Get all patternEvents.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (Objects.isNull(patternId) || patternId.isEmpty()) {
      return response.notAcceptable("Pattern id is required");
    }

    try {
      return response.readMany(
        PatternEvent.KEY_MANY,
        patternEventDAO.readAll(
          Access.fromContext(crc),
          ImmutableList.of(new BigInteger(patternId))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new patternEvent

   @param data with which to update PatternEvent record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(PatternEventWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        PatternEvent.KEY_MANY,
        PatternEvent.KEY_ONE,
        patternEventDAO.create(
          Access.fromContext(crc),
          data.getPatternEvent()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
