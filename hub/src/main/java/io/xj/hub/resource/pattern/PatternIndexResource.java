// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.pattern;

import com.google.common.collect.ImmutableList;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.PatternDAO;
import io.xj.core.model.pattern.Pattern;
import io.xj.core.model.pattern.PatternWrapper;
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
 Patterns
 */
@Path("patterns")
public class PatternIndexResource extends HubResource {
  private final PatternDAO patternDAO = injector.getInstance(PatternDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("sequenceId")
  String sequenceId;

  @QueryParam("cloneId")
  String cloneId;

  /**
   Get all patterns.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (Objects.isNull(sequenceId) || sequenceId.isEmpty()) {
      return response.notAcceptable("Sequence id is required");
    }

    try {
      return response.readMany(
        Pattern.KEY_MANY,
        patternDAO.readAll(
          Access.fromContext(crc),
          ImmutableList.of(new BigInteger(sequenceId))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new pattern

   @param data with which to update Pattern record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(PatternWrapper data, @Context ContainerRequestContext crc) {
    try {
      Pattern created;
      if (Objects.nonNull(cloneId)) {
        created = patternDAO.clone(
          Access.fromContext(crc),
          new BigInteger(cloneId),
          data.getPattern());
      } else {
        created = patternDAO.create(
          Access.fromContext(crc),
          data.getPattern());
      }
      return response.create(
        Pattern.KEY_MANY,
        Pattern.KEY_ONE,
        created);

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }

}
