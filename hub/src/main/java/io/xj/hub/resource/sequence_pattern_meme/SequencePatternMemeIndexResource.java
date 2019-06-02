// Copyright (c) 2018, XJ Music Inc. (https://xj.io) All Rights Reserved.
package io.xj.hub.resource.sequence_pattern_meme;

import com.google.common.collect.ImmutableList;

import io.xj.core.access.impl.Access;
import io.xj.core.dao.SequencePatternMemeDAO;
import io.xj.core.model.sequence_pattern_meme.SequencePatternMeme;
import io.xj.core.model.sequence_pattern_meme.SequencePatternMemeWrapper;
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
 Pattern record
 */
@Path("sequence-pattern-memes")
public class SequencePatternMemeIndexResource extends HubResource {
  private final SequencePatternMemeDAO sequencePatternMemeDAO = injector.getInstance(SequencePatternMemeDAO.class);
  private final HttpResponseProvider response = injector.getInstance(HttpResponseProvider.class);

  @QueryParam("sequencePatternId")
  String sequencePatternId;

  /**
   Get Memes in one pattern.

   @return application/json response.
   */
  @GET
  @RolesAllowed(UserRoleType.ARTIST)
  public Response readAll(@Context ContainerRequestContext crc) throws IOException {

    if (Objects.isNull(sequencePatternId) || sequencePatternId.isEmpty()) {
      return response.notAcceptable("Pattern id is required");
    }

    try {
      return response.readMany(
        SequencePatternMeme.KEY_MANY,
        sequencePatternMemeDAO.readAll(
          Access.fromContext(crc),
          ImmutableList.of(new BigInteger(sequencePatternId))));

    } catch (Exception e) {
      return response.failure(e);
    }
  }

  /**
   Create new pattern meme

   @param data with which to update Pattern record.
   @return Response
   */
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @RolesAllowed(UserRoleType.ARTIST)
  public Response create(SequencePatternMemeWrapper data, @Context ContainerRequestContext crc) {
    try {
      return response.create(
        SequencePatternMeme.KEY_MANY,
        SequencePatternMeme.KEY_ONE,
        sequencePatternMemeDAO.create(
          Access.fromContext(crc),
          data.getSequencePatternMeme()));

    } catch (Exception e) {
      return response.failureToCreate(e);
    }
  }
}
